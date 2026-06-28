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
