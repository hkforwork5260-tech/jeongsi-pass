package com.jeongsi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.jeongsi.app.data.CompareModel
import com.jeongsi.app.data.Repository
import com.jeongsi.app.ui.UiState
import com.jeongsi.app.ui.components.UnitCardView
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CompareViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<CompareModel>>(UiState.Loading)
    val state: StateFlow<UiState<CompareModel>> = _state
    fun load(group: String) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            runCatching { Repository.compare(group) }
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "불러오기 실패") }
        }
    }
}

@Composable
fun CompareScreen(group: String, onBack: () -> Unit, onOpenDetail: (Long) -> Unit, vm: CompareViewModel = viewModel()) {
    val state by vm.state.collectAsStateCompat()
    LaunchedEffect(group) { vm.load(group) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("비교분석", onBack)
        when (val s = state) {
            is UiState.Loading -> CenterText("불러오는 중…")
            is UiState.Error -> CenterText("⚠ ${s.message}")
            is UiState.Success -> {
                val c = s.data
                ScrollColumn {
                    Text("${c.groupName} 비교분석", style = HiFiType.display.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text)
                    Box(
                        Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp)
                            .background(HiFiColors.NewSoft, RoundedCornerShape(14.dp)).padding(14.dp),
                    ) { Text(c.summary, style = HiFiType.body, color = HiFiColors.Text) }

                    c.units.forEachIndexed { i, card ->
                        Box(Modifier.padding(bottom = 10.dp)) {
                            UnitCardView(card, onClick = { onOpenDetail(card.unitId) }) {
                                val tag = if (i == 0) "↑ 가장 안정적" else "${i + 1}순위"
                                Text(tag, style = HiFiType.caption.copy(fontWeight = FontWeight.Bold),
                                    color = if (i == 0) HiFiColors.New else HiFiColors.Text2)
                            }
                        }
                    }
                }
            }
        }
    }
}
