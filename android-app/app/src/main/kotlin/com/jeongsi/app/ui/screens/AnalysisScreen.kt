package com.jeongsi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeongsi.app.data.AnalysisModel
import com.jeongsi.app.data.ReflectTypeRow
import com.jeongsi.app.data.Repository
import com.jeongsi.app.ui.UiState
import com.jeongsi.app.ui.components.HiFiChip
import com.jeongsi.app.ui.components.HiFiChipVariant
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnalysisViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<AnalysisModel>>(UiState.Loading)
    val state: StateFlow<UiState<AnalysisModel>> = _state
    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            runCatching { Repository.analysis() }
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "불러오기 실패") }
        }
    }
}

@Composable
fun AnalysisScreen(onBack: () -> Unit, vm: AnalysisViewModel = viewModel()) {
    val state by vm.state.collectAsStateCompat()
    LaunchedEffect(Unit) { vm.load() }
    androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
        AppTopBar("성적 분석", onBack)
        when (val s = state) {
            is UiState.Loading -> CenterText("분석 중…")
            is UiState.Error -> CenterText("⚠ ${s.message}")
            is UiState.Success ->
                if (!s.data.hasScore) CenterText("성적을 입력하면 분석이 제공됩니다.")
                else AnalysisContent(s.data)
        }
    }
}

@Composable
private fun AnalysisContent(a: AnalysisModel) {
    ScrollColumn {
        Text("성적 분석", style = HiFiType.display.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text)

        // 요약
        Box(
            Modifier.fillMaxWidth().padding(top = 10.dp)
                .background(HiFiColors.BrandSoft, RoundedCornerShape(14.dp)).padding(14.dp),
        ) {
            Column {
                Text("국·수·탐 평균 백분위 ${a.averagePct ?: "-"} · 전국 상위 ${a.nationalTopPercent ?: "-"}%",
                    style = HiFiType.title.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text)
                Text(a.comment, style = HiFiType.caption, color = HiFiColors.Text2, modifier = Modifier.padding(top = 4.dp))
            }
        }

        // 유불리 분석
        SectionTitle("반영영역별 유불리")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FavorBox("가장 유리 TOP3", a.bestTypes, HiFiColors.New, Modifier.weight(1f))
            FavorBox("가장 불리 TOP3", a.worstTypes, HiFiColors.Closing, Modifier.weight(1f))
        }

        // 반영유형별 상위누적% 표
        SectionTitle("반영유형별 상위누적%")
        Text("대학마다 반영 영역이 달라, 유형별 내 총점·전국 상위누적%가 달라집니다.",
            style = HiFiType.caption, color = HiFiColors.Text2, modifier = Modifier.padding(bottom = 8.dp))
        Box(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            Column {
                TableHeader()
                a.reflectTypes.forEach { RowItem(it) }
            }
        }
    }
}

@Composable
private fun FavorBox(title: String, items: List<String>, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Column(
        modifier.background(HiFiColors.Bg2, RoundedCornerShape(14.dp)).padding(12.dp),
    ) {
        Text(title, style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = color)
        items.forEachIndexed { i, t ->
            Text("${i + 1}. $t", style = HiFiType.caption, color = HiFiColors.Text, modifier = Modifier.padding(top = 4.dp))
        }
        if (items.isEmpty()) Text("-", style = HiFiType.caption, color = HiFiColors.Text3, modifier = Modifier.padding(top = 4.dp))
    }
}

private val W = listOf(108.dp, 70.dp, 90.dp, 70.dp, 90.dp, 56.dp)

@Composable
private fun TableHeader() {
    Row(
        Modifier.background(HiFiColors.Bg3, RoundedCornerShape(8.dp)).padding(vertical = 8.dp, horizontal = 4.dp),
    ) {
        listOf("반영유형", "표준총점", "표준 상위%", "백분위", "백분위 상위%", "대학").forEachIndexed { i, h ->
            Text(h, style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text2,
                modifier = Modifier.width(W[i]))
        }
    }
}

@Composable
private fun RowItem(r: ReflectTypeRow) {
    val dim = !r.eligible
    val col = if (dim) HiFiColors.Text3 else HiFiColors.Text
    Row(Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
        Column(Modifier.width(W[0])) {
            Text("${r.reflectType}(탐${r.inquiryCount})", style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = col)
            if (dim) Text("지원불가", style = HiFiType.caption, color = HiFiColors.Closing)
        }
        Cell(r.stdTotal?.toString() ?: "-", col, W[1])
        Cell(r.stdTopPercent?.let { "$it%" } ?: "-", if (dim) col else HiFiColors.Brand, W[2])
        Cell(r.pctTotal?.toString() ?: "-", col, W[3])
        Cell(r.pctTopPercent?.let { "$it%" } ?: "-", if (dim) col else HiFiColors.Brand, W[4])
        Cell("${r.univCount}개", col, W[5])
    }
}

@Composable
private fun Cell(text: String, color: androidx.compose.ui.graphics.Color, width: androidx.compose.ui.unit.Dp) {
    Text(text, style = HiFiType.caption, color = color, modifier = Modifier.width(width))
}
