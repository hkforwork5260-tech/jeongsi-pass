package com.jeongsi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeongsi.app.data.Repository
import com.jeongsi.app.data.UnitCard
import com.jeongsi.app.ui.UiState
import com.jeongsi.app.ui.components.HiFiButton
import com.jeongsi.app.ui.components.HiFiButtonSize
import com.jeongsi.app.ui.components.HiFiButtonVariant
import com.jeongsi.app.ui.components.HiFiChip
import com.jeongsi.app.ui.components.UnitCardView
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<UnitCard>>>(UiState.Loading)
    val state: StateFlow<UiState<List<UnitCard>>> = _state

    var track: String? = null
    var region: String? = null
    var group: String? = null
    var sort: String? = null
    var university: String? = null
    var department: String? = null
    var query: String = ""

    private val _univs = MutableStateFlow<List<String>>(emptyList())
    val univs: StateFlow<List<String>> = _univs
    private val _depts = MutableStateFlow<List<String>>(emptyList())
    val depts: StateFlow<List<String>> = _depts
    fun loadLists() {
        viewModelScope.launch { runCatching { Repository.universities() }.onSuccess { _univs.value = it } }
        viewModelScope.launch { runCatching { Repository.departments() }.onSuccess { _depts.value = it } }
    }

    fun search() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            runCatching { Repository.search(group = group, track = track, region = region, q = query.ifBlank { null }, sort = sort, university = university) }
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "검색 실패") }
        }
    }

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast
    fun clearToast() { _toast.value = null }

    fun addMock(card: UnitCard) {
        viewModelScope.launch {
            val err = runCatching { Repository.addMock(card.unitId) }.getOrNull()
            if (err == null) {
                val cur = _state.value
                if (cur is UiState.Success) _state.value = UiState.Success(cur.data.map { if (it.unitId == card.unitId) it.copy(isMockApplied = true) else it })
            } else _toast.value = err
        }
    }
}

private enum class Mode(val label: String) { BASIC("기본 검색"), RECOMMEND("추천"), THEME("인기 테마"), DIRECT("직접 검색") }

private val REGIONS = listOf("서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주")
private val THEMES = listOf("의예" to "의대", "약학" to "약대", "수의예" to "수의대", "한의예" to "한의대", "반도체" to "반도체", "컴퓨터" to "컴퓨터/SW", "소프트웨어" to "소프트웨어", "경영" to "경영", "교육" to "교대/사범")

@Composable
fun SearchScreen(onOpenDetail: (Long) -> Unit, onBack: () -> Unit, vm: SearchViewModel = viewModel()) {
    val state by vm.state.collectAsStateCompat()
    val toast by vm.toast.collectAsStateCompat()
    val sctx = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(toast) { toast?.let { android.widget.Toast.makeText(sctx, it, android.widget.Toast.LENGTH_SHORT).show(); vm.clearToast() } }
    val univs by vm.univs.collectAsStateCompat()
    val depts by vm.depts.collectAsStateCompat()
    LaunchedEffect(Unit) { vm.loadLists() }
    var selUniv by remember { mutableStateOf<String?>(null) }
    var selDept by remember { mutableStateOf<String?>(null) }
    var mode by remember { mutableStateOf(Mode.BASIC) }
    var text by remember { mutableStateOf("") }
    var selTrack by remember { mutableStateOf<String?>(null) }
    var selRegion by remember { mutableStateOf<String?>(null) }
    var selGroup by remember { mutableStateOf<String?>(null) }
    var selTheme by remember { mutableStateOf<String?>(null) }

    fun run() { vm.search() }
    LaunchedEffect(Unit) { vm.search() }

    Column(Modifier.fillMaxSize()) {
      AppTopBar("대학 검색", onBack)
      Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {

        // 모드 탭
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Mode.entries.forEach { m ->
                HiFiChip(m.label, selected = mode == m, small = true, onClick = {
                    mode = m
                    if (m != Mode.DIRECT && m != Mode.THEME) { text = ""; vm.query = "" }
                    if (m != Mode.THEME) selTheme = null
                    vm.sort = if (m == Mode.RECOMMEND) "fit" else null
                    run()
                })
            }
        }

        // 모드별 컨트롤
        when (mode) {
            Mode.DIRECT -> {
                Box(Modifier.fillMaxWidth().padding(top = 10.dp).background(HiFiColors.Bg2, RoundedCornerShape(14.dp)).padding(14.dp)) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it; vm.query = it; run() },
                        singleLine = true,
                        textStyle = HiFiType.body.copy(color = HiFiColors.Text),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { run() }),
                        decorationBox = { inner ->
                            if (text.isEmpty()) Text("예) 서울대 / 컴퓨터 / 경영학과", style = HiFiType.body, color = HiFiColors.Text3)
                            inner()
                        },
                    )
                }
            }
            Mode.THEME -> {
                Row(Modifier.padding(top = 10.dp).fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    THEMES.forEach { (kw, label) ->
                        Chip(label, selTheme == kw) { selTheme = kw; vm.query = kw; run() }
                    }
                }
            }
            Mode.BASIC, Mode.RECOMMEND -> {
                // 드롭다운 필터: 계열 / 군 / 지역 / 학교 / 학과
                Row(Modifier.padding(top = 10.dp).fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterDropdown("계열", selTrack, listOf("humanities" to "인문", "natural" to "자연", "arts" to "예체능")) {
                        selTrack = it; vm.track = it; run()
                    }
                    FilterDropdown("군", selGroup, listOf("GA" to "가군", "NA" to "나군", "DA" to "다군")) {
                        selGroup = it; vm.group = it; run()
                    }
                    FilterDropdown("지역", selRegion, REGIONS.map { it to it }) {
                        selRegion = it; vm.region = it; run()
                    }
                    FilterDropdown("학교", selUniv, univs.map { it to it.replace("대학교", "대") }) {
                        selUniv = it; vm.university = it; run()
                    }
                    FilterDropdown("학과", selDept, depts.map { it to it }, columns = 2) {
                        selDept = it; vm.department = it; run()
                    }
                }
                if (mode == Mode.RECOMMEND) {
                    Text("내 성적에 맞는 순으로 추천합니다.", style = HiFiType.caption, color = HiFiColors.Brand,
                        modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        // 결과
        when (val s = state) {
            is UiState.Loading -> CenterText("검색 중…")
            is UiState.Error -> CenterText("⚠ ${s.message}")
            is UiState.Success -> {
                Text("검색 결과 ${s.data.size}개", style = HiFiType.caption, color = HiFiColors.Text2,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp))
                if (s.data.isEmpty()) { CenterText("결과가 없습니다. 조건을 바꿔보세요."); return@Column }
                LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(s.data, key = { it.unitId }) { card ->
                        UnitCardView(card, onClick = { onOpenDetail(card.unitId) }) {
                            HiFiButton(
                                if (card.isMockApplied) "모의지원 완료" else "모의지원",
                                onClick = { vm.addMock(card) },
                                variant = if (card.isMockApplied) HiFiButtonVariant.Ghost else HiFiButtonVariant.Primary,
                                size = HiFiButtonSize.Sm,
                                enabled = !card.isMockApplied,
                            )
                        }
                    }
                    item { Box(Modifier.padding(8.dp)) {} }
                }
            }
        }
      }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    HiFiChip(text = label, selected = selected, small = true, onClick = onClick)
}

/** 필터 칩 — 탭 시 3칸 그리드 + 세로 스크롤 선택 다이얼로그. 선택값 없으면 label, 있으면 선택값 표시. */
@Composable
private fun FilterDropdown(label: String, selected: String?, options: List<Pair<String, String>>, columns: Int = 3, onPick: (String?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val display = options.firstOrNull { it.first == selected }?.second
    val on = selected != null
    Row(
        Modifier.background(if (on) HiFiColors.Brand else HiFiColors.Bg2, RoundedCornerShape(999.dp))
            .border(1.dp, if (on) HiFiColors.Brand else HiFiColors.Border, RoundedCornerShape(999.dp))
            .clickableNoRipple { open = true }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(display ?: label, style = HiFiType.caption.copy(fontWeight = FontWeight.Bold),
            color = if (on) androidx.compose.ui.graphics.Color.White else HiFiColors.Text)
        Text(" ▾", style = HiFiType.caption, color = if (on) androidx.compose.ui.graphics.Color.White else HiFiColors.Text2)
    }
    if (open) {
        FilterPickerDialog(label, selected, options, columns, onDismiss = { open = false }) {
            onPick(it); open = false
        }
    }
}

/** 선택지를 N칸 그리드로 펼치고 세로 스크롤되는 다이얼로그. 셀은 균일 높이·가운데정렬. */
@Composable
private fun FilterPickerDialog(
    label: String,
    selected: String?,
    options: List<Pair<String, String>>,
    columns: Int,
    onDismiss: () -> Unit,
    onPick: (String?) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth()
                .background(HiFiColors.Bg, RoundedCornerShape(18.dp))
                .padding(18.dp),
        ) {
            Text("$label 선택", style = HiFiType.title, color = HiFiColors.Text)
            Spacer(Modifier.height(14.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.heightIn(max = 420.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { PickerCell("전체", selected == null) { onPick(null) } }
                gridItems(options) { (v, d) -> PickerCell(d, selected == v) { onPick(v) } }
            }
        }
    }
}

/** 그리드 셀 — 고정 높이·가운데정렬·최대 2줄(말줄임). 들쭉날쭉/줄바꿈 orphan 방지. */
@Composable
private fun PickerCell(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) HiFiColors.Brand else HiFiColors.Bg2)
            .border(1.dp, if (selected) HiFiColors.Brand else HiFiColors.Border, RoundedCornerShape(12.dp))
            .clickableNoRipple { onClick() }
            .padding(horizontal = 6.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Text(
            text,
            style = HiFiType.caption.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.sp),
            color = if (selected) androidx.compose.ui.graphics.Color.White else HiFiColors.Text,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
