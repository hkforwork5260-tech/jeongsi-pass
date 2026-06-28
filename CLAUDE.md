# 합격각 (JeongsiPass) — 프로젝트 기억

> 메가스터디 지원 포트폴리오. 정시 합격 분석 + 인스타식 학과 탐색 안드로이드 앱.
> 새 세션은 이 파일 + `plan`(`~/.claude/plans/typed-churning-duckling.md`) 먼저 읽기.

## 정체성
- 메가스터디 정시 합격예측 서비스를 본뜬 앱 + **인스타식 찾아보기(hero)**가 차별점.
- 목표: ① 풀스택 + **SQL 역량 가시화** ② 빠른 시연. 지금은 **더미 데이터**, 나중 실데이터 교체.
- 기반: `~/jobalert` 풀스택 패턴 이식(안드 Compose + Spring Boot + Postgres + Flyway + 익명 기기인증).

## 구조
```
~/정시합격앱/
├── android-app/   안드(Kotlin/Compose). 패키지 com.jeongsi.app. 앱명 "합격각"
├── backend/       Spring Boot 3.5 + Kotlin + JPA + Flyway + PostgreSQL. com.jeongsi.backend
│   └── docker-compose.yml (jeongsi-postgres, db/user/pw 모두 jeongsi)
├── DESIGN_BRIEF.md (디자인 핸드오프; 바탕화면에도 사본)
├── DB_SCHEMA.md / API_CONTRACT.md
└── CLAUDE.md
```

## 기술 결정 (확정)
- DB: PostgreSQL + Docker. 더미데이터: Flyway SQL INSERT(`V2__seed_dummy.sql`).
- 합격판정/확률: 자사 실공식 비공개 → **placeholder**(`ScoringService`). 라벨 안정/적정/소신/상향 + 로지스틱 확률.
- 홈: 균형 구성 + **찾아보기를 hero 축**으로.
- 협업: Claude=백엔드·로직·데이터레이어·디자인시스템 / 사용자=화면 디자인 산출 → Claude가 Compose 구현.

## 현재 상태
### ✅ 완료
- **Phase 0** 안드 스캐폴드: jobalert android-app 복사 → job 도메인 제거(디자인 시스템·테마만 보존) →
  리브랜딩(com.jeongsi.app, FCM/google-services/백엔드 의존성 제거). `assembleDebug` 그린(APK 18.6MB).
  보존 컴포넌트: HiFiButton/Chip/IconBtn/TabBar/OnboardingDots + theme(Color/Type/Theme, 블루 #4F6EF0).
- **Phase 1** 백엔드 스캐폴드 + DB: Flyway V1(8테이블) + V2(대학10·모집단위40·배치컷·환산규칙·모의지원).
  bootRun 부팅 OK, Hibernate validate 통과(JSONB 매핑 포함).
- **Phase 2** 백엔드 API + 합격판정 + SQL 집계 — **전 엔드포인트 curl 검증 완료**:
  성적입력/분석, units 검색·상세·recommend, **discover(점수 랭킹)**, mock-applications, favorites,
  report(군별), stats/popular(SQL GROUP BY), home(D-day). `ScoringService`=환산점수→배치컷 diff→라벨+확률+지정과목 자격.

- **Phase 3** 안드 데이터레이어 + 기능형 화면 6종 — **assembleDebug 그린(APK 23.7MB)**:
  - data/: DeviceId(UUID·SharedPreferences), ApiClient(Retrofit+OkHttp, X-Device-Id 인터셉터, Json SnakeCase),
    ApiService(전 엔드포인트), Models(@Serializable DTO 미러), Repository.
  - ui/screens/: HomeScreen(D-day·요약·hero·빠른메뉴·군별추천·인기TOP5), DiscoverScreen(VerticalPager 릴스+관심/모의지원 토글),
    SearchScreen(필터칩+검색+담기), ReportScreen(군별), ScoreInputScreen(정밀 폼), ScreenCommon/UiState.
  - ui/components/UnitUi.kt: 합격 라벨 색(labelColor) + AdmissionBadge + UnitCardView 공용.
  - MainActivity: NavHost + 하단탭(홈/찾아보기/검색/리포트). HiFiTabBar 탭 입시용으로 교체.
  - **풀스택 라이브 검증**: POST /scores·GET /home·/discover 200, 지정과목 자격체크 정확(확통+사탐→자연계열 지원불가).

- **★ 데이터 확장 + SQL 가시화 + 검색 다듬기 (2026-06-28 세션 2)** — "데이터만 꽂으면 동작" 구조 목표:
  - **검색 필터 UI**: 긴 드롭다운 → **3칸 그리드 + 세로 스크롤 다이얼로그**(`FilterPickerDialog`/`PickerCell`). 학과는 2칸+폰트11sp로 긴 이름 1줄. 균일높이·가운데정렬. (`SearchScreen.kt`)
  - **축1 데이터(`V6__da_units_and_yearly_cutoffs.sql`)**: 다군(DA) 3→**17개**(id77~90), 배치컷 2027단일→**2025·26·27 3개년**(각 90행). 과거연도는 INSERT…SELECT로 단위별 추세(상승/하락/보합) 파생. source='dummy' 유지.
  - **축2 SQL 분석(`TrendService.kt`, JdbcTemplate)**: **RANK/PERCENT_RANK/COUNT OVER(PARTITION BY region)** 지역내 순위 + **CTE+LAG** 연도별 추이. 엔드포인트 `GET /analytics/region-rank`, `/analytics/cutoff-trend/{unitId}`. DTO `RegionRankDto`/`CutoffTrendDto`. 전부 curl 검증. (StatsService=GROUP BY집계, TrendService=윈도우함수 → JPA로 못하는 SQL 분석계층 2종)
  - **앱 노출**: 모집단위 상세화면에 "📍지역 순위(상위%)" 줄 + "연도별 배치컷 추이 ▲▼" 카드. 안드 `CutoffTrend`/`RegionRank` 모델·ApiService·Repository 추가. 에뮬 실측 확인.
  - **합격예측 인터페이스 분리(`AdmissionPredictor.kt`)**: 라벨·확률 산출을 전략패턴으로 분리(기본=`PlaceholderAdmissionPredictor`). `ScoringService`가 주입받아 위임(시그니처 불변, CatalogService 무영향). 데이터 swap 또는 자체모델 swap 둘 다 가능. 결과 동일 검증.
  - **문서**: `DATA_SWAP_GUIDE.md`(3층구조·교체지점·체크리스트 — "데이터만 꽂으면 실서비스" 증명), `DESIGN_HANDOFF.md`(디자인 작업 인계용 파일지도).

- **★ Phase 4 디자인 1차 완료 (2026-06-28 세션 2)** — 방향 **"블루 고급화"**(현 블루+크림 유지). Figma 없이 클로드가 Compose 직접. 결국 사용자가 **나(Claude Code)에게 맡김** → 내가 적용+빌드+에뮬 스샷 검증. 전 화면 에뮬 실측.
  - **디자인 토큰(`theme/Decor.kt` 신규)**: `cardShadow`(soft shadow)·`brandGradient`·`Pill`·`MegastudyBanner`(가로 배너 Crop)·`MegastudyIcon`(M아이콘). 그림자/그라데이션/로고 한 곳에서 조절.
  - **전파**: 홈 **그라데이션 히어로 헤더**(타이틀+D-day Pill+검색바 내장), `SectionTitle` **브랜드 액센트바**(전 화면), 카드 **soft shadow**(`UnitCardView`·`GroupPickCard`·StatCard·ScoreInput·Mock·Discover·ComboCard), 홈 TOP5 **메달 랭크 배지**.
  - **스플래시**(`MainActivity.SplashScreen`): 흰 배경 메가 로고+로딩 스피너 ~1.6초 → 진입.
  - **메가스터디 로고**(받은 3파일 `res/drawable/megastudy_wordmark/icon/lockup`): 홈헤더=M아이콘, 홈·모의지원 하단=가로 배너, 찾아보기=M아이콘, 스플래시=세로 락업. "벤치마킹 크레딧"(공식 제휴 아님).
  - **모의지원 개편**: 버튼 "실제 지원 등록"→"실제 지원", **플랫 펠**(미등록 브랜드아웃라인/등록 솔리드그린✓), **취소 토글**(removeActual), 삭제버튼→우하단 작은 **✕**.
  - **검색 필터**: 드롭다운→**3칸 그리드 다이얼로그**(`FilterPickerDialog`/`PickerCell`), 학과 2칸+폰트11sp.
  - **런처 아이콘**: 시바 마스코트(기반앱 잔재)→**브랜드블루+흰 체크마크**(`ic_launcher_foreground.xml` 벡터).
  - 함정: `MegastudyBanner`는 흰배경 로고를 Crop으로 가로꽉참. 맥 한글파일명 NFD라 대학로고는 보류(받은 폴더는 ㄱ배치=28중 5곳만). 분석 표는 horizontalScroll(정상).

### 🚧 다음
- **Phase 4 디자인 1차 완료.** 추가 손볼 것 생기면 그때. 대학 로고는 전체 초성 배치 다 모이면 일괄(에셋 NFC 정규화+로딩).
- **Phase 5(선택)** Railway 배포.
- (선택) 분석 2종을 별도 "분석/랭킹" 화면으로 확대 / region-rank 한글 파라미터는 URL 인코딩 필요.
- (선택, 포트폴리오) 앱→JD 매핑 자소서/면접 토킹포인트, 발표 PPT, 엑셀 지표 대시보드. (수시·모의고사 풀서비스는 미구현 갭 — 사용자가 수시는 안 건드리기로)

- **★ 메가 충실도 보강 (Phase A·B·C + D일부)** — 로그인된 실제 메가 합격예측 확인 후 분석 깊이 추가:
  - **Phase A(V3)**: score_rules(index_type 활용지표·inquiry_count·수능/내신/기타 비율) + cutoffs(target_avg 목표등록자평균) + user_scores(필수정보 계열/성별/졸업연도 + 원점수 공통/선택) 확장, 신규 target_units·score_dist_params(반영유형별 전국 분포). 엔티티·리포 확장. 검증됨.
  - **Phase B**: `ScoringService` 재설계 — 환산점수(반영비율) → **내위치 ▲▼**(환산−배치컷) → **5단계**(안정/적정/소신/상향/위험). **상위누적%**=정규분포 CDF(score_dist_params, A&S 근사). 신규 `AnalysisService`(반영유형별 국수영탐/과/사/국영탐 × 탐1·2 상위누적% + 유불리 TOP3), `TargetService`(목표대학), ApplicationService.compare(비교분석). API: /scores/analysis 확장·/units/search(sort)·/target-units·/report/compare. **curl 검증 완료**(국수영탐 백분위 281→상위1.98%, 국수영과 같은점수 3.74%).
  - **Phase C**: Models·UnitUi 확장(▲▼ positionText·5색 labelColor·반영영역/활용지표/수능비율 카드 표시). 화면 probability→positionDelta.
  - **Phase D(일부)**: **성적분석 심화 화면**(AnalysisScreen — 유불리 + 반영유형별 상위누적% 표) 신규 + 홈 성적요약 카드 탭 연결. **assembleDebug 그린**.
  - **D 폴리시 완료**: 대학검색 **17지역 필터 + 정렬**(배치컷/내성적맞춤fit/경쟁률), 리포트 **비교분석**(CompareScreen, /report/compare) + **목표대학 설정**(/target-units), 성적입력 **필수정보(계열/성별/졸업연도) + 원점수(공통/선택)**. 신규 화면 AnalysisScreen·CompareScreen. assembleDebug 그린.
  - **더미 확장(V4)**: 대학 10→**28곳**, 모집단위 40→**76개**, **13개 지역**(서울·부산·대전·경기·인천·대구·광주·강원·충북·전북·경남·울산·제주). 5단계 라벨 전부 분포(위험~안정).
  - 2단 구성 유지: 가벼운 층(홈·찾아보기=핵심만) + 깊은 층(성적분석 등). placeholder(정규분포 가정), 실데이터 시 ScoringService·score_dist_params·cutoffs 교체.

- **★ 연결·구조 정리 패스 (디자인 아님)** — 일관성/중복 해결: ①통일 규칙 "학과=뭐든 탭→상세"(홈 인기TOP5·리포트·비교 카드 onClick 신규 연결, 에뮬 확인) ②바로가기 4버튼 제거(하단탭과 중복)+성적 카드에 "성적 수정" 진입 ③BrowseScreen 삭제, 홈 더보기→찾아보기. 정보구조: "지원 가능 학과"=찾아보기(릴스)+검색 2곳. 화면=홈/찾아보기/검색/리포트 탭 + 푸시(상세/분석/비교/성적입력, 뒤로가기 바).

- **★ 모의지원 중심 개편** — ①관심(favorite) 제거, 모의지원으로 통일(Discover/Detail 버튼=모의지원/지원(홈페이지)/상세). ②모의지원 **군별 20개 제한**(ApplicationService.MAX_PER_GROUP, addMock AddResult enum→409). ③**리포트 탭→모의지원 탭**(MockScreen): 군별 목록(N/20)+삭제+군지망 배치(target), **조합 합격확률 분석**(전부불합/1곳+/2곳+/3곳, 독립 가정 곱셈) + 라벨구성. 백엔드 `GET /mock-applications/summary`(MockSummaryDto). ④검색 예체능 토글 추가(4모드는 이미 구현돼 있었음). ReportScreen/CompareScreen은 미사용(라우트 남음). favorites 엔드포인트는 백엔드에 잔존(미사용).

- **★ 모의지원/홈/네비 대개편 (7건)** — ①모의지원 탭 전면 재작성: [가][나][다] 큰버튼→군별 목록(N/20)→**실제 지원 등록**(AlertDialog 확인+하루3번 안내)→**실제지원 슬롯(가/나/다·남은변경)**→**합격예측 분석 막대그래프**(1곳+/2곳+/3곳/전부불합+구성). 백엔드 `actual_applications`(V5)·ActualApplicationService(set 일일3제한 SetResult)·`POST /actual-applications/{id}`, MockSummaryDto에 actualPicks·remainingChanges(조합확률=실제지원 기준). ②군별추천 랜덤화(CatalogService.groupRecommend take6.randomOrNull)+홈 ON_RESUME 재로드+가나다 크게. ③성적요약 평균백분위 줄 제거. ④hero 더보기→RecommendScreen(라벨별, /units/recommend). ⑤상세 지원버튼 제거+군 우측 크게. ⑥군별추천 더보기→StrategyScreen(프리셋 안정/균형/소신형, `/units/strategy?preset=`). ⑦검색 탭 제거→3탭(홈/찾아보기/모의지원)+홈 검색칸→search 푸시. ReportScreen/CompareScreen/TargetService·favorites=미사용 잔존.

- **★ 디테일 개선 라운드 2 (6건)** — ①모의지원 버튼 "✓ 담음"→"모의지원"(색상으로 구분). ②검색에 **학교 필터**(스크롤칩, `/units/universities`)+search university 파라미터. ③④**전략 화면 재작성**: 프리셋+**군별 라벨 직접지정**, **조합 12개씩**(카르테시안, `/units/strategy-combos` ga/na/da/track/offset, 안정성순), 조합카드(가/나/다 각 줄→상세, **자세히→ComboDetailScreen**), 스크롤 더보기. 군별추천 홈카드=GroupPickCard(가파랑/나초록/다주황·간소화·칩제거). ⑤찾아보기 1칸씩(atMost(1), PC마우스라 쫙 넘어가는 것). ⑥a 군별합격률 제거. ⑥b **조합 분석 추가**: 안정성 지수(0~100, ComboMath)+한줄평, 빈군 채우기/조정 추천(fillCandidate), 또래 인기 조합(mock 집계 peerCombo). 상세 군배지 "다"만.

### ▶ 앱 실행법 (데모)
1. 백엔드: `cd ~/정시합격앱/backend && docker compose up -d && ./gradlew bootRun`
2. 안드: Android Studio로 `~/정시합격앱/android-app` 열고 **에뮬레이터**에서 Run (BASE_URL=10.0.2.2:8080).
   실기기는 `ApiClient.BASE_URL`을 PC의 LAN IP로 교체.

## 실행법
```bash
cd ~/정시합격앱/backend && docker compose up -d && ./gradlew bootRun   # 백엔드(8080)
curl localhost:8080/api/v1/home -H "X-Device-Id: test"                # 확인
cd ~/정시합격앱/android-app && ./gradlew assembleDebug                  # 안드(사용자 맥에서)
```

## 함정/메모
- compose 프로젝트명 충돌 방지: jeongsi docker-compose에 `name: jeongsi` 박음(jobalert과 격리). 둘 다 5432라 동시 기동 X.
- Jackson 단일문자 프로퍼티(`dDay`)는 snake 변환 어긋남 → `@get:JsonProperty("d_day")`로 고정.
- 더미 배치컷·반영비율은 현실 근사값(실데이터 아님). `cutoffs.source='dummy'`.
- 안드 빌드 검증은 사용자 맥에서(`assembleDebug`). 백엔드는 이 환경에서 docker+bootRun 가능.
- bootRun을 background로 여러 번 재시작하면 **JVM이 쌓여 8080 충돌** → 재시작 전 `lsof -ti tcp:8080 | xargs kill -9` + `ps|grep bootRun` 정리. `adb install -r` 후 앱 종료되니 `am start` 재실행.
- cutoffs 조회는 전부 `year=2027` 한정(StatsService/CatalogService) → 과거연도(2025·26) 추가해도 기존 화면 안 깨짐. 분석(TrendService)만 전 연도 사용.
- 디자인 작업은 `DESIGN_HANDOFF.md`, 실데이터 전환은 `DATA_SWAP_GUIDE.md` 참고.
