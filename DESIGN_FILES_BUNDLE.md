# 합격각 — 디자인 작업용 소스 번들

> DESIGN_HANDOFF.md와 함께 이 파일을 클로드에 첨부하세요. 디자인에 필요한 실제 코드 전부입니다.
> 방향=블루 고급화(현 블루+크림 유지, 계층·여백·그림자). 토큰(theme/)부터 → HomeScreen 적용 → 전파.

---

## `ui/theme/Color.kt`

```kotlin
package com.jeongsi.app.ui.theme

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * HiFi 디자인 토큰.
 * 2026-06-08 리브랜딩: 코랄 → "신뢰의 블루 × 따뜻한 시바 오렌지" (mascot-dan/palette.css 기준).
 * 60% 웜화이트 배경 · 30% 블루 · 10% 오렌지(+상태색 소량).
 * 이름은 유지(전 화면이 참조) — 값만 교체.
 */
object HiFiColors {
    // Brand (Primary 블루 · 신뢰/전문)
    val Brand = Color(0xFF4F6EF0)
    val BrandHover = Color(0xFF3D58D6)
    val BrandShadow = Color(0xFF3A52C9)   // 3D 버튼 그림자
    val BrandDark = Color(0xFF3D58D6)
    val BrandSoft = Color(0xFFEAEEFE)

    // NEW (Success 초록)
    val New = Color(0xFF1FA968)
    val NewShadow = Color(0xFF178A55)
    val NewSoft = Color(0xFFE7F6EE)

    // UPDATE (Accent 시바 오렌지)
    val Update = Color(0xFFE89A4A)
    val UpdateShadow = Color(0xFFC9803A)
    val UpdateSoft = Color(0xFFFCEBD6)

    // CLOSING (Alert 빨강)
    val Closing = Color(0xFFF0533A)
    val ClosingShadow = Color(0xFFD2432D)
    val ClosingSoft = Color(0xFFFDEAE6)

    // INFO (Primary 블루 재사용)
    val Info = Color(0xFF4F6EF0)
    val InfoSoft = Color(0xFFEAEEFE)

    // Text (ink)
    val Text = Color(0xFF241F1B)
    val Text2 = Color(0xFF8A8178)
    val Text3 = Color(0xFFB5AEA4)

    // Background (웜화이트 페이지 · 흰 카드 surface · 웜그레이)
    val Bg = Color(0xFFFAF7F2)
    val Bg2 = Color(0xFFFFFFFF)
    val Bg3 = Color(0xFFEFEAE1)

    // Border
    val Border = Color(0xFFECE6DD)
    val BorderDark = Color(0xFFDCD4C8)
}

// ACTIVE = 일반 진행중(NEW/UPDATE/CLOSING 아님). 메인 토글엔 안 뜨고 찾아보기·검색에 노출.
@Serializable
enum class JobKind { NEW, UPDATE, CLOSING, ACTIVE }

fun JobKind.color(): Color = when (this) {
    JobKind.NEW -> HiFiColors.New
    JobKind.UPDATE -> HiFiColors.Update
    JobKind.CLOSING -> HiFiColors.Closing
    JobKind.ACTIVE -> HiFiColors.Text2
}

fun JobKind.softColor(): Color = when (this) {
    JobKind.NEW -> HiFiColors.NewSoft
    JobKind.UPDATE -> HiFiColors.UpdateSoft
    JobKind.CLOSING -> HiFiColors.ClosingSoft
    JobKind.ACTIVE -> HiFiColors.Bg2
}

fun JobKind.shadowColor(): Color = when (this) {
    JobKind.NEW -> HiFiColors.NewShadow
    JobKind.UPDATE -> HiFiColors.UpdateShadow
    JobKind.CLOSING -> HiFiColors.ClosingShadow
    JobKind.ACTIVE -> HiFiColors.Text3
}

fun JobKind.label(): String = when (this) {
    JobKind.NEW -> "NEW"
    JobKind.UPDATE -> "변경"
    JobKind.CLOSING -> "마감임박"
    JobKind.ACTIVE -> "진행중"
}
```

---

## `ui/theme/Type.kt`

```kotlin
package com.jeongsi.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Pretendard fallback. 프로덕션에서는 res/font/pretendard_variable.ttf 추가 권장.
 * 시스템 sans-serif로 동작 보장.
 */
val PretendardFamily = FontFamily.SansSerif

/**
 * README 타이포 표 그대로 매핑.
 */
object HiFiType {
    val display = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.5).sp,
    )
    val title = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp,
    )
    val h2 = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
    )
    val body = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    )
    val body2 = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 19.sp,
    )
    val caption = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp,
    )
    val monoNum = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.ExtraBold,
        fontFeatureSettings = "tnum",
    )
}
```

---

## `ui/theme/Theme.kt`

```kotlin
package com.jeongsi.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

/**
 * Material3 ColorScheme의 일부 슬롯만 매핑 (브랜드/배경/텍스트 위주).
 * 컴포넌트는 거의 다 커스텀이라 Material 색은 백업용.
 */
private val HiFiColorScheme = lightColorScheme(
    primary = HiFiColors.Brand,
    onPrimary = HiFiColors.Bg,
    primaryContainer = HiFiColors.BrandSoft,
    onPrimaryContainer = HiFiColors.BrandDark,
    secondary = HiFiColors.New,
    background = HiFiColors.Bg,
    onBackground = HiFiColors.Text,
    surface = HiFiColors.Bg,
    onSurface = HiFiColors.Text,
    surfaceVariant = HiFiColors.Bg2,
    onSurfaceVariant = HiFiColors.Text2,
    outline = HiFiColors.Border,
    outlineVariant = HiFiColors.BorderDark,
    error = HiFiColors.Closing,
)

/**
 * 라운드 코너 토큰. README "Border Radius" 표.
 */
object HiFiShapes {
    val chip = RoundedCornerShape(999.dp)
    val buttonSm = RoundedCornerShape(14.dp)
    val button = RoundedCornerShape(16.dp)
    val card = RoundedCornerShape(18.dp)
    val cardSm = RoundedCornerShape(14.dp)
    val sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val phone = RoundedCornerShape(44.dp)
    val logo = RoundedCornerShape(14.dp)
    val logoSm = RoundedCornerShape(10.dp)
    val logoLg = RoundedCornerShape(18.dp)
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HiFiColorScheme,
    ) {
        CompositionLocalProvider(LocalContentColor provides HiFiColors.Text) {
            content()
        }
    }
}
```

---

## `ui/theme/Decor.kt`

```kotlin
package com.jeongsi.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 디자인 데코 헬퍼 (2026-06-28 "블루 고급화" 패스).
 * 그림자·그라데이션·알약배지를 토큰화 → 모든 화면에 일관 적용.
 */

/** 부드러운 카드 그림자. border만 쓰던 평면 카드에 입체감. clip=false라 그림자가 밖으로 번짐. */
fun Modifier.cardShadow(shape: Shape, elevation: Dp = 8.dp): Modifier =
    this.shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = Color(0x14101828),
        spotColor = Color(0x1A101828),
    )

/** 브랜드 블루 대각 그라데이션 (히어로 헤더용). */
fun brandGradient(): Brush = Brush.linearGradient(
    colors = listOf(Color(0xFF5B79F2), HiFiColors.BrandDark),
)

/** 작은 알약 배지 (D-day 등). 기본=반투명 흰 위 브랜드 글자. */
@Composable
fun Pill(
    text: String,
    bg: Color = Color.White,
    fg: Color = HiFiColors.BrandDark,
) {
    Box(
        Modifier.background(bg, RoundedCornerShape(999.dp)).padding(horizontal = 11.dp, vertical = 5.dp),
    ) {
        Text(text, style = HiFiType.caption, color = fg)
    }
}
```

---

## `ui/screens/ScreenCommon.kt`

```kotlin
package com.jeongsi.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T> StateFlow<T>.collectAsStateCompat(): State<T> = collectAsState()

/** 뒤로가기 상단바 (푸시 화면용). */
@Composable
fun AppTopBar(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clickableNoRipple(onBack),
            contentAlignment = Alignment.Center,
        ) {
            Text("‹", style = HiFiType.display, color = HiFiColors.Text)
        }
        Text(title, style = HiFiType.title, color = HiFiColors.Text)
    }
}

/** 화면 전체 가운데 메시지(로딩/에러용). */
@Composable
fun CenterText(text: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, style = HiFiType.body, color = HiFiColors.Text2, textAlign = TextAlign.Center)
    }
}

/** 세로 스크롤 + 표준 패딩 컬럼. */
@Composable
fun ScrollColumn(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        content = content,
    )
}

/** 섹션 제목(+ 선택적 우측 액션). */
@Composable
fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = HiFiType.title.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = HiFiColors.Text)
        Box(Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(action, style = HiFiType.caption, color = HiFiColors.Brand,
                modifier = Modifier.clickableNoRipple(onAction))
        }
    }
}

/** 리플 없는 클릭(카드·텍스트 버튼용). */
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    clickable(interactionSource = interaction, indication = null) { onClick() }
}
```

---

## `ui/screens/HomeScreen.kt`

```kotlin
package com.jeongsi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeongsi.app.data.Home
import com.jeongsi.app.data.Repository
import com.jeongsi.app.data.UnitCard
import com.jeongsi.app.ui.UiState
import com.jeongsi.app.ui.components.AdmissionBadge
import com.jeongsi.app.ui.components.GroupPickCard
import com.jeongsi.app.ui.components.HiFiButton
import com.jeongsi.app.ui.components.UnitCardView
import com.jeongsi.app.ui.components.UnivAvatar
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import com.jeongsi.app.ui.theme.Pill
import com.jeongsi.app.ui.theme.brandGradient
import com.jeongsi.app.ui.theme.cardShadow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<Home>>(UiState.Loading)
    val state: StateFlow<UiState<Home>> = _state

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            runCatching { Repository.home() }
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "불러오기 실패") }
        }
    }
}

@Composable
fun HomeScreen(
    onOpenScoreInput: () -> Unit,
    onOpenDiscover: () -> Unit,
    onOpenAnalysis: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenRecommend: () -> Unit,
    onOpenStrategy: () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    val state by vm.state.collectAsStateCompat()
    // 화면 들어올 때마다 갱신(군별 추천 변동 위해)
    val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(owner) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, e ->
            if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) vm.load()
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }

    when (val s = state) {
        is UiState.Loading -> CenterText("불러오는 중…")
        is UiState.Error -> CenterText("⚠ ${s.message}\n백엔드(8080) 실행 중인지 확인하세요.")
        is UiState.Success -> HomeContent(s.data, onOpenScoreInput, onOpenAnalysis, onOpenDetail, onOpenSearch, onOpenRecommend, onOpenStrategy)
    }
}

@Composable
private fun HomeContent(
    home: Home,
    onScore: () -> Unit,
    onAnalysis: () -> Unit,
    onDetail: (Long) -> Unit,
    onSearch: () -> Unit,
    onRecommend: () -> Unit,
    onStrategy: () -> Unit,
) {
    ScrollColumn {
        // 1. ★ 히어로 헤더 (그라데이션) — 타이틀 + D-day 알약 + 검색바
        Column(
            Modifier.fillMaxWidth()
                .cardShadow(RoundedCornerShape(24.dp), elevation = 14.dp)
                .background(brandGradient(), RoundedCornerShape(24.dp))
                .padding(20.dp),
        ) {
            Text("합격각", style = HiFiType.display, color = Color.White)
            Row(
                Modifier.padding(top = 4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("${home.admissionYear}학년도 수능", style = HiFiType.body, color = Color.White.copy(alpha = 0.85f))
                Pill("D-${home.dDay}")
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp)
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .clickableNoRipple(onSearch)
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text("🔍", style = HiFiType.body)
                Text("대학·학과 검색", style = HiFiType.body, color = HiFiColors.Text3,
                    modifier = Modifier.padding(start = 8.dp))
            }
        }
        Box(Modifier.padding(bottom = 14.dp)) {}

        // 2. 내 성적 요약
        Box(
            Modifier.fillMaxWidth()
                .cardShadow(RoundedCornerShape(18.dp), elevation = 6.dp)
                .background(HiFiColors.BrandSoft, RoundedCornerShape(18.dp))
                .padding(16.dp),
        ) {
            if (!home.hasScore || home.analysis == null) {
                Column {
                    Text("아직 성적이 없어요", style = HiFiType.title, color = HiFiColors.Text)
                    Text("성적을 입력하면 내 합격각을 보여드려요", style = HiFiType.caption, color = HiFiColors.Text2,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
                    HiFiButton("성적 입력하고 내 합격각 보기", onClick = onScore, fullWidth = true)
                }
            } else {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).clickableNoRipple(onAnalysis)) {
                        Text("내 성적", style = HiFiType.caption, color = HiFiColors.Text2)
                        Text("합격 분석 보기 ›", style = HiFiType.title.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Brand)
                    }
                    Text("성적 수정", style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text2,
                        modifier = Modifier.clickableNoRipple(onScore))
                }
            }
        }

        // 3. ★ HERO — 내 점수로 지원 가능한 학과 (더보기 → 라벨별 추천)
        SectionTitle("내 점수로 지원 가능한 학과", action = "더보기", onAction = onRecommend)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            home.heroUnits.take(6).forEach { card ->
                HeroCard(card, onClick = { onDetail(card.unitId) })
            }
        }

        // 4. 군별 추천 조합 (간소화 색상 카드, 더보기 → 전략)
        if (home.groupRecommend.isNotEmpty()) {
            SectionTitle("군별 추천 조합", action = "전략 더보기", onAction = onStrategy)
            home.groupRecommend.forEach { card ->
                Box(Modifier.padding(bottom = 10.dp)) {
                    GroupPickCard(card, onClick = { onDetail(card.unitId) })
                }
            }
        }

        // 5. 성적대별 인기 TOP5 (행 탭 → 상세)
        if (home.popularTop5.isNotEmpty()) {
            SectionTitle("성적대별 인기 학과 TOP5")
            home.popularTop5.forEach { p ->
                Row(
                    Modifier.fillMaxWidth().clickableNoRipple { onDetail(p.unitId) }.padding(vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text("${p.rank}", style = HiFiType.title.copy(fontWeight = FontWeight.Bold),
                        color = HiFiColors.Brand, modifier = Modifier.width(28.dp))
                    UnivAvatar(p.university.name, size = 36.dp)
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text("${p.university.name} ${p.departmentName}", style = HiFiType.body, color = HiFiColors.Text)
                        Text("${p.recruitGroupName} · 배치컷 ${p.cutPercentile}", style = HiFiType.caption, color = HiFiColors.Text2)
                    }
                    Text("모의지원 ${p.mockApplyCount}", style = HiFiType.caption, color = HiFiColors.Text2)
                }
            }
        }
    }
}

@Composable
private fun HeroCard(card: UnitCard, onClick: () -> Unit) {
    Column(
        Modifier.width(220.dp)
            .cardShadow(RoundedCornerShape(18.dp), elevation = 8.dp)
            .background(HiFiColors.Bg2, RoundedCornerShape(18.dp))
            .clickableNoRipple(onClick)
            .padding(16.dp),
    ) {
        AdmissionBadge(card)
        Text(card.university.name, style = HiFiType.caption, color = HiFiColors.Text2,
            modifier = Modifier.padding(top = 10.dp))
        Text(card.departmentName, style = HiFiType.title.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text)
        Text("${card.recruitGroupName} · 배치컷 ${card.admission.cutPercentile}",
            style = HiFiType.caption, color = HiFiColors.Text2, modifier = Modifier.padding(top = 6.dp))
    }
}

```

---

## `ui/components/UnitUi.kt`

```kotlin
package com.jeongsi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jeongsi.app.data.UnitCard
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType

/** 합격 5단계 색 (의미색: 안정 초록 / 적정 블루 / 소신 주황 / 상향 빨강 / 위험 진회색). */
fun labelColor(code: String?): Color = when (code) {
    "SAFE" -> HiFiColors.New
    "MODERATE" -> HiFiColors.Brand
    "REACH" -> HiFiColors.Update
    "HARD" -> HiFiColors.Closing
    "RISK" -> HiFiColors.Text
    else -> HiFiColors.Text3
}

fun labelSoftColor(code: String?): Color = when (code) {
    "SAFE" -> HiFiColors.NewSoft
    "MODERATE" -> HiFiColors.BrandSoft
    "REACH" -> HiFiColors.UpdateSoft
    "HARD" -> HiFiColors.ClosingSoft
    "RISK" -> HiFiColors.Bg3
    else -> HiFiColors.Bg3
}

/** 내 위치 ▲/▼ 텍스트 (환산−배치컷). null이면 빈 문자열. */
fun positionText(delta: Double?): String = when {
    delta == null -> ""
    delta >= 0 -> "▲${"%.1f".format(delta)}"
    else -> "▼${"%.1f".format(-delta)}"
}

/** 군 색상: 가=블루 / 나=초록 / 다=주황. */
fun groupColor(code: String): Color = when (code) {
    "GA" -> HiFiColors.Brand; "NA" -> HiFiColors.New; "DA" -> HiFiColors.Update; else -> HiFiColors.Text2
}

/** 군별 추천 간소화 카드 — 군 색상 배지(가/나/다) + 대학·학과 + 라벨·합격%. (배치컷 등 상세는 생략) */
@Composable
fun GroupPickCard(card: UnitCard, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Row(
        modifier.fillMaxWidth()
            .background(HiFiColors.Bg2, RoundedCornerShape(16.dp))
            .border(1.dp, HiFiColors.Border, RoundedCornerShape(16.dp))
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(52.dp).background(groupColor(card.recruitGroup), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(card.recruitGroupName.take(1), style = HiFiType.title.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(card.university.name, style = HiFiType.caption, color = HiFiColors.Text2)
            Text(card.departmentName, style = HiFiType.title.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text)
            Box(Modifier.padding(top = 6.dp)) { AdmissionBadge(card) }
        }
    }
}

/** 전략 조합 카드 — 가/나/다 3줄(각 줄 탭→상세) + 자세히(→조합상세). */
@Composable
fun ComboCard(
    combo: com.jeongsi.app.data.Combo,
    onOpenDetail: (Long) -> Unit,
    onOpenCombo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth()
            .background(HiFiColors.Bg2, RoundedCornerShape(16.dp))
            .border(1.dp, HiFiColors.Border, RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.background(HiFiColors.BrandSoft, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text("안정성 ${combo.stabilityScore}", style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Brand)
            }
            combo.probAtLeast1?.let {
                Text("· 1곳+ $it%", style = HiFiType.caption, color = HiFiColors.Text2, modifier = Modifier.padding(start = 8.dp))
            }
            Box(Modifier.weight(1f))
            Text("자세히 ›", style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Brand,
                modifier = Modifier.clickable { onOpenCombo() })
        }
        combo.picks.forEach { p ->
            Row(
                Modifier.fillMaxWidth().clickable { onOpenDetail(p.unitId) }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(34.dp).background(groupColor(p.recruitGroup), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text(p.recruitGroupName.take(1), style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = Color.White) }
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("${p.university.name} ${p.departmentName}", style = HiFiType.body.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text)
                }
                p.admission.labelName?.let {
                    Text(it, style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = labelColor(p.admission.labelCode))
                }
            }
        }
    }
}

/** 대학명 → 2글자 약칭 (서울대학교→서울, 한국외국어대학교→한국). */
fun univShort(name: String): String {
    val base = name.replace("대학교", "").replace("대학", "").replace("여자", "여")
    return base.take(2).ifEmpty { name.take(2) }
}

private val AVATAR_COLORS = listOf(
    Color(0xFF4F6EF0), Color(0xFFE89A4A), Color(0xFF1FA968), Color(0xFFF0533A),
    Color(0xFF7C5CFC), Color(0xFF2BB3C0), Color(0xFFD64B8A), Color(0xFF5B7083),
)

fun univColor(name: String): Color = AVATAR_COLORS[(name.hashCode().and(0x7FFFFFFF)) % AVATAR_COLORS.size]

/** 대학 로고 아바타 — 이니셜 사각(이미지3식). */
@Composable
fun UnivAvatar(name: String, size: androidx.compose.ui.unit.Dp = 44.dp) {
    Box(
        Modifier.size(size).background(univColor(name), RoundedCornerShape(size / 3)),
        contentAlignment = Alignment.Center,
    ) {
        Text(univShort(name), style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}

/** 합격 라벨 배지 (라벨 + 확률%). 성적 미입력 시 "성적 입력 필요". */
@Composable
fun AdmissionBadge(card: UnitCard, modifier: Modifier = Modifier) {
    val a = card.admission
    if (a.labelName == null) {
        Box(
            modifier
                .background(HiFiColors.Bg3, RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) { Text("성적 입력 필요", style = HiFiType.caption, color = HiFiColors.Text2) }
        return
    }
    val c = labelColor(a.labelCode)
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.background(labelSoftColor(a.labelCode), RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                buildString {
                    append(a.labelName)
                    val pos = positionText(a.positionDelta)
                    if (pos.isNotEmpty()) append("  $pos")
                },
                style = HiFiType.caption.copy(fontWeight = FontWeight.Bold),
                color = c,
            )
        }
        a.probability?.let { p ->
            Box(
                Modifier.padding(start = 6.dp).background(c, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("합격 $p%", style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = androidx.compose.ui.graphics.Color.White)
            }
        }
    }
}

/** 모집단위 카드 (검색·리포트·홈 미리보기 공용). */
@Composable
fun UnitCardView(
    card: UnitCard,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(HiFiColors.Bg2, RoundedCornerShape(18.dp))
            .border(1.dp, HiFiColors.Border, RoundedCornerShape(18.dp))
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UnivAvatar(card.university.name, size = 40.dp)
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(card.university.name, style = HiFiType.caption, color = HiFiColors.Text2)
                Text(
                    card.departmentName,
                    style = HiFiType.title.copy(fontWeight = FontWeight.Bold),
                    color = HiFiColors.Text,
                )
            }
            HiFiChip(text = card.recruitGroupName, small = true, variant = HiFiChipVariant.Outline)
        }
        Box(Modifier.padding(top = 8.dp)) { AdmissionBadge(card) }
        Row(
            Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("배치컷 ${card.admission.cutPercentile}", style = HiFiType.caption, color = HiFiColors.Text2)
            card.admission.convertedScore?.let { Text("내환산 $it", style = HiFiType.caption, color = HiFiColors.Brand) }
            card.competitionRate?.let { Text("경쟁률 ${it}:1", style = HiFiType.caption, color = HiFiColors.Text2) }
        }
        Row(
            Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("${card.reflectAreas} · ${card.indexName}", style = HiFiType.caption, color = HiFiColors.Text3)
            Text("수능 ${card.suneungRatio.toInt()}%", style = HiFiType.caption, color = HiFiColors.Text3)
            Text("모집 ${card.quota}명", style = HiFiType.caption, color = HiFiColors.Text3)
        }
        if (card.admission.eligible.not() && card.admission.eligibleReason != null) {
            Text(
                "⚠ ${card.admission.eligibleReason}",
                style = HiFiType.caption,
                color = HiFiColors.Closing,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        trailing?.let {
            Box(Modifier.padding(top = 10.dp)) { it() }
        }
    }
}
```

---

## `ui/components/HiFiButton.kt`

```kotlin
package com.jeongsi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType

enum class HiFiButtonVariant { Primary, Default, Green, Ghost }
enum class HiFiButtonSize { Sm, Md, Lg }

private data class ButtonStyle(
    val bg: Color,
    val fg: Color,
    val shadow: Color,
    val padH: Dp,
    val padV: Dp,
    val textStyle: TextStyle,
    val shadowDp: Dp,
    val pressedDp: Dp,
    val cornerDp: Dp,
)

private fun styleFor(variant: HiFiButtonVariant, size: HiFiButtonSize): ButtonStyle {
    val (bg, fg, shadow) = when (variant) {
        HiFiButtonVariant.Primary -> Triple(HiFiColors.Brand, Color.White, HiFiColors.BrandShadow)
        HiFiButtonVariant.Green -> Triple(HiFiColors.New, Color.White, HiFiColors.NewShadow)
        HiFiButtonVariant.Default -> Triple(HiFiColors.Bg2, HiFiColors.Text, HiFiColors.BorderDark)
        HiFiButtonVariant.Ghost -> Triple(Color.Transparent, HiFiColors.Text2, Color.Transparent)
    }
    return when (size) {
        HiFiButtonSize.Sm -> ButtonStyle(bg, fg, shadow, 10.dp, 9.dp, HiFiType.body2.copy(fontSize = 15.sp), 3.dp, 1.dp, 14.dp)
        HiFiButtonSize.Md -> ButtonStyle(bg, fg, shadow, 22.dp, 14.dp, HiFiType.body, 4.dp, 2.dp, 16.dp)
        HiFiButtonSize.Lg -> ButtonStyle(bg, fg, shadow, 26.dp, 18.dp, HiFiType.h2, 4.dp, 2.dp, 16.dp)
    }
}

/**
 * 듀오링고풍 3D 버튼.
 * 그림자 박스(아래)와 본체 박스(위)를 겹쳐서 그린 다음, 누르면 본체만 내려가서
 * 보이는 그림자 두께가 줄어드는 것처럼 보이게 처리.
 */
@Composable
fun HiFiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: HiFiButtonVariant = HiFiButtonVariant.Default,
    size: HiFiButtonSize = HiFiButtonSize.Md,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
) {
    val s = styleFor(variant, size)
    val shape = RoundedCornerShape(s.cornerDp)
    val widthMod = if (fullWidth) Modifier.fillMaxWidth() else Modifier

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val isPressed = pressed && enabled
    val bodyOffset: Dp = if (isPressed) (s.shadowDp - s.pressedDp) else 0.dp

    Box(modifier = modifier.then(widthMod).padding(bottom = s.shadowDp)) {
        // 그림자 (Ghost variant는 투명이라 안 보임)
        if (variant != HiFiButtonVariant.Ghost) {
            Box(
                Modifier
                    .matchParentSize()
                    .offset(y = s.shadowDp)
                    .clip(shape)
                    .background(s.shadow)
            )
        }
        // 본체 — fullWidth일 땐 본체도 fillMaxWidth로 펼쳐서 텍스트가 진짜 가운데에 오게 함.
        // 안 그러면 본체가 wrap content라 outer Box 안에서 좌측에 정렬돼 텍스트도 좌측에 치우쳐 보임.
        val bodyWidthMod = if (fullWidth) Modifier.fillMaxWidth() else Modifier
        Box(
            modifier = Modifier
                .then(bodyWidthMod)
                .offset(y = bodyOffset)
                .clip(shape)
                .background(s.bg)
                .clickable(
                    interactionSource = interaction,
                    indication = ripple(color = HiFiColors.BorderDark),
                    enabled = enabled,
                    onClick = onClick,
                )
                .padding(horizontal = s.padH, vertical = s.padV)
                .alpha(if (enabled) 1f else 0.4f)
        ) {
            Text(
                text = text,
                style = s.textStyle,
                color = s.fg,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
```

---

## `ui/components/HiFiChip.kt`

```kotlin
package com.jeongsi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType

enum class HiFiChipVariant { Solid, Outline }

@Composable
fun HiFiChip(
    text: String,
    selected: Boolean = false,
    variant: HiFiChipVariant = HiFiChipVariant.Solid,
    small: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val bg: Color
    val fg: Color
    val border: Color
    when (variant) {
        HiFiChipVariant.Solid -> {
            bg = if (selected) HiFiColors.Brand else HiFiColors.Bg2
            fg = if (selected) Color.White else HiFiColors.Text
            border = Color.Transparent
        }
        HiFiChipVariant.Outline -> {
            bg = if (selected) HiFiColors.BrandSoft else HiFiColors.Bg
            fg = if (selected) HiFiColors.BrandDark else HiFiColors.Text
            border = if (selected) HiFiColors.Brand else HiFiColors.Border
        }
    }
    val padH: Dp = if (small) 10.dp else 14.dp
    val padV: Dp = if (small) 5.dp else 7.dp
    val fontSize = if (small) 12.sp else 14.sp

    var m = modifier
        .clip(CircleShape)
        .background(bg)
    if (border != Color.Transparent) {
        m = m.border(2.dp, border, CircleShape)
    }
    if (onClick != null) {
        m = m.clickable(onClick = onClick)
    }
    Box(m.padding(horizontal = padH, vertical = padV)) {
        Text(
            text = text,
            style = HiFiType.body.copy(fontSize = fontSize),
            color = fg,
        )
    }
}

/**
 * 공고 라벨 (NEW/UPDATE/CLOSING) - 색깔만 다른 작은 칩.
 */
@Composable
fun HiFiLabel(text: String, bg: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = HiFiType.caption.copy(fontSize = 11.sp, letterSpacing = 0.6.sp),
            color = Color.White,
        )
    }
}
```

