package com.jeongsi.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeongsi.app.data.CutoffTrend
import com.jeongsi.app.data.RegionRank
import com.jeongsi.app.data.Repository
import com.jeongsi.app.data.UnitCard
import com.jeongsi.app.data.UnitDetail
import com.jeongsi.app.ui.UiState
import com.jeongsi.app.ui.components.AdmissionBadge
import com.jeongsi.app.ui.components.HiFiButton
import com.jeongsi.app.ui.components.HiFiButtonSize
import com.jeongsi.app.ui.components.HiFiButtonVariant
import com.jeongsi.app.ui.components.HiFiChip
import com.jeongsi.app.ui.components.HiFiChipVariant
import com.jeongsi.app.ui.components.UnivAvatar
import com.jeongsi.app.ui.components.labelColor
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import com.jeongsi.app.ui.theme.cardShadow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UnitDetailViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<UnitDetail>>(UiState.Loading)
    val state: StateFlow<UiState<UnitDetail>> = _state

    // 윈도우 함수 분석 (본문과 독립 — 실패해도 화면엔 영향 없음)
    private val _trend = MutableStateFlow<List<CutoffTrend>>(emptyList())
    val trend: StateFlow<List<CutoffTrend>> = _trend
    private val _rank = MutableStateFlow<RegionRank?>(null)
    val rank: StateFlow<RegionRank?> = _rank

    fun load(id: Long) {
        _state.value = UiState.Loading
        _trend.value = emptyList(); _rank.value = null
        viewModelScope.launch {
            runCatching { Repository.unitDetail(id) }
                .onSuccess { detail ->
                    _state.value = UiState.Success(detail)
                    launch { runCatching { Repository.cutoffTrend(id) }.onSuccess { _trend.value = it } }
                    launch {
                        val c = detail.card
                        runCatching { Repository.regionRank(c.university.region, c.track) }
                            .onSuccess { list -> _rank.value = list.firstOrNull { it.unitId == id } }
                    }
                }
                .onFailure { _state.value = UiState.Error(it.message ?: "불러오기 실패") }
        }
    }

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast
    fun clearToast() { _toast.value = null }

    fun toggleMock(card: UnitCard) {
        viewModelScope.launch {
            if (card.isMockApplied) {
                runCatching { Repository.removeMock(card.unitId) }
                patch { it.copy(card = it.card.copy(isMockApplied = false)) }
            } else {
                val err = runCatching { Repository.addMock(card.unitId) }.getOrNull()
                if (err == null) patch { it.copy(card = it.card.copy(isMockApplied = true)) } else _toast.value = err
            }
        }
    }

    private fun patch(f: (UnitDetail) -> UnitDetail) {
        val s = _state.value
        if (s is UiState.Success) _state.value = UiState.Success(f(s.data))
    }
}

@Composable
fun UnitDetailScreen(unitId: Long, onBack: () -> Unit, vm: UnitDetailViewModel = viewModel()) {
    val state by vm.state.collectAsStateCompat()
    val toast by vm.toast.collectAsStateCompat()
    val tctx = LocalContext.current
    LaunchedEffect(unitId) { vm.load(unitId) }
    LaunchedEffect(toast) { toast?.let { android.widget.Toast.makeText(tctx, it, android.widget.Toast.LENGTH_SHORT).show(); vm.clearToast() } }
    Column(Modifier.fillMaxWidth()) {
        AppTopBar("모집 요강", onBack)
        when (val s = state) {
            is UiState.Loading -> CenterText("불러오는 중…")
            is UiState.Error -> CenterText("⚠ ${s.message}")
            is UiState.Success -> DetailContent(s.data, vm)
        }
    }
}

@Composable
private fun DetailContent(d: UnitDetail, vm: UnitDetailViewModel) {
    val card = d.card
    val ctx = LocalContext.current
    val trend by vm.trend.collectAsStateCompat()
    val rank by vm.rank.collectAsStateCompat()
    ScrollColumn {
        // 헤더: 아바타 + 대학 + 학과 / 우측에 군 크게
        Row(verticalAlignment = Alignment.CenterVertically) {
            UnivAvatar(card.university.name, size = 56.dp)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("${card.university.name} · ${card.university.region}", style = HiFiType.caption, color = HiFiColors.Text2)
                Text(card.departmentName, style = HiFiType.title.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text)
            }
            Box(
                Modifier.background(HiFiColors.Brand, RoundedCornerShape(16.dp)).padding(horizontal = 18.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(card.recruitGroupName.take(1), style = HiFiType.display.copy(fontWeight = FontWeight.Bold), color = androidx.compose.ui.graphics.Color.White)
            }
        }
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HiFiChip(card.trackName, small = true, variant = HiFiChipVariant.Outline)
            HiFiChip(card.admissionType, small = true, variant = HiFiChipVariant.Outline)
        }

        // 합격 판정
        Box(Modifier.padding(top = 16.dp)) { AdmissionBadge(card) }
        card.admission.convertedScore?.let {
            Text("내 환산 $it  ·  배치컷 ${card.admission.cutPercentile}", style = HiFiType.body,
                color = HiFiColors.Text2, modifier = Modifier.padding(top = 6.dp))
        }
        if (!card.admission.eligible && card.admission.eligibleReason != null) {
            Text("⚠ ${card.admission.eligibleReason}", style = HiFiType.caption, color = HiFiColors.Closing,
                modifier = Modifier.padding(top = 6.dp))
        }

        // 스탯 카드 (이미지3 식)
        Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("배치컷", card.admission.cutPercentile.toString(), Modifier.weight(1f))
            StatCard("경쟁률", card.competitionRate?.let { "$it:1" } ?: "-", Modifier.weight(1f))
            StatCard("모집", "${card.quota}명", Modifier.weight(1f))
        }

        // 지역 내 순위 (윈도우 함수 RANK / PERCENT_RANK)
        rank?.let { r ->
            Box(
                Modifier.padding(top = 10.dp).fillMaxWidth()
                    .background(HiFiColors.BrandSoft, RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    "📍 ${r.region} ${card.trackName}계열 배치컷  ${r.regionRank}위 / ${r.regionTotal}곳  ·  상위 ${r.topPercent}%",
                    style = HiFiType.body.copy(fontWeight = FontWeight.Bold), color = HiFiColors.BrandDark,
                )
            }
        }

        // 연도별 배치컷 추이 (윈도우 함수 LAG)
        if (trend.isNotEmpty()) {
            SectionTitle("연도별 배치컷 추이")
            trend.forEach { TrendRow(it) }
        }

        // 전형요소별 반영 비율
        SectionTitle("전형요소별 반영 비율")
        InfoLine("반영 영역", "${card.reflectAreas} · ${card.indexName}")
        InfoLine("수능", "${card.suneungRatio.toInt()}%")
        if (card.naesinRatio > 0) InfoLine("내신", "${card.naesinRatio.toInt()}%")
        InfoLine("국·수·영·탐 비율", "${d.weightKorean.toInt()} · ${d.weightMath.toInt()} · ${d.weightEnglish.toInt()} · ${d.weightInquiry.toInt()}")
        if (d.mathRequired == "calculus_geometry") InfoLine("수학 지정", "미적분·기하")
        if (d.inquiryRequired == "science") InfoLine("탐구 지정", "과학탐구")

        // 경쟁자 지표
        SectionTitle("지원자 지표")
        InfoLine("모의지원자 평균", card.applicantAvg?.toString() ?: "-")
        InfoLine("목표등록자 평균", card.targetAvg?.toString() ?: "-")

        // 지원 자격 (더미)
        SectionTitle("지원 자격")
        Text("고등학교 졸업(예정)자 또는 동등 이상의 학력이 있다고 인정되는 자", style = HiFiType.body, color = HiFiColors.Text)
        Text("최저학력 기준: 없음", style = HiFiType.caption, color = HiFiColors.Text2, modifier = Modifier.padding(top = 4.dp))
        Text(d.intro, style = HiFiType.caption, color = HiFiColors.Text3, modifier = Modifier.padding(top = 6.dp))

        // 액션 — 모의지원
        Box(Modifier.padding(top = 20.dp, bottom = 28.dp).fillMaxWidth()) {
            HiFiButton(if (card.isMockApplied) "모의지원 완료 (취소하려면 탭)" else "모의지원", onClick = { vm.toggleMock(card) },
                variant = if (card.isMockApplied) HiFiButtonVariant.Ghost else HiFiButtonVariant.Primary,
                fullWidth = true)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Column(
        modifier.cardShadow(RoundedCornerShape(14.dp), elevation = 5.dp)
            .background(HiFiColors.Bg2, RoundedCornerShape(14.dp)).padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = HiFiType.caption, color = HiFiColors.Text2)
        Text(value, style = HiFiType.title.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun TrendRow(t: CutoffTrend) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("${t.year}년", style = HiFiType.body, color = HiFiColors.Text2)
        Box(Modifier.weight(1f))
        Text("컷 ${t.cutPercentile}", style = HiFiType.body.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text)
        t.yoyChange?.let { ch ->
            val up = ch >= 0
            Text(
                "  ${if (up) "▲" else "▼"}${kotlin.math.abs(ch)}",
                style = HiFiType.caption.copy(fontWeight = FontWeight.Bold),
                color = if (up) HiFiColors.Closing else HiFiColors.New,
                modifier = Modifier.padding(start = 6.dp),
            )
        } ?: Text("  기준연도", style = HiFiType.caption, color = HiFiColors.Text3, modifier = Modifier.padding(start = 6.dp))
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(label, style = HiFiType.body, color = HiFiColors.Text2)
        Box(Modifier.weight(1f))
        Text(value, style = HiFiType.body.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text)
    }
}
