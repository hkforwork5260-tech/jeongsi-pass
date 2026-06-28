# 합격각 — 디자인 작업 핸드오프 (클로드에게 줄 정리)

> 이 파일을 클로드에 통째로 주거나, 작업할 화면의 줄만 짚어주세요.
> **목표:** 지금의 "기능형 블루 UI"를 **블루 고급화**(현 블루+크림 톤 유지, 계층·여백·그림자·마이크로인터랙션으로 완성도↑)로 끌어올리기.
> **방식:** Figma 없이 **클로드가 Compose 코드로 직접 디자인**. 디자인 시스템(토큰) 먼저 → 화면 적용.

---

## 0. 작업 규칙 (중요)

- **토큰 우선:** 색/글자/간격/그림자는 `ui/theme/`에서 정의. 화면은 토큰을 참조만 → 한 곳 바꾸면 전 화면 전파.
- **공용 컴포넌트 깨지 않기:** `HiFiButton/HiFiChip/UnitUi` 등은 여러 화면이 공유. 한 화면만 다르게 하려면 그 화면 안에 전용 컴포넌트를 따로 만들 것(예: 검색화면의 `PickerCell`).
- **한 화면씩 기준점 → 검토 → 전파.** 홈을 먼저 완성하고 그 톤을 나머지에 전파하는 순서 권장.
- **빌드/검증은 사용자 맥에서.** 아래 명령 참고. 백엔드(8080) 떠 있어야 화면에 데이터 나옴.

---

## 1. 디자인 시스템 (먼저 보고 만질 곳) — `app/src/main/kotlin/com/jeongsi/app/ui/theme/`

| 파일 | 역할 | 디자인 시 |
|---|---|---|
| `Color.kt` | 색 토큰 `HiFiColors` (Brand 블루 #4F6EF0, 웜화이트 Bg #FAF7F2, 오렌지 Update, 상태색) | 팔레트 조정의 중심 |
| `Type.kt` | 타이포 `HiFiType` (display/title/h2/body/body2/caption). Pretendard fallback=sans-serif | 폰트 스케일·웨이트 |
| `Decor.kt` | **(신규)** `cardShadow()` 그림자, `brandGradient()` 그라데이션, `Pill()` 알약배지 | 입체감·그라데이션 헬퍼 |
| `Theme.kt` | MaterialTheme 래퍼 | 거의 안 건드림 |

> **폰트 팁:** 진짜 Pretendard를 쓰려면 `app/src/main/res/font/`에 ttf 추가 후 `Type.kt`의 `PretendardFamily` 교체. 지금은 시스템 sans-serif.

---

## 2. 화면 — `app/src/main/kotlin/com/jeongsi/app/ui/screens/`

각 파일은 `~ViewModel`(데이터 로드) + `~Content`(UI)로 구성. **디자인은 `~Content`와 그 안 보조 컴포저블만 손대면 됨.**

| 파일 | 화면 | 우선순위 |
|---|---|---|
| `HomeScreen.kt` | 홈(첫인상·hero) | ★ 1순위 — 여기 톤 확정 |
| `DiscoverScreen.kt` | 인스타식 찾아보기(릴스) | ★ 차별화 포인트 |
| `UnitDetailScreen.kt` | 모집단위 상세(추이·지역순위 카드 포함) | 2순위 |
| `SearchScreen.kt` | 대학 검색(필터 다이얼로그) | 2순위 |
| `MockScreen.kt` | 모의지원(군별·조합확률) | 2순위 |
| `ScoreInputScreen.kt` | 성적 입력 폼 | 3순위 |
| `AnalysisScreen.kt` | 성적분석 심화 | 3순위 |
| `RecommendScreen.kt` / `StrategyScreen.kt` / `ComboDetailScreen.kt` | 추천·전략·조합상세 | 3순위 |
| `ScreenCommon.kt` | **공용 레이아웃** `ScrollColumn`/`SectionTitle`/`AppTopBar`/`CenterText` | 여기 손보면 전 화면 전파 |
| ~~`ReportScreen.kt` / `CompareScreen.kt`~~ | **미사용**(라우트만 잔존) | 건드리지 말 것 |

---

## 3. 공용 컴포넌트 — `app/src/main/kotlin/com/jeongsi/app/ui/components/`

| 파일 | 역할 |
|---|---|
| `HiFiButton.kt` | 3D 버튼(Primary/Ghost, 크기) |
| `HiFiChip.kt` | 칩(Solid/Outline, small) |
| `HiFiIconBtn.kt` | 아이콘 버튼 |
| `HiFiTabBar.kt` | 하단 3탭(홈/찾아보기/모의지원) |
| `UnitUi.kt` | `UnitCardView`/`AdmissionBadge`/`labelColor`/`UnivAvatar` (학과 카드 공용) |
| `OnboardingDots.kt` | 페이지 인디케이터 |

> 카드 전체 톤을 바꾸려면 `UnitUi.kt`의 `UnitCardView`가 핵심(검색·찾아보기·추천 등에서 공유).

---

## 4. 지금 진행 상태 (홈 일부 시작됨 — 이어서 하거나 갈아엎어도 됨)

`HomeScreen.kt`에 이미 적용된 것:
- 상단을 **그라데이션 히어로 헤더**로 교체(합격각 타이틀 + `Pill("D-…")` + 검색바를 그 안에).
- `성적 카드`·`HeroCard`에 `cardShadow()` 적용(평면→입체).
- `Decor.kt` 신규 생성(그림자/그라데이션/알약).

남은 홈 작업(원안): 인기 TOP5 메달 랭크 배지 + 카드화, 군별추천 카드 그림자 통일.
→ **이 시작점이 마음에 안 들면 `HomeScreen.kt` 헤더 블록과 `Decor.kt`를 통째로 다시 디자인해도 됨.** (현재 컴파일 OK 상태)

---

## 5. 빌드·실행·검증 (사용자 맥)

```bash
# 백엔드(데이터) — 8080. 화면에 실데이터 나오게 하려면 필요
cd ~/정시합격앱/backend && docker compose up -d && ./gradlew bootRun

# 안드 빌드
cd ~/정시합격앱/android-app && ./gradlew assembleDebug

# 에뮬레이터 설치·실행 (Pixel_7)
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.jeongsi.app.debug/com.jeongsi.app.MainActivity

# 스크린샷으로 확인
adb exec-out screencap -p > /tmp/shot.png
```
- 패키지: `com.jeongsi.app.debug` / 앱 BASE_URL=`10.0.2.2:8080`(에뮬)
- 성적 있는 테스트 기기 id: `f6b0dee2-29eb-4b60-bb23-65f93a1709cd`

## 6. 함정
- `HiFiChip` 등 공용 컴포넌트는 텍스트 좌측정렬 등 기본값 있음 → 화면별 특수 요구는 전용 컴포넌트로.
- `cardShadow`는 `clip=false`라 그림자가 밖으로 번짐. 스크롤 컨테이너 가장자리에서 살짝 잘릴 수 있음(정상).
- 빌드 후 `adb install -r` 하면 앱이 종료됨 → 다시 `am start` 필요.
