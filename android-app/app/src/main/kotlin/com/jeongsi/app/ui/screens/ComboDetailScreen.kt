package com.jeongsi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeongsi.app.data.Repository
import com.jeongsi.app.data.UnitCard
import com.jeongsi.app.ui.UiState
import com.jeongsi.app.ui.components.GroupPickCard
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ComboDetailViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<UnitCard>>>(UiState.Loading)
    val state: StateFlow<UiState<List<UnitCard>>> = _state
    fun load(ids: List<Long>) {
        viewModelScope.launch {
            runCatching { ids.map { Repository.unitDetail(it).card } }
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "불러오기 실패") }
        }
    }
}

@Composable
fun ComboDetailScreen(ids: List<Long>, onBack: () -> Unit, onOpenDetail: (Long) -> Unit, vm: ComboDetailViewModel = viewModel()) {
    val state by vm.state.collectAsStateCompat()
    LaunchedEffect(ids) { vm.load(ids) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("조합 상세", onBack)
        when (val s = state) {
            is UiState.Loading -> CenterText("불러오는 중…")
            is UiState.Error -> CenterText("⚠ ${s.message}")
            is UiState.Success -> {
                val cards = s.data
                val probs = cards.mapNotNull { it.admission.probability?.let { p -> p / 100.0 } }
                val pNone = probs.fold(1.0) { a, p -> a * (1 - p) }
                val p1 = if (probs.isEmpty()) null else ((1 - pNone) * 100).roundToInt()
                ScrollColumn {
                    Box(
                        Modifier.fillMaxWidth().background(HiFiColors.BrandSoft, RoundedCornerShape(16.dp)).padding(16.dp),
                    ) {
                        Column {
                            Text("이 조합", style = HiFiType.caption, color = HiFiColors.Text2)
                            Text("한 곳 이상 합격 ${p1 ?: "-"}%", style = HiFiType.display.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Brand)
                            val comp = cards.mapNotNull { it.admission.labelName }.groupingBy { it }.eachCount()
                                .entries.joinToString(" · ") { "${it.key} ${it.value}개" }
                            if (comp.isNotBlank()) Text("구성: $comp", style = HiFiType.caption, color = HiFiColors.Text2, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                    cards.forEach { card ->
                        Box(Modifier.padding(top = 12.dp)) {
                            GroupPickCard(card, onClick = { onOpenDetail(card.unitId) })
                        }
                    }
                    Box(Modifier.padding(16.dp)) {}
                }
            }
        }
    }
}
