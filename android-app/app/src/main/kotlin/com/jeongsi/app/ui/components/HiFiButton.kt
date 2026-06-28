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
