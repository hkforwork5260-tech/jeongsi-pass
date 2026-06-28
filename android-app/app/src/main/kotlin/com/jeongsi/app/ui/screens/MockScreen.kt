package com.jeongsi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeongsi.app.data.MockSummary
import com.jeongsi.app.data.Repository
import com.jeongsi.app.data.UnitCard
import com.jeongsi.app.ui.UiState
import com.jeongsi.app.ui.components.HiFiButton
import com.jeongsi.app.ui.components.HiFiButtonSize
import com.jeongsi.app.ui.components.HiFiButtonVariant
import com.jeongsi.app.ui.components.UnitCardView
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import com.jeongsi.app.ui.theme.MegastudyBanner
import com.jeongsi.app.ui.theme.cardShadow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MockViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<MockSummary>>(UiState.Loading)
    val state: StateFlow<UiState<MockSummary>> = _state
    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast
    fun clearToast() { _toast.value = null }

    fun load() {
        viewModelScope.launch {
            runCatching { Repository.mockSummary() }
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "불러오기 실패") }
        }
    }

    fun remove(unitId: Long) { viewModelScope.launch { runCatching { Repository.removeMock(unitId) }; load() } }

    fun setActual(unitId: Long) {
        viewModelScope.launch {
            val err = runCatching { Repository.setActual(unitId) }.getOrNull()
            if (err != null) _toast.value = err
            load()
        }
    }

    fun cancelActual(group: String) {
        viewModelScope.launch { runCatching { Repository.removeActual(group) }; load() }
    }
}

private val GROUPS = listOf("GA" to "가", "NA" to "나", "DA" to "다")

@Composable
fun MockScreen(onOpenDetail: (Long) -> Unit, vm: MockViewModel = viewModel()) {
    val state by vm.state.collectAsStateCompat()
    val toast by vm.toast.collectAsStateCompat()
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(toast) { toast?.let { android.widget.Toast.makeText(ctx, it, android.widget.Toast.LENGTH_SHORT).show(); vm.clearToast() } }
    when (val s = state) {
        is UiState.Loading -> CenterText("불러오는 중…")
        is UiState.Error -> CenterText("⚠ ${s.message}")
        is UiState.Success -> MockContent(s.data, onOpenDetail, vm)
    }
}

@Composable
private fun MockContent(m: MockSummary, onOpenDetail: (Long) -> Unit, vm: MockViewModel) {
    var selectedGroup by remember { mutableStateOf("GA") }
    var pendingActual by remember { mutableStateOf<UnitCard?>(null) }
    val pickIds = m.picks.map { it.unitId }.toSet()
    val countByGroup = m.groups.associate { it.group to it.units.size }

    pendingActual?.let { card ->
        AlertDialog(
            onDismissRequest = { pendingActual = null },
            confirmButton = { TextButton(onClick = { vm.setActual(card.unitId); pendingActual = null }) { Text("등록") } },
            dismissButton = { TextButton(onClick = { pendingActual = null }) { Text("취소") } },
            title = { Text("실제 지원 대학 등록") },
            text = { Text("${card.university.name} ${card.departmentName}을(를) ${groupNameOf(card.recruitGroup)} 실제 지원 대학으로 등록할까요?\n\n하루 3번까지 변경할 수 있어요.") },
        )
    }

    ScrollColumn {
        Text("모의지원", style = HiFiType.display.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text)

        // 가/나/다 큰 버튼
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GROUPS.forEach { (code, label) ->
                val on = selectedGroup == code
                Column(
                    Modifier.weight(1f)
                        .background(if (on) HiFiColors.Brand else HiFiColors.Bg2, RoundedCornerShape(16.dp))
                        .border(1.dp, if (on) HiFiColors.Brand else HiFiColors.Border, RoundedCornerShape(16.dp))
                        .clickableNoRipple { selectedGroup = code }
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("${label}군", style = HiFiType.title.copy(fontWeight = FontWeight.Bold),
                        color = if (on) Color.White else HiFiColors.Text)
                    Text("${countByGroup[code] ?: 0}/20", style = HiFiType.caption,
                        color = if (on) Color.White else HiFiColors.Text2)
                }
            }
        }

        // 선택 군의 모의지원 목록
        val group = m.groups.firstOrNull { it.group == selectedGroup }
        SectionTitle("${groupNameOf(selectedGroup)} 모의지원 (${group?.units?.size ?: 0})")
        if (group == null || group.units.isEmpty()) {
            Text("이 군에 담은 모의지원이 없어요. 찾아보기·검색에서 담아보세요.", style = HiFiType.caption, color = HiFiColors.Text2)
        } else {
            group.units.forEach { card ->
                val isPick = card.unitId in pickIds
                Box(Modifier.padding(bottom = 10.dp)) {
                    UnitCardView(card, onClick = { onOpenDetail(card.unitId) }) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            ActualButton(isPick) {
                                if (isPick) vm.cancelActual(card.recruitGroup) else pendingActual = card
                            }
                            Box(Modifier.weight(1f))
                            Box(
                                Modifier.size(30.dp).clickableNoRipple { vm.remove(card.unitId) },
                                contentAlignment = Alignment.Center,
                            ) { Text("✕", style = HiFiType.body, color = HiFiColors.Text3) }
                        }
                    }
                }
            }
        }

        // 실제 지원 대학 슬롯 (가/나/다)
        SectionTitle("실제 지원 대학")
        GROUPS.forEach { (code, label) ->
            val pick = m.picks.firstOrNull { it.recruitGroup == code }
            val remain = m.remainingChanges[code] ?: 3
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    .cardShadow(RoundedCornerShape(14.dp), elevation = 5.dp)
                    .background(HiFiColors.Bg2, RoundedCornerShape(14.dp))
                    .clickableNoRipple { selectedGroup = code }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.background(HiFiColors.Brand, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                ) { Text("${label}군", style = HiFiType.body.copy(fontWeight = FontWeight.Bold), color = Color.White) }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    if (pick != null) {
                        Text("${pick.university.name} ${pick.departmentName}", style = HiFiType.body.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text)
                        Text("${pick.admission.labelName ?: ""} ${pick.admission.probability?.let { "· 합격 $it%" } ?: ""}", style = HiFiType.caption, color = HiFiColors.Text2)
                    } else {
                        Text("미설정", style = HiFiType.body, color = HiFiColors.Text3)
                    }
                }
                Text("오늘 ${remain}번 변경 가능", style = HiFiType.caption, color = HiFiColors.Text3)
            }
        }

        // 합격예측 분석 (그래프)
        SectionTitle("합격예측 분석")
        Box(
            Modifier.fillMaxWidth().cardShadow(RoundedCornerShape(16.dp), elevation = 6.dp)
                .background(HiFiColors.Bg2, RoundedCornerShape(16.dp)).padding(16.dp),
        ) {
            Column {
                Text(m.summary, style = HiFiType.body, color = HiFiColors.Text)
                if (m.picks.isNotEmpty()) {
                    ProbBar("한 곳 이상 합격", m.probAtLeast1, HiFiColors.New)
                    ProbBar("두 곳 이상 합격", m.probAtLeast2, HiFiColors.Brand)
                    ProbBar("세 곳 모두 합격", m.probAll, HiFiColors.Update)
                    ProbBar("전부 불합격", m.probNone, HiFiColors.Closing)

                    // 기대 합격 개수 + 균형 진단
                    val expected = m.picks.sumOf { (it.admission.probability ?: 0) }.toDouble() / 100.0
                    // 안정성 지수 + 기대 합격 개수
                    Box(
                        Modifier.fillMaxWidth().padding(top = 16.dp)
                            .background(HiFiColors.BrandSoft, RoundedCornerShape(12.dp)).padding(14.dp),
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("안정성 지수 ${m.stabilityScore ?: "-"}", style = HiFiType.title.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Brand)
                                Box(Modifier.weight(1f))
                                Text("기대 합격 ${"%.1f".format(expected)}곳", style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text2)
                            }
                            // 안정성 막대
                            Box(Modifier.fillMaxWidth().height(8.dp).padding(top = 8.dp).background(HiFiColors.Bg3, RoundedCornerShape(999.dp))) {
                                Box(Modifier.fillMaxWidth((m.stabilityScore ?: 0) / 100f).height(8.dp).background(HiFiColors.Brand, RoundedCornerShape(999.dp)))
                            }
                            if (m.stabilityVerdict.isNotBlank())
                                Text(m.stabilityVerdict, style = HiFiType.caption, color = HiFiColors.Text2, modifier = Modifier.padding(top = 8.dp))
                            if (m.composition.isNotBlank())
                                Text("구성: ${m.composition}", style = HiFiType.caption, color = HiFiColors.Text3, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }

                // 빈 군 채우기 / 조정 추천
                if (m.fillAdvice.isNotBlank()) {
                    Text("추천", style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text2, modifier = Modifier.padding(top = 16.dp))
                    Text("💡 ${m.fillAdvice}", style = HiFiType.body, color = HiFiColors.Text, modifier = Modifier.padding(top = 4.dp))
                    m.fillCandidate?.let { c ->
                        Box(Modifier.padding(top = 8.dp)) {
                            com.jeongsi.app.ui.components.GroupPickCard(c, onClick = { onOpenDetail(c.unitId) })
                        }
                    }
                }

                // 비슷한 성적대 인기 조합
                if (m.peerCombo.isNotEmpty()) {
                    Text("비슷한 성적대 인기 조합", style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text2, modifier = Modifier.padding(top = 16.dp))
                    Text("또래가 많이 담은 가·나·다", style = HiFiType.caption, color = HiFiColors.Text3, modifier = Modifier.padding(top = 2.dp, bottom = 4.dp))
                    m.peerCombo.forEach { c ->
                        Box(Modifier.padding(top = 8.dp)) {
                            com.jeongsi.app.ui.components.GroupPickCard(c, onClick = { onOpenDetail(c.unitId) })
                        }
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 28.dp)) {
            MegastudyBanner()
        }
    }
}

/** 실제 지원 버튼 — 플랫 펠. 미등록=브랜드 아웃라인 / 등록=솔리드 그린+체크. */
@Composable
private fun ActualButton(isPick: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .then(
                if (isPick) Modifier.background(HiFiColors.New)
                else Modifier.background(HiFiColors.Bg2).border(1.5.dp, HiFiColors.Brand, RoundedCornerShape(999.dp)),
            )
            .clickableNoRipple(onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (isPick) "✓ 실제 지원" else "실제 지원",
            style = HiFiType.body2.copy(fontWeight = FontWeight.Bold),
            color = if (isPick) Color.White else HiFiColors.Brand,
        )
    }
}

/** 가로 막대 그래프 한 줄. */
@Composable
private fun ProbBar(label: String, value: Int?, color: Color) {
    val v = value ?: 0
    Column(Modifier.padding(top = 12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = HiFiType.caption, color = HiFiColors.Text2)
            Text("$v%", style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = color)
        }
        Box(
            Modifier.fillMaxWidth().height(10.dp).padding(top = 4.dp)
                .background(HiFiColors.Bg3, RoundedCornerShape(999.dp)),
        ) {
            Box(Modifier.fillMaxWidth(v / 100f).height(10.dp).background(color, RoundedCornerShape(999.dp)))
        }
    }
}

private fun groupNameOf(code: String) = when (code) { "GA" -> "가군"; "NA" -> "나군"; "DA" -> "다군"; else -> code }

/** 조합 균형 진단 — 안정/도전 비중으로 한 줄 코멘트. */
private fun diagnose(picks: List<com.jeongsi.app.data.UnitCard>): String {
    if (picks.isEmpty()) return "실제 지원 대학을 등록하면 진단해 드려요."
    val labels = picks.mapNotNull { it.admission.labelCode }
    val safe = labels.count { it == "SAFE" || it == "MODERATE" }
    val reach = labels.count { it == "HARD" || it == "RISK" }
    val filled = picks.size
    return when {
        filled < 3 -> "아직 ${3 - filled}개 군이 비었어요. 가·나·다 모두 채우면 더 정확해요."
        reach >= 2 -> "도전 위주 조합이에요. 안정권 한 곳을 더하면 합격 안정성이 올라가요."
        safe >= 3 -> "매우 안전한 조합이에요. 한 곳쯤 상향 도전을 노려봐도 좋아요."
        else -> "안정과 도전이 균형 잡힌 조합이에요. 좋아요!"
    }
}
