# 합격각 — 세션 종료 리포트 (2026-06-28)

> 정시 합격 분석 + 인스타식 학과 탐색 안드로이드 앱(메가스터디 지원 포트폴리오).
> 풀스택: 안드 Kotlin/Compose + Spring Boot + PostgreSQL(Docker) + Flyway. 더미 데이터.
> **다음 세션은 이 파일 + `CLAUDE.md` 먼저 읽기.** (CLAUDE.md에 단계별 상세 전부 있음)

## ✅ 완료 (이번까지 누적)
- 풀스택 토대 + 메가 합격예측 깊이: 환산점수·▲▼ 내위치·5단계(안정/적정/소신/상향/위험)·합격확률%·
  반영영역/비율·전국 상위누적%(정규CDF)·유불리 분석.
- 화면: 홈(검색칸·성적요약·지원가능 미리보기·군별추천·인기TOP5) / 인스타 찾아보기(릴스, 1칸씩) /
  성적입력(필수정보 없음·원점수/표준/백분위·탐구 선택과목 칩) / 성적분석 심화 / 모집단위 상세(군 우측 크게, 지원버튼 없음) /
  지원가능 학과(라벨별 접기+계열·군 필터) / **모의지원 탭**(가·나·다→실제지원 등록 확인·하루3번 제한→조합 합격확률·안정성지수·빈군추천·또래조합) /
  전략(군별 라벨 직접지정 안정~위험, 조합 12개+ 스크롤, 자세히→조합상세) / 검색(드롭다운 필터 계열·군·지역·학교·학과).
- 하단 3탭: 홈 / 찾아보기 / 모의지원 (검색은 홈 검색칸→푸시). 관심(favorite) 기능 제거(모의지원 통일).
- 모든 학과 카드 탭 → 상세 일관. 모의지원 버튼=채움↔외곽선(비색상) 구분.
- DB: 대학 28·모집단위 76·13지역. Flyway V1~V5(actual_applications까지).
- 안드 `assembleDebug` 그린, 백엔드 curl 검증 다수. 에뮬레이터(Pixel_7)로 화면 실측 검증.

## 🟡 다음에 이어갈 지점 (후보)
1. **디자인 입히기 (Phase 4)** — 지금은 기능형 UI(블루 디자인시스템). 임현경님 Figma 디자인 도착 시 화면별로 반영.
   디자인 브리프: 바탕화면 `합격각_디자인브리프.md` (단, 그동안 화면 늘어남 — 갱신 필요).
2. 남은 다듬기(사용자 피드백 계속). 더미 다군(DA) 학과가 적어 전략 조합 다양성 제한 → 다군 더미 확장 고려.
3. (선택) 정리: 미사용 코드(ReportScreen·CompareScreen·TargetService·favorites 엔드포인트·diagnose()) 삭제.

## 💾 갱신 파일
- `CLAUDE.md` (프로젝트 영속 기억 — 단계별 상세 전부)
- `android-app/` (전 화면·데이터레이어), `backend/` (API·서비스·Flyway V1~V5), `DESIGN_BRIEF.md`·`DB_SCHEMA.md`·`API_CONTRACT.md`

## ▶ 실행법
```bash
cd ~/정시합격앱/backend && docker compose up -d && ./gradlew bootRun   # 백엔드(8080)
# 안드: Android Studio로 ~/정시합격앱/android-app 열고 Pixel_7 에뮬레이터 Run
#   (또는 CLI: ~/Library/Android/sdk/emulator/emulator -avd Pixel_7 &  + adb install)
# 앱 BASE_URL=10.0.2.2:8080 (에뮬레이터). 앱 device_id(테스트): f6b0dee2-29eb-4b60-bb23-65f93a1709cd
```

## 함정/메모
- 더미 placeholder(배치컷·확률·상위누적·전략). 실데이터 시 `ScoringService`·`ComboMath`·`score_dist_params`·cutoffs 교체.
- 찾아보기 "쫙 넘어감"은 PC 마우스 드래그 artifact(실기기는 atMost(1)로 1칸씩 정상).
- compose 프로젝트명 jeongsi(jobalert과 격리). 둘 다 5432 → 동시 기동 X.
