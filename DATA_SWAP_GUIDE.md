# 합격각 — 실데이터 교체 가이드

> **이 문서 한 장의 주장:** 이 앱은 "시스템(그릇 + 로직)"을 완성해 두고, **실데이터만 꽂으면** 그대로 실서비스로 동작하도록 설계됐다.
> 지금 들어 있는 데이터는 전부 더미(`source='dummy'`)이며, 스키마·분석 로직·앱 화면은 데이터와 무관하게 고정이다.

---

## 1. 3층 구조 — 무엇이 고정이고 무엇이 갈리는가

| 층 | 내용 | 실서비스 전환 시 |
|---|---|---|
| **① 그릇** | 테이블 스키마 (Flyway `V1~V6`) | **그대로** (변경 없음) |
| **② 데이터** | 행(rows) — 대학·모집단위·배치컷·환산규칙·분포 | **여기만 교체** |
| **③ 로직** | SQL 집계·윈도우 함수 분석, 환산점수·지원자격 계산 | **그대로** |
| **③′ 예측 모델** | 합격 라벨·확률 산출식 | 데이터와 같은 방식이면 그대로 / 자체 모델이면 **인터페이스만 교체** |

핵심: ②는 행 교체, ③′은 빈(bean) 교체. 그 외는 손대지 않는다.

---

## 2. 데이터 교체 지점 (②) — 행만 갈아끼움

모두 PostgreSQL 테이블. 신규 Flyway 마이그레이션(`V7__real_data.sql` 등)으로 더미 행을 지우고 실데이터를 INSERT, `source`를 `'megastudy'`(또는 출처명)로 표기하면 끝. **스키마 변경 불필요.**

| 테이블 | 담는 것 | 공개 데이터 출처(예) |
|---|---|---|
| `universities` | 대학 마스터(이름·지역·설립유형) | 대학알리미(academyinfo.go.kr) |
| `recruitment_units` | 모집단위(대학×학과×전형×군·모집인원) | 각 대학 **정시 모집요강**, 어디가(adiga.kr) |
| `score_rules` | 환산식(국·수·영·탐 반영비율, 영어 등급표 JSONB, 지정과목) | 각 대학 **정시 모집요강** |
| `cutoffs` | 연도별 배치컷·경쟁률·지원자평균·목표등록자평균 | 어디가(전년도 입시결과), 대학알리미(경쟁률) |
| `score_dist_params` | 반영유형별 전국 점수 분포(mean/stddev) — 상위누적% 계산용 | 평가원 수능 등급·표준점수↔백분위 자료에서 추정 |

> **교체 절차 예:**
> ```sql
> -- V7__real_data.sql
> DELETE FROM cutoffs WHERE source = 'dummy';
> INSERT INTO cutoffs (recruitment_unit_id, year, cut_percentile, ..., source)
> VALUES (...), (...);   -- source = 'megastudy'
> ```
> `cutoffs (recruitment_unit_id, year)` 복합 UNIQUE 덕에 연도별 시계열이 자연스럽게 쌓이고,
> 다개년이 들어오면 **연도별 추이(LAG) 분석이 자동으로 살아난다**(앱 상세화면 "연도별 배치컷 추이").

### ❌ 외부에서 못 가져오는 단 하나
`mock_applications`(실시간 모의지원 풀)은 **메가 내부에만 존재**하는 데이터(네트워크 효과). 외부 복제 불가.
→ 이 앱에선 더미/시뮬레이션으로 채우고, 실서비스에선 회사의 실제 모의지원 로그를 이 테이블에 적재하면 인기·또래조합 집계가 그대로 실수치가 된다.

---

## 3. 예측 모델 교체 지점 (③′) — 인터페이스 한 개

합격 **라벨·확률**을 내는 부분만 전략 패턴으로 분리돼 있다.

- 인터페이스: `AdmissionPredictor.predict(convertedScore, cutPercentile) → (labelCode, labelName, probability)`
- 기본 구현: `PlaceholderAdmissionPredictor` (내 위치 = 환산−배치컷 → 5단계 + 로지스틱 확률)
- 파일: `backend/.../service/AdmissionPredictor.kt`

두 경우 모두 이 한 곳에서 끝난다:
1. **회사 방식이 "환산점수 vs 배치컷"과 동일** → 기본 구현 그대로 두고 ②(데이터)만 교체.
2. **회사가 자체 모델(ML 등) 사용** → `AdmissionPredictor`를 구현한 빈을 추가(`@Primary`)하면 Spring이 자동 주입. `ScoringService`·컨트롤러·앱은 무수정.

> 환산점수 계산·지원자격(지정과목)·상위누적% 같은 **공용 로직은 `ScoringService`에 그대로** 남아 모델과 분리돼 있다.

---

## 4. 손대지 않는 것 (데이터와 무관)

- **스키마**: Flyway `V1~V6` (그릇)
- **SQL 분석 계층**: `StatsService`(GROUP BY 인기집계), `TrendService`(RANK/PERCENT_RANK 지역순위, LAG 연도추이)
- **앱(안드로이드)**: 화면·데이터레이어 전부. API 응답 형식이 같으므로 무수정.

---

## 5. 전환 체크리스트

- [ ] `universities` / `recruitment_units` 실데이터 적재 (모집요강·어디가)
- [ ] `score_rules` 반영비율·영어환산표 적재 (모집요강)
- [ ] `cutoffs` 연도별 배치컷·경쟁률 적재, `source='megastudy'` (어디가·대학알리미) — 다개년 권장
- [ ] `score_dist_params` 분포 갱신 (평가원 자료 기반)
- [ ] 회사 자체 예측모델이 있으면 `AdmissionPredictor` 구현체 추가, 없으면 생략
- [ ] (실서비스) `mock_applications`에 실제 모의지원 로그 연결
- [ ] 스키마·분석 쿼리·앱 코드 변경 = **0건** 확인

> 즉, 데이터 적재 작업만으로 전체 서비스가 실데이터로 동작한다. 이것이 이 프로젝트의 설계 목표다.
