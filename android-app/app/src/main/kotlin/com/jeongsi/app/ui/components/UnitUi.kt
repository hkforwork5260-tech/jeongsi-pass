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
import com.jeongsi.app.ui.theme.cardShadow

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
            .cardShadow(RoundedCornerShape(16.dp), elevation = 6.dp)
            .background(HiFiColors.Bg2, RoundedCornerShape(16.dp))
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
            .cardShadow(RoundedCornerShape(16.dp), elevation = 6.dp)
            .background(HiFiColors.Bg2, RoundedCornerShape(16.dp))
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
            .cardShadow(RoundedCornerShape(18.dp), elevation = 7.dp)
            .background(HiFiColors.Bg2, RoundedCornerShape(18.dp))
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
