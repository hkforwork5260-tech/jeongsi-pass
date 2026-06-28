# 합격각 — 세션 종료 리포트 (2026-06-28 세션 2)

> 정시 합격 분석 + 인스타식 학과 탐색 안드(Compose) + Spring Boot + PostgreSQL 풀스택. 더미 데이터.
> **다음 세션은 이 파일 + `CLAUDE.md` 먼저 읽기.** 디자인은 `DESIGN_HANDOFF.md`, 실데이터 전환은 `DATA_SWAP_GUIDE.md`.
> (이전 리포트: `session_end_2026-06-28.md` = 세션 1)

## ✅ 완료 (이번 세션)
- **검색 필터 UI 개선** — 긴 드롭다운 → **3칸 그리드 + 세로스크롤 다이얼로그**. 학과는 2칸+폰트11sp(긴 이름 1줄), 균일높이·가운데정렬. (`SearchScreen.kt`)
- **축1 데이터 확장 (Flyway `V6`)** — 다군(DA) 3→**17개**, 배치컷 2027단일→**2025·26·27 3개년**(단위별 추세 파생). source='dummy'. 적용·검증.
- **축2 SQL 분석 (`TrendService`)** — **윈도우 함수** RANK/PERCENT_RANK(지역내 순위), CTE+LAG(연도별 추이). `GET /analytics/region-rank`·`/analytics/cutoff-trend/{id}`. curl 검증.
- **앱 노출** — 모집단위 상세화면에 "📍지역 순위·상위%" + "연도별 배치컷 추이 ▲▼". 안드 모델/ApiService/Repository 추가. 에뮬 실측 확인.
- **합격예측 인터페이스 분리** — `AdmissionPredictor`(전략패턴) + 기본구현 `PlaceholderAdmissionPredictor`. `ScoringService` 위임(시그니처 불변). 데이터/모델 둘 다 swap 가능. 결과 동일.
- **문서 2종** — `DATA_SWAP_GUIDE.md`("데이터만 꽂으면 동작" 증명), `DESIGN_HANDOFF.md`(디자인 인계).
- **디자인 Phase 4 — 1차 완료 (블루 고급화, 클로드가 적용·에뮬 검증)**:
  - 토큰 `theme/Decor.kt`: `cardShadow`/`brandGradient`/`Pill`/`MegastudyBanner`/`MegastudyIcon`.
  - 전 화면 전파: 그라데이션 히어로 헤더, 섹션 액센트바, 카드 soft shadow, TOP5 메달랭크.
  - **스플래시**(`MainActivity`): 메가 로고+로딩 → 진입.
  - **메가 로고** 4곳(헤더 M아이콘/홈·모의지원 하단 가로배너/찾아보기 M아이콘/스플래시 락업). `res/drawable/megastudy_*`.
  - **모의지원 버튼** 플랫 펠+취소 토글+✕ 삭제. **런처 아이콘** 시바→체크마크.

## 🟡 이어갈 지점
- 추가 디자인 디테일(생기면). 대학 로고는 전체 초성 배치 모이면 일괄(에셋 NFC 정규화).
- (선택) 분석 2종 → 별도 "분석/랭킹" 화면 확대.
- (선택) 미사용 코드 정리(ReportScreen·CompareScreen·favorites 등).
- (선택, 포트폴리오) 앱→JD 매핑 자소서/면접 토킹포인트·발표 PPT·엑셀 대시보드. 수시/모의고사 풀서비스=미구현 갭(수시는 안 건드리기로).

## 💾 갱신/신규 파일
- 신규(백): `service/TrendService.kt`·`AdmissionPredictor.kt`, `db/migration/V6__da_units_and_yearly_cutoffs.sql`
- 신규(안드): `ui/theme/Decor.kt`, `res/drawable/megastudy_wordmark.png`·`megastudy_icon.png`·`megastudy_lockup.jpg`
- 신규(문서): `DATA_SWAP_GUIDE.md`, `DESIGN_HANDOFF.md`, `DESIGN_FILES_BUNDLE.md`
- 수정(백): `Controllers.kt`·`ScoringService.kt`·`dto/Dtos.kt`
- 수정(안드): `MainActivity.kt`(스플래시)·`HomeScreen.kt`·`SearchScreen.kt`·`UnitDetailScreen.kt`·`MockScreen.kt`·`DiscoverScreen.kt`·`ScreenCommon.kt`·`ScoreInputScreen.kt`·`data/Models.kt`·`ApiService.kt`·`Repository.kt`·`ui/components/UnitUi.kt`·`res/drawable/ic_launcher_foreground.xml`
- 갱신: `CLAUDE.md`(현재상태·함정)

## ▶ 실행법
```bash
cd ~/정시합격앱/backend && docker compose up -d && ./gradlew bootRun   # 8080
cd ~/정시합격앱/android-app && ./gradlew assembleDebug                  # 안드(사용자 맥)
# 에뮬: adb install -r ...app-debug.apk && adb shell am start -n com.jeongsi.app.debug/com.jeongsi.app.MainActivity
```

## 함정/메모
- bootRun background 다중 재시작 시 JVM 쌓여 8080 충돌 → 재시작 전 `lsof -ti tcp:8080|xargs kill -9` 정리.
- cutoffs 조회 전부 year=2027 한정 → 과거연도 추가해도 기존화면 무영향. 분석만 전 연도.
- region-rank 한글 파라미터(지역)는 URL 인코딩 필요(`curl --data-urlencode`).
- `HiFiChip` 등 공용 컴포넌트 안 깨기 — 화면 전용 요구는 전용 컴포저블로(예: `PickerCell`).
- 백엔드 현재 8080 가동 중(세션 종료 시점).
