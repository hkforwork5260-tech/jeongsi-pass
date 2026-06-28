# 채용알리미 Android App

`project/design_handoff_jobalert/` 디자인 핸드오프를 기반으로 한 Jetpack Compose 구현체.

## 빠르게 열기

1. Android Studio Hedgehog (또는 Iguana 이상) 에서 `android-app/` 폴더를 열기
2. Gradle Sync (첫 실행 시 Android Gradle Plugin·Compose BOM 자동 다운로드)
3. `app` configuration으로 Run → Android 26 이상 디바이스/에뮬레이터에서 실행

Android Studio가 없으면 (네트워크 + Android SDK 필요):
```
./gradlew :app:assembleDebug
```

## 이번 세션 구현 범위

- [x] 프로젝트 스캐폴드 (Gradle 8.10.2 + AGP 8.6 + Kotlin 2.0.21 + Compose BOM 2024.10)
- [x] 디자인 시스템 (Color/Type/Theme — README 토큰 1:1 매핑)
- [x] 듀오링고풍 3D 버튼 (`HiFiButton`)
- [x] 칩 (`HiFiChip`, `HiFiLabel`)
- [x] 폰 프레임 컴포넌트 (`HiFiStatusBar`, `HiFiAppBar`, `HiFiGestureNav`)
- [x] 아이콘 버튼 (`HiFiIconBtn`)
- [x] 공고 카드 (`HiFiJobCard`)
- [x] 5탭 하단 네비게이션 (`HiFiTabBar`)
- [x] 마스코트 꽁이 SVG → Canvas 이식 (`Mascot`)
- [x] 화면 3개:
  - 온보딩 ① 직군 선택 (`OnboardingJobCategoryScreen`)
  - 메인 피드 (`MainScreen`)
  - 공고 상세 (`JobDetailScreen`)
- [x] Navigation Compose 라우터 + 23개 placeholder

## 다음 세션 (구현 예정)

- [ ] 온보딩 ②~④ (기업 규모/산업, 회사 스와이프 Reels, 위젯 권한)
- [ ] 메인 빈 상태
- [ ] 필터 풀스크린
- [ ] 검색 + 검색 결과
- [ ] 찾아보기 Reels (vertical pager)
- [ ] 관심 기업 그리드
- [ ] 회사 상세 2종 (공고 있음/없음)
- [ ] 마이페이지 + 서브 5개
- [ ] 알림 히스토리 / 마감 캘린더 / 공유 시트 / 비슷한 공고
- [ ] 잠금화면 push (FCM)
- [ ] 홈 위젯 (App Widget API + Glance)

## 아키텍처 메모

- **데이터**: 현재 `data/sample/SampleJobs.kt`에 하드코딩. Repository 인터페이스로 분리 후 백엔드 붙이기 쉽게 구조 잡혀 있음.
- **테마**: `MaterialTheme`은 일부 슬롯만 매핑 — Material 컴포넌트는 거의 안 쓰고 거의 다 커스텀 (디자인 충실도 우선).
- **마스코트**: 현재 Canvas로 SVG path 재현. 출시 시 일러스트레이터 의뢰 → VectorDrawable 또는 Lottie로 교체.
- **폰트**: 시스템 sans-serif fallback. `res/font/pretendard_variable.ttf` 추가 후 `PretendardFamily`를 그 폰트로 교체.
- **3D 버튼**: Compose에 box-shadow가 없어서 그림자 박스 + 본체 박스 2개 겹친 구조. 누르면 본체만 translateY로 내려가 보이는 그림자 두께가 줄어드는 식.

## 폴더 구조

```
android-app/
├── build.gradle.kts            # 프로젝트 빌드
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
├── gradle/wrapper/
└── app/
    ├── build.gradle.kts        # 모듈 빌드 (Compose deps)
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── kotlin/com/jobalert/app/
        │   ├── MainActivity.kt
        │   ├── data/
        │   │   ├── model/Job.kt
        │   │   └── sample/SampleJobs.kt
        │   ├── nav/NavGraph.kt
        │   └── ui/
        │       ├── components/  # HiFiButton, JobCard, Mascot 등 9개
        │       ├── screens/
        │       │   ├── onboarding/OnboardingJobCategoryScreen.kt
        │       │   ├── main/MainScreen.kt
        │       │   └── detail/JobDetailScreen.kt
        │       └── theme/       # Color, Type, Theme
        └── res/                 # strings/themes/launcher icons
```
