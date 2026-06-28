# 합격각 — DB 스키마 & SQL 설계 노트

> PostgreSQL 16. Flyway 마이그레이션(`backend/src/main/resources/db/migration/`).
> 정시 합격 분석 도메인을 정규화한 관계형 스키마. **이 문서는 SQL 설계 역량 설명용.**

## ERD (관계)
```
universities ──< recruitment_units ──< cutoffs          (대학 1:N 모집단위 1:N 배치컷[연도별])
                       │     └──── score_rules (1:1)      (모집단위별 환산 규칙)
                       │
devices ──< user_scores (1:1)                            (기기 1:1 성적)
   │
   ├──< mock_applications >── recruitment_units           (모의지원: 기기 × 모집단위)
   └──< favorites          >── recruitment_units           (관심:   기기 × 모집단위)
```

## 테이블 (8)
| 테이블 | 행수(더미) | 핵심 설계 의도 |
|---|---|---|
| `universities` | 10 | 대학 마스터. region 인덱스. |
| `recruitment_units` | 40 | **핵심 아이템** = 대학×학과×전형×군. group/track 인덱스. |
| `score_rules` | 40 | 모집단위 1:1 환산식. **JSONB**(영어 등급→점수표) 사용. |
| `cutoffs` | 40 | **연도별 배치컷**을 분리 → JOIN·이력 관리. `(unit,year)` UNIQUE. |
| `devices` | 8+ | 익명 기기(X-Device-Id). |
| `user_scores` | N | 기기 1:1 성적(국·수·영·한·탐2 + 선택과목). |
| `mock_applications` | 36+ | 모의지원. `(device,unit)` UNIQUE로 중복 방지. |
| `favorites` | N | 관심 학과. `(device,unit)` UNIQUE. |

## 설계 포인트 (어필거리)
1. **배치컷 분리(cutoffs)** — 모집단위에 컬럼으로 박지 않고 연도별 테이블로 분리해 **시계열 이력**과
   **JOIN 집계**를 자연스럽게. `(recruitment_unit_id, year)` 복합 UNIQUE.
2. **JSONB 활용** — `score_rules.english_grade_score`에 영어 등급별 환산 점수표를 JSONB로 저장(대학마다 다름).
   Hibernate `@JdbcTypeCode(SqlTypes.JSON)`로 매핑.
3. **시드 시 명시적 PK + 시퀀스 동기화** — FK 안정성 위해 명시적 id INSERT 후
   `SELECT setval('..._id_seq', MAX(id))`로 시퀀스 정렬.
4. **무결성** — 모든 FK `ON DELETE CASCADE`, 중복 방지 UNIQUE 제약, 조회 경로에 인덱스.

## 대표 쿼리 (SQL 쇼케이스)

### ① 성적대별 인기 모집단위 TOP5 (`StatsService`)
모의지원 수를 JOIN + GROUP BY로 집계. 배치컷 구간(band)·계열·군으로 필터.
```sql
SELECT u.name, ru.department_name, ru.recruit_group,
       c.cut_percentile, c.competition_rate,
       COUNT(ma.id) AS mock_cnt
FROM recruitment_units ru
JOIN universities u ON u.id = ru.university_id
JOIN cutoffs c      ON c.recruitment_unit_id = ru.id AND c.year = 2027
LEFT JOIN mock_applications ma ON ma.recruitment_unit_id = ru.id
WHERE c.cut_percentile >= :cutMin AND c.cut_percentile < :cutMax
GROUP BY u.name, ru.department_name, ru.recruit_group, c.cut_percentile, c.competition_rate
ORDER BY mock_cnt DESC, c.cut_percentile DESC
LIMIT 5;
```

### ② 합격 판정 — 내 환산점수 vs 배치컷 (참고: 앱은 서비스 계층에서 계산)
SQL로도 표현 가능함을 보이는 예시. `CASE`로 라벨 분류:
```sql
SELECT u.name, ru.department_name, c.cut_percentile,
       round((s.korean_pct*r.weight_korean + s.math_pct*r.weight_math
            + s.inquiry1_pct*r.weight_inquiry) / 100.0, 1) AS my_score,
       CASE
         WHEN (계산식) - c.cut_percentile >= 5  THEN '안정'
         WHEN (계산식) - c.cut_percentile >= 1  THEN '적정'
         WHEN (계산식) - c.cut_percentile >= -3 THEN '소신'
         ELSE '상향'
       END AS label
FROM recruitment_units ru
JOIN cutoffs c     ON c.recruitment_unit_id = ru.id AND c.year = 2027
JOIN score_rules r ON r.recruitment_unit_id = ru.id
CROSS JOIN user_scores s
WHERE s.device_id = :deviceId
ORDER BY c.cut_percentile DESC;
```

## 실데이터 교체 경로
- `cutoffs.source` = `'dummy'` → 실데이터 적재 시 `'megastudy'` 등으로. 스키마 변경 없이 행만 교체.
- `score_rules` 반영비율·영어표도 동일 형식으로 갱신.
- 즉, **스키마는 그대로 두고 데이터만 갈아끼우면** 앱 전체가 실데이터로 동작.
