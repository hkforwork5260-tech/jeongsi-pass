package com.jeongsi.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeongsi.app.data.Repository
import com.jeongsi.app.data.UnitCard
import com.jeongsi.app.ui.UiState
import com.jeongsi.app.ui.components.HiFiChip
import com.jeongsi.app.ui.components.HiFiChipVariant
import com.jeongsi.app.ui.components.UnivAvatar
import com.jeongsi.app.ui.components.labelColor
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import com.jeongsi.app.ui.theme.MegastudyIcon
import com.jeongsi.app.ui.theme.cardShadow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DiscoverViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<UnitCard>>>(UiState.Loading)
    val state: StateFlow<UiState<List<UnitCard>>> = _state

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            runCatching { Repository.discover(30) }
                .onSuccess { _state.value = UiState.Success(it) }
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
                patch(card.unitId) { it.copy(isMockApplied = false) }
            } else {
                val err = runCatching { Repository.addMock(card.unitId) }.getOrNull()
                if (err == null) patch(card.unitId) { it.copy(isMockApplied = true) } else _toast.value = err
            }
        }
    }

    private fun patch(unitId: Long, f: (UnitCard) -> UnitCard) {
        val cur = _state.value
        if (cur is UiState.Success) _state.value = UiState.Success(cur.data.map { if (it.unitId == unitId) f(it) else it })
    }
}

@Composable
fun DiscoverScreen(onOpenDetail: (Long) -> Unit, vm: DiscoverViewModel = viewModel()) {
    val state by vm.state.collectAsStateCompat()
    val toast by vm.toast.collectAsStateCompat()
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(toast) { toast?.let { android.widget.Toast.makeText(ctx, it, android.widget.Toast.LENGTH_SHORT).show(); vm.clearToast() } }

    when (val s = state) {
        is UiState.Loading -> CenterText("불러오는 중…")
        is UiState.Error -> CenterText("⚠ ${s.message}\n백엔드(8080) 실행 중인지 확인하세요.")
        is UiState.Success -> {
            if (s.data.isEmpty()) { CenterText("표시할 학과가 없습니다."); return }
            val pager = rememberPagerState(pageCount = { s.data.size })
            VerticalPager(
                state = pager,
                modifier = Modifier.fillMaxSize(),
                // 한 번에 한 장씩만 넘어가게(휙휙 방지)
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pager,
                    pagerSnapDistance = PagerSnapDistance.atMost(1),
                ),
            ) { page ->
                DiscoverCard(s.data[page], vm, onOpenDetail)
            }
        }
    }
}

@Composable
private fun DiscoverCard(card: UnitCard, vm: DiscoverViewModel, onOpenDetail: (Long) -> Unit) {
    val ctx = LocalContext.current
    val lc = labelColor(card.admission.labelCode)
    Box(Modifier.fillMaxSize().background(HiFiColors.Bg)) {
        // 메가스터디 M 아이콘 (상단 빈 공간)
        MegastudyIcon(
            size = 52.dp,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 28.dp, top = 28.dp),
        )
        Column(
            Modifier.fillMaxSize().padding(start = 28.dp, end = 96.dp, top = 40.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            // 대학
            Row(verticalAlignment = Alignment.CenterVertically) {
                UnivAvatar(card.university.name, size = 52.dp)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(card.university.name, style = HiFiType.title.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text)
                    Text(card.university.region, style = HiFiType.caption, color = HiFiColors.Text2)
                }
            }
            // 학과
            Text(
                card.departmentName,
                style = HiFiType.display.copy(fontWeight = FontWeight.Bold),
                color = HiFiColors.Text,
                modifier = Modifier.padding(top = 28.dp),
            )
            // 군 / 5단계 / 합격%
            Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                HiFiChip(card.recruitGroupName, small = true, variant = HiFiChipVariant.Outline)
                card.admission.labelName?.let {
                    Box(Modifier.background(HiFiColors.Bg3, RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 5.dp)) {
                        Text(it, style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = lc)
                    }
                }
                card.admission.probability?.let { p ->
                    Box(Modifier.background(lc, RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 5.dp)) {
                        Text("합격 $p%", style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }
            }
            // 스탯 카드
            Row(Modifier.padding(top = 28.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("배치컷", card.admission.cutPercentile.toString(), Modifier.weight(1f))
                Stat("경쟁률", card.competitionRate?.let { "$it:1" } ?: "-", Modifier.weight(1f))
                Stat("모집", "${card.quota}명", Modifier.weight(1f))
            }
            // 반영/내환산
            Text(
                "${card.reflectAreas} · ${card.indexName} · 수능 ${card.suneungRatio.toInt()}%" +
                    (card.admission.convertedScore?.let { "   내 환산 $it" } ?: ""),
                style = HiFiType.caption, color = HiFiColors.Text2, modifier = Modifier.padding(top = 24.dp),
            )
            if (!card.admission.eligible && card.admission.eligibleReason != null) {
                Text("⚠ ${card.admission.eligibleReason}", style = HiFiType.caption, color = HiFiColors.Closing,
                    modifier = Modifier.padding(top = 10.dp))
            }
        }

        // 우측 액션 버튼 (이미지3 식) — 모의지원 / 지원(홈페이지) / 상세
        Column(
            Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ActionBtn(Icons.Filled.Add, if (card.isMockApplied) "담김" else "모의지원",
                active = card.isMockApplied) { vm.toggleMock(card) }
            ActionBtn(Icons.Outlined.OpenInNew, "지원", active = false) {
                card.university.homepageUrl?.let { url -> runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } }
            }
            ActionBtn(Icons.Outlined.Info, "상세", active = false) { onOpenDetail(card.unitId) }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    Column(
        modifier.cardShadow(RoundedCornerShape(16.dp), elevation = 5.dp)
            .background(HiFiColors.Bg2, RoundedCornerShape(16.dp)).padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = HiFiType.caption, color = HiFiColors.Text2)
        Text(value, style = HiFiType.title.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ActionBtn(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(56.dp)
                .background(if (active) HiFiColors.Brand else HiFiColors.Bg2, RoundedCornerShape(28.dp))
                .border(if (active) 0.dp else 1.dp, HiFiColors.Border, RoundedCornerShape(28.dp))
                .clickableNoRipple(onClick),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = label, tint = if (active) Color.White else HiFiColors.Text, modifier = Modifier.size(26.dp)) }
        Text(label, style = HiFiType.caption, color = HiFiColors.Text2, modifier = Modifier.padding(top = 4.dp))
    }
}
