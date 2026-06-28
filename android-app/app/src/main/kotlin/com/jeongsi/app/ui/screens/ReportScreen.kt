package com.jeongsi.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeongsi.app.data.Report
import com.jeongsi.app.data.Repository
import com.jeongsi.app.ui.UiState
import com.jeongsi.app.ui.components.UnitCardView
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<Report>>(UiState.Loading)
    val state: StateFlow<UiState<Report>> = _state

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            runCatching { Repository.report() }
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "불러오기 실패") }
        }
    }

    fun setTarget(unitId: Long) {
        viewModelScope.launch { runCatching { Repository.setTarget(unitId, 1) } }
    }
}

@Composable
fun ReportScreen(onCompare: (String) -> Unit, onOpenDetail: (Long) -> Unit, vm: ReportViewModel = viewModel()) {
    val state by vm.state.collectAsStateCompat()
    LaunchedEffect(Unit) { vm.load() }

    when (val s = state) {
        is UiState.Loading -> CenterText("불러오는 중…")
        is UiState.Error -> CenterText("⚠ ${s.message}")
        is UiState.Success -> ReportContent(s.data, onCompare, onOpenDetail) { vm.setTarget(it) }
    }
}

@Composable
private fun ReportContent(report: Report, onCompare: (String) -> Unit, onOpenDetail: (Long) -> Unit, onSetTarget: (Long) -> Unit) {
    ScrollColumn {
        Text("합격예측 리포트", style = HiFiType.display.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text)
        Box(
            Modifier.fillMaxWidth().padding(top = 10.dp)
                .background(HiFiColors.BrandSoft, RoundedCornerShape(14.dp)).padding(14.dp),
        ) {
            Text(report.summary, style = HiFiType.body, color = HiFiColors.Text)
        }

        if (report.groups.isEmpty()) {
            Box(Modifier.padding(top = 24.dp)) {
                Text("모의지원을 담으면 군별 조합과 합격 가능성을 보여드립니다.", style = HiFiType.body, color = HiFiColors.Text2)
            }
            return@ScrollColumn
        }
        report.groups.forEach { g ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                SectionTitle("${g.groupName}  (${g.units.size})")
                if (g.units.size >= 2) {
                    Text("비교분석 ›", style = HiFiType.caption, color = HiFiColors.Brand,
                        modifier = Modifier.clickableNoRipple { onCompare(g.group) })
                }
            }
            g.units.forEach { card ->
                Box(Modifier.padding(bottom = 10.dp)) {
                    UnitCardView(card, onClick = { onOpenDetail(card.unitId) }) {
                        com.jeongsi.app.ui.components.HiFiButton(
                            "목표 대학으로 설정",
                            onClick = { onSetTarget(card.unitId) },
                            variant = com.jeongsi.app.ui.components.HiFiButtonVariant.Ghost,
                            size = com.jeongsi.app.ui.components.HiFiButtonSize.Sm,
                        )
                    }
                }
            }
        }
    }
}
