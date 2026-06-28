package com.jeongsi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.jeongsi.app.ui.theme.HiFiColors

/**
 * 온보딩 1~4 진행 표시기. activeIndex 위치는 길쭉한 코랄 바, 나머지는 회색 점.
 */
@Composable
fun OnboardingDots(
    total: Int,
    activeIndex: Int,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        repeat(total) { i ->
            val active = i == activeIndex
            val done = i < activeIndex
            val w = if (active) 24.dp else 8.dp
            Box(
                Modifier
                    .height(8.dp)
                    .width(w)
                    .clip(if (active) RoundedCornerShape(4.dp) else CircleShape)
                    .background(
                        when {
                            active -> HiFiColors.Brand
                            done -> HiFiColors.Brand
                            else -> HiFiColors.Bg3
                        }
                    )
            )
            if (i < total - 1) Spacer(Modifier.width(6.dp))
        }
    }
}
