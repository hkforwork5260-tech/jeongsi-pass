package com.jeongsi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.jeongsi.app.ui.components.HiFiChip
import com.jeongsi.app.ui.components.UnitCardView
import com.jeongsi.app.ui.components.labelColor
import com.jeongsi.app.ui.components.labelSoftColor
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecommendViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<Map<String, List<UnitCard>>>>(UiState.Loading)
    val state: StateFlow<UiState<Map<String, List<UnitCard>>>> = _state
    fun load() {
        viewModelScope.launch {
            runCatching { Repository.recommend() }
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "불러오기 실패") }
        }
    }
}

private val LABEL_ORDER = listOf("SAFE" to "안정", "MODERATE" to "적정", "REACH" to "소신", "HARD" to "상향", "RISK" to "위험")

/** 내 점수로 지원 가능한 학과 — 합격 라벨별 접기/펼치기 + 계열·군 필터. */
@Composable
fun RecommendScreen(onBack: () -> Unit, onOpenDetail: (Long) -> Unit, vm: RecommendViewModel = viewModel()) {
    val state by vm.state.collectAsStateCompat()
    var track by remember { mutableStateOf<String?>(null) }
    var group by remember { mutableStateOf<String?>(null) }
    var region by remember { mutableStateOf<String?>(null) }
    var university by remember { mutableStateOf<String?>(null) }
    var department by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf("SAFE") }   // 펼친 라벨(하나만)
    LaunchedEffect(Unit) { vm.load() }

    Column(Modifier.fillMaxSize()) {
        AppTopBar("지원 가능 학과", onBack)
        when (val s = state) {
            is UiState.Loading -> CenterText("불러오는 중…")
            is UiState.Error -> CenterText("⚠ ${s.message}")
            is UiState.Success -> {
                fun filtered(list: List<UnitCard>) = list
                    .filter { track == null || it.track == track }
                    .filter { group == null || it.recruitGroup == group }
                    .filter { region == null || it.university.region == region }
                    .filter { university == null || it.university.name == university }
                    .filter { department == null || it.departmentName == department }

                if (s.data.values.all { it.isEmpty() }) { CenterText("성적을 입력하면 합격 라벨별로 정리해 드려요."); return@Column }

                val allCards = s.data.values.flatten()
                val univOptions = allCards.map { it.university.name }.distinct().sorted().map { it to it.replace("대학교", "대") }
                val deptOptions = allCards.map { it.departmentName }.distinct().sorted().map { it to it }

                ScrollColumn {
                    // 필터
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        HiFiChip("전체계열", selected = track == null, small = true, onClick = { track = null })
                        HiFiChip("인문", selected = track == "humanities", small = true, onClick = { track = "humanities" })
                        HiFiChip("자연", selected = track == "natural", small = true, onClick = { track = "natural" })
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        HiFiChip("전체군", selected = group == null, small = true, onClick = { group = null })
                        listOf("GA" to "가군", "NA" to "나군", "DA" to "다군").forEach { (c, n) ->
                            HiFiChip(n, selected = group == c, small = true, onClick = { group = c })
                        }
                    }
                    // 지역·학교·학과 필터 (검색탭과 동일)
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterDropdown("지역", region, REGIONS.map { it to it }) { region = it }
                        FilterDropdown("학교", university, univOptions) { university = it }
                        FilterDropdown("학과", department, deptOptions, columns = 2) { department = it }
                    }

                    // 라벨 헤더(접기/펼치기)
                    LABEL_ORDER.forEach { (code, name) ->
                        val list = filtered(s.data[code].orEmpty())
                        val open = expanded == code
                        Row(
                            Modifier.fillMaxWidth().padding(top = 14.dp)
                                .background(labelSoftColor(code), RoundedCornerShape(14.dp))
                                .clickableNoRipple { expanded = if (open) "" else code }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("$name (${list.size})", style = HiFiType.title.copy(fontWeight = FontWeight.Bold), color = labelColor(code))
                            Box(Modifier.weight(1f))
                            Text(if (open) "▲" else "▼", color = labelColor(code))
                        }
                        if (open) {
                            if (list.isEmpty()) {
                                Text("조건에 맞는 학과가 없어요.", style = HiFiType.caption, color = HiFiColors.Text2,
                                    modifier = Modifier.padding(top = 8.dp, start = 4.dp))
                            } else {
                                list.forEach { card ->
                                    Box(Modifier.padding(top = 10.dp)) { UnitCardView(card, onClick = { onOpenDetail(card.unitId) }) }
                                }
                            }
                        }
                    }
                    Box(Modifier.padding(12.dp)) {}
                }
            }
        }
    }
}
