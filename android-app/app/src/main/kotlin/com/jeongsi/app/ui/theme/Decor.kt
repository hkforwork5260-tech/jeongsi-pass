package com.jeongsi.app.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jeongsi.app.R

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

/**
 * 메가스터디 벤치마킹 배지 — 흰 카드 안에 실제 로고. 흰배경 로고가 흰 카드에 매끈하게 블렌딩.
 * @param logoHeight 로고 높이 / @param caption "합격예측 벤치마킹" 캡션 표시 여부
 */
@Composable
fun MegastudyBadge(
    modifier: Modifier = Modifier,
    logoHeight: Dp = 22.dp,
    caption: Boolean = true,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (caption) Text("합격예측 벤치마킹", style = HiFiType.caption, color = HiFiColors.Text3)
        Box(
            Modifier.padding(top = if (caption) 8.dp else 0.dp)
                .cardShadow(RoundedCornerShape(14.dp), elevation = 4.dp)
                .background(Color.White, RoundedCornerShape(14.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.megastudy_wordmark),
                contentDescription = "megastudy",
                modifier = Modifier.height(logoHeight),
            )
        }
    }
}

/** 메가스터디 가로 배너 — 흰 카드에 워드마크를 가로 꽉차게(Crop). 홈/모의지원 하단용. */
@Composable
fun MegastudyBanner(modifier: Modifier = Modifier, height: Dp = 86.dp) {
    Box(
        modifier.fillMaxWidth()
            .cardShadow(RoundedCornerShape(16.dp), elevation = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .height(height),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.megastudy_wordmark),
            contentDescription = "megastudy",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop,
        )
    }
}

/** 메가스터디 M 아이콘 (글자 없음, 그라데이션 사각). */
@Composable
fun MegastudyIcon(modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Image(
        painter = painterResource(R.drawable.megastudy_icon),
        contentDescription = "megastudy",
        modifier = modifier.height(size),
    )
}

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
