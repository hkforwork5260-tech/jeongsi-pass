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
