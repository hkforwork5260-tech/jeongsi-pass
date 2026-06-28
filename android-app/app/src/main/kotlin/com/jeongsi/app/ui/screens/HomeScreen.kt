package com.jeongsi.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
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
import com.jeongsi.app.data.Home
import com.jeongsi.app.data.Repository
import com.jeongsi.app.data.UnitCard
import com.jeongsi.app.ui.UiState
import com.jeongsi.app.ui.components.AdmissionBadge
import com.jeongsi.app.ui.components.GroupPickCard
import com.jeongsi.app.ui.components.HiFiButton
import com.jeongsi.app.ui.components.UnitCardView
import com.jeongsi.app.ui.components.UnivAvatar
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import com.jeongsi.app.ui.theme.MegastudyBanner
import com.jeongsi.app.ui.theme.MegastudyIcon
import com.jeongsi.app.ui.theme.Pill
import com.jeongsi.app.ui.theme.brandGradient
import com.jeongsi.app.ui.theme.cardShadow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<Home>>(UiState.Loading)
    val state: StateFlow<UiState<Home>> = _state

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            runCatching { Repository.home() }
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "불러오기 실패") }
        }
    }
}

@Composable
fun HomeScreen(
    onOpenScoreInput: () -> Unit,
    onOpenDiscover: () -> Unit,
    onOpenAnalysis: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenRecommend: () -> Unit,
    onOpenStrategy: () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    val state by vm.state.collectAsStateCompat()
    // 화면 들어올 때마다 갱신(군별 추천 변동 위해)
    val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(owner) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, e ->
            if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) vm.load()
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }

    when (val s = state) {
        is UiState.Loading -> CenterText("불러오는 중…")
        is UiState.Error -> CenterText("⚠ ${s.message}\n백엔드(8080) 실행 중인지 확인하세요.")
        is UiState.Success -> HomeContent(s.data, onOpenScoreInput, onOpenAnalysis, onOpenDetail, onOpenSearch, onOpenRecommend, onOpenStrategy)
    }
}

@Composable
private fun HomeContent(
    home: Home,
    onScore: () -> Unit,
    onAnalysis: () -> Unit,
    onDetail: (Long) -> Unit,
    onSearch: () -> Unit,
    onRecommend: () -> Unit,
    onStrategy: () -> Unit,
) {
    ScrollColumn {
        // 1. ★ 히어로 헤더 (그라데이션) — 타이틀 + D-day 알약 + 검색바
        Column(
            Modifier.fillMaxWidth()
                .cardShadow(RoundedCornerShape(24.dp), elevation = 14.dp)
                .background(brandGradient(), RoundedCornerShape(24.dp))
                .padding(20.dp),
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("합격각", style = HiFiType.display, color = Color.White)
                    Row(
                        Modifier.padding(top = 4.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("${home.admissionYear}학년도 수능", style = HiFiType.body, color = Color.White.copy(alpha = 0.85f))
                        Pill("D-${home.dDay}")
                    }
                }
                // 메가스터디 M 아이콘 (글자 없음)
                MegastudyIcon(size = 46.dp)
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp)
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .clickableNoRipple(onSearch)
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text("🔍", style = HiFiType.body)
                Text("대학·학과 검색", style = HiFiType.body, color = HiFiColors.Text3,
                    modifier = Modifier.padding(start = 8.dp))
            }
        }
        Box(Modifier.padding(bottom = 14.dp)) {}

        // 2. 내 성적 요약
        Box(
            Modifier.fillMaxWidth()
                .cardShadow(RoundedCornerShape(18.dp), elevation = 6.dp)
                .background(HiFiColors.BrandSoft, RoundedCornerShape(18.dp))
                .padding(16.dp),
        ) {
            if (!home.hasScore || home.analysis == null) {
                Column {
                    Text("아직 성적이 없어요", style = HiFiType.title, color = HiFiColors.Text)
                    Text("성적을 입력하면 내 합격각을 보여드려요", style = HiFiType.caption, color = HiFiColors.Text2,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
                    HiFiButton("성적 입력하고 내 합격각 보기", onClick = onScore, fullWidth = true)
                }
            } else {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).clickableNoRipple(onAnalysis)) {
                        Text("내 성적", style = HiFiType.caption, color = HiFiColors.Text2)
                        Text("합격 분석 보기 ›", style = HiFiType.title.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Brand)
                    }
                    Text("성적 수정", style = HiFiType.caption.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text2,
                        modifier = Modifier.clickableNoRipple(onScore))
                }
            }
        }

        // 3. ★ HERO — 내 점수로 지원 가능한 학과 (더보기 → 라벨별 추천)
        SectionTitle("내 점수로 지원 가능한 학과", action = "더보기", onAction = onRecommend)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            home.heroUnits.take(6).forEach { card ->
                HeroCard(card, onClick = { onDetail(card.unitId) })
            }
        }

        // 4. 군별 추천 조합 (간소화 색상 카드, 더보기 → 전략)
        if (home.groupRecommend.isNotEmpty()) {
            SectionTitle("군별 추천 조합", action = "전략 더보기", onAction = onStrategy)
            home.groupRecommend.forEach { card ->
                Box(Modifier.padding(bottom = 10.dp)) {
                    GroupPickCard(card, onClick = { onDetail(card.unitId) })
                }
            }
        }

        // 5. 성적대별 인기 TOP5 (카드 + 메달 랭크, 행 탭 → 상세)
        if (home.popularTop5.isNotEmpty()) {
            SectionTitle("성적대별 인기 학과 TOP5")
            Column(
                Modifier.fillMaxWidth()
                    .cardShadow(RoundedCornerShape(18.dp), elevation = 6.dp)
                    .background(HiFiColors.Bg2, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            ) {
                home.popularTop5.forEach { p ->
                    Row(
                        Modifier.fillMaxWidth().clickableNoRipple { onDetail(p.unitId) }.padding(vertical = 10.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        RankBadge(p.rank, Modifier.padding(end = 10.dp))
                        UnivAvatar(p.university.name, size = 36.dp)
                        Column(Modifier.weight(1f).padding(start = 10.dp)) {
                            Text("${p.university.name} ${p.departmentName}", style = HiFiType.body, color = HiFiColors.Text)
                            Text("${p.recruitGroupName} · 배치컷 ${p.cutPercentile}", style = HiFiType.caption, color = HiFiColors.Text2)
                        }
                        Text("모의지원 ${p.mockApplyCount}", style = HiFiType.caption, color = HiFiColors.Text2)
                    }
                }
            }
        }

        // 메가스터디 벤치마킹 — 가로 배너 (크게)
        Box(Modifier.fillMaxWidth().padding(top = 30.dp, bottom = 20.dp)) {
            MegastudyBanner()
        }
    }
}

@Composable
private fun RankBadge(rank: Int, modifier: Modifier = Modifier) {
    val solid = rank <= 3
    Box(
        modifier.size(28.dp)
            .background(if (solid) HiFiColors.Brand else HiFiColors.BrandSoft, RoundedCornerShape(999.dp)),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Text("$rank", style = HiFiType.caption, color = if (solid) Color.White else HiFiColors.Brand)
    }
}

@Composable
private fun HeroCard(card: UnitCard, onClick: () -> Unit) {
    Column(
        Modifier.width(220.dp)
            .cardShadow(RoundedCornerShape(18.dp), elevation = 8.dp)
            .background(HiFiColors.Bg2, RoundedCornerShape(18.dp))
            .clickableNoRipple(onClick)
            .padding(16.dp),
    ) {
        AdmissionBadge(card)
        Text(card.university.name, style = HiFiType.caption, color = HiFiColors.Text2,
            modifier = Modifier.padding(top = 10.dp))
        Text(card.departmentName, style = HiFiType.title.copy(fontWeight = FontWeight.Bold), color = HiFiColors.Text)
        Text("${card.recruitGroupName} · 배치컷 ${card.admission.cutPercentile}",
            style = HiFiType.caption, color = HiFiColors.Text2, modifier = Modifier.padding(top = 6.dp))
    }
}

