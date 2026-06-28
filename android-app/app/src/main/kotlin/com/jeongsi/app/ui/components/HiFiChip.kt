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
