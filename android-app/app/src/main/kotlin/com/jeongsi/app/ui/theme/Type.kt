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
