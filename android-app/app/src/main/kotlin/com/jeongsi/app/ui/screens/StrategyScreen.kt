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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeongsi.app.data.Combo
import com.jeongsi.app.data.Repository
import com.jeongsi.app.ui.components.ComboCard
import com.jeongsi.app.ui.components.HiFiChip
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StrategyViewModel : ViewModel() {
    private val _combos = MutableStateFlow<List<Combo>>(emptyList())
    val combos: StateFlow<List<Combo>> = _combos
    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore
    var loading = false; private set

    var ga = "SAFE"; var na = "MODERATE"; var da = "REACH"; var track: String? = null
    var region: String? = null; var university: String? = null; var department: String? = null
    private var offset = 0

    private val _univs = MutableStateFlow<List<String>>(emptyList())
    val univs: StateFlow<List<String>> = _univs
    private val _depts = MutableStateFlow<List<String>>(emptyList())
    val depts: StateFlow<List<String>> = _depts
    fun loadLists() {
        viewModelScope.launch { runCatching { Repository.universities() }.onSuccess { _univs.value = it } }
        viewModelScope.launch { runCatching { Repository.departments() }.onSuccess { _depts.value = it } }
    }

    fun reset() {
        offset = 0; _combos.value = emptyList(); _hasMore.value = false
        load()
    }

    fun load() {
        if (loading) return
        loading = true
        viewModelScope.launch {
            runCatching { Repository.strategyCombos("balanced", ga, na, da, track, region, university, department, offset, 12) }
                .onSuccess { s ->
                    _combos.value = _combos.value + s.combos
                    _hasMore.value = s.hasMore
                    offset += s.combos.size
                }
            loading = false
        }
    }
}

private val LABELS = listOf("SAFE" to "안정", "MODERATE" to "적정", "REACH" to "소신", "HARD" to "상향", "RISK" to "위험")

@Composable
fun StrategyScreen(onBack: () -> Unit, onOpenDetail: (Long) -> Unit, onOpenCombo: (List<Long>) -> Unit = {}, vm: StrategyViewModel = viewModel()) {
    val combos by vm.combos.collectAsStateCompat()
    val hasMore by vm.hasMore.collectAsStateCompat()
    val univs by vm.univs.collectAsStateCompat()
    val depts by vm.depts.collectAsStateCompat()
    LaunchedEffect(Unit) { if (combos.isEmpty()) vm.reset() }
    LaunchedEffect(Unit) { vm.loadLists() }

    Column(Modifier.fillMaxSize()) {
        AppTopBar("군별 추천 조합", onBack)
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            item {
                Text("군별로 원하는 합격 라벨을 정하면 맞는 조합을 추천해 드려요.", style = HiFiType.caption, color = HiFiColors.Text2,
                    modifier = Modifier.padding(vertical = 8.dp))
                // 군별 라벨 직접 지정
                LabelRow("가군", vm.ga) { vm.ga = it; vm.reset() }
                LabelRow("나군", vm.na) { vm.na = it; vm.reset() }
                LabelRow("다군", vm.da) { vm.da = it; vm.reset() }
                // 계열
                Row(Modifier.padding(top = 8.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HiFiChip("전체계열", selected = vm.track == null, small = true, onClick = { vm.track = null; vm.reset() })
                    HiFiChip("인문", selected = vm.track == "humanities", small = true, onClick = { vm.track = "humanities"; vm.reset() })
                    HiFiChip("자연", selected = vm.track == "natural", small = true, onClick = { vm.track = "natural"; vm.reset() })
                }
                // 지역·학교·학과 필터 (검색탭과 동일)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterDropdown("지역", vm.region, REGIONS.map { it to it }) { vm.region = it; vm.reset() }
                    FilterDropdown("학교", vm.university, univs.map { it to it.replace("대학교", "대") }) { vm.university = it; vm.reset() }
                    FilterDropdown("학과", vm.department, depts.map { it to it }, columns = 2) { vm.department = it; vm.reset() }
                }
                Box(Modifier.padding(top = 2.dp)) {}
            }
            if (combos.isEmpty()) {
                item { Box(Modifier.padding(top = 24.dp)) { Text("조건에 맞는 조합이 없어요. 라벨·계열을 바꿔보세요.", color = HiFiColors.Text2) } }
            }
            items(combos) { c ->
                Box(Modifier.padding(bottom = 10.dp)) {
                    ComboCard(c, onOpenDetail = onOpenDetail, onOpenCombo = { onOpenCombo(c.picks.map { it.unitId }) })
                }
            }
            if (hasMore) {
                item {
                    LaunchedEffect(combos.size) { vm.load() }   // 끝 도달 시 더 불러오기
                    Box(Modifier.fillMaxWidth().padding(16.dp)) { Text("더 불러오는 중…", color = HiFiColors.Text3) }
                }
            }
            item { Box(Modifier.padding(20.dp)) {} }
        }
    }
}

@Composable
private fun LabelRow(group: String, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.padding(top = 6.dp).fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(group, style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text2,
            modifier = Modifier.padding(end = 8.dp))
        Row(
            Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            LABELS.forEach { (code, name) ->
                HiFiChip(name, selected = selected == code, small = true, onClick = { onSelect(code) })
            }
        }
    }
}
