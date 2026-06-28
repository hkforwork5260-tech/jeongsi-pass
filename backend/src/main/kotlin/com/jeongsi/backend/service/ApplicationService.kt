package com.jeongsi.backend.service

import com.jeongsi.backend.dto.CompareDto
import com.jeongsi.backend.dto.Labels
import com.jeongsi.backend.dto.MockSummaryDto
import com.jeongsi.backend.dto.ReportDto
import com.jeongsi.backend.dto.ReportGroupDto
import com.jeongsi.backend.dto.UnitCardDto
import com.jeongsi.backend.entity.Favorite
import com.jeongsi.backend.entity.MockApplication
import com.jeongsi.backend.repository.FavoriteRepository
import com.jeongsi.backend.repository.MockApplicationRepository
import com.jeongsi.backend.repository.RecruitmentUnitRepository
import com.jeongsi.backend.repository.UserScoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.roundToInt

/** 모의지원 + 합격예측 리포트 + 조합 분석. */
@Service
class ApplicationService(
    private val mockApps: MockApplicationRepository,
    private val favorites: FavoriteRepository,
    private val units: RecruitmentUnitRepository,
    private val userScores: UserScoreRepository,
    private val catalog: CatalogService,
    private val targetService: TargetService,
    private val actualService: ActualApplicationService,
) {
    companion object { const val MAX_PER_GROUP = 20 }
    // ---- 모의지원 ----
    enum class AddResult { OK, NOT_FOUND, LIMIT }

    /** 모의지원 추가. 군별 최대 20개 제한. */
    @Transactional
    fun addMock(deviceDbId: Long, unitId: Long): AddResult {
        val unit = units.findById(unitId).orElse(null) ?: return AddResult.NOT_FOUND
        if (mockApps.findByDeviceIdAndRecruitmentUnitId(deviceDbId, unitId) != null) return AddResult.OK
        val inGroup = mockApps.findByDeviceId(deviceDbId).count { it.recruitGroup == unit.recruitGroup }
        if (inGroup >= MAX_PER_GROUP) return AddResult.LIMIT
        mockApps.save(MockApplication(deviceId = deviceDbId, recruitmentUnitId = unitId, recruitGroup = unit.recruitGroup))
        return AddResult.OK
    }

    @Transactional
    fun removeMock(deviceDbId: Long, unitId: Long) =
        mockApps.deleteByDeviceIdAndRecruitmentUnitId(deviceDbId, unitId)

    fun listMock(deviceDbId: Long): List<UnitCardDto> {
        val unitIds = mockApps.findByDeviceId(deviceDbId).map { it.recruitmentUnitId }
        if (unitIds.isEmpty()) return emptyList()
        return catalog.buildCards(units.findAllById(unitIds), deviceDbId)
    }

    // ---- 관심 ----
    @Transactional
    fun addFavorite(deviceDbId: Long, unitId: Long): Boolean {
        if (!units.existsById(unitId)) return false
        if (favorites.findByDeviceIdAndRecruitmentUnitId(deviceDbId, unitId) != null) return true
        favorites.save(Favorite(deviceId = deviceDbId, recruitmentUnitId = unitId))
        return true
    }

    @Transactional
    fun removeFavorite(deviceDbId: Long, unitId: Long) =
        favorites.deleteByDeviceIdAndRecruitmentUnitId(deviceDbId, unitId)

    fun listFavorites(deviceDbId: Long): List<UnitCardDto> {
        val unitIds = favorites.findByDeviceId(deviceDbId).map { it.recruitmentUnitId }
        if (unitIds.isEmpty()) return emptyList()
        return catalog.buildCards(units.findAllById(unitIds), deviceDbId)
    }

    // ---- 합격예측 리포트 (군별 묶음) ----
    fun report(deviceDbId: Long): ReportDto {
        val hasScore = userScores.findByDeviceId(deviceDbId) != null
        val cards = listMock(deviceDbId)
        val groups = listOf("GA", "NA", "DA").map { g ->
            ReportGroupDto(
                group = g,
                groupName = Labels.groupName(g),
                units = cards.filter { it.recruitGroup == g }
                    .sortedByDescending { it.admission.positionDelta ?: -99.0 },
            )
        }.filter { it.units.isNotEmpty() }

        val summary = when {
            !hasScore -> "성적을 입력하면 합격 가능성이 계산됩니다. 현재는 담은 모의지원만 표시합니다."
            cards.isEmpty() -> "모의지원을 담으면 군별 조합과 합격 가능성을 보여드립니다."
            else -> {
                val safe = cards.count { it.admission.labelCode == "SAFE" || it.admission.labelCode == "MODERATE" }
                val reach = cards.count { it.admission.labelCode == "HARD" || it.admission.labelCode == "RISK" }
                "총 ${cards.size}개 · 안정권 ${safe}개 · 도전(상향·위험) ${reach}개. 군별 1개씩 균형을 맞춰보세요."
            }
        }
        return ReportDto(hasScore = hasScore, groups = groups, summary = summary)
    }

    /** 비교분석 리포트 — 한 군 내 모의지원 대학을 ▲▼·5단계로 나란히 비교. */
    fun compare(deviceDbId: Long, group: String): CompareDto {
        val cards = listMock(deviceDbId).filter { it.recruitGroup == group }
            .sortedByDescending { it.admission.positionDelta ?: -99.0 }
        val safest = cards.firstOrNull { it.admission.positionDelta != null }
        val summary = when {
            cards.isEmpty() -> "${Labels.groupName(group)}에 담은 모의지원이 없습니다."
            cards.size == 1 -> "${Labels.groupName(group)}에 1개 — 비교하려면 더 담아보세요."
            safest != null -> "가장 안정적인 선택은 ${safest.university.name} ${safest.departmentName} (${safest.admission.labelName})."
            else -> "성적을 입력하면 합격 가능성으로 비교됩니다."
        }
        return CompareDto(group, Labels.groupName(group), cards, safest, summary)
    }

    /**
     * 모의지원 탭 요약 — 군별 모의지원 목록 + 가/나/다 배치(목표) 조합의 합격 확률 분석.
     * 정시는 군당 1곳 지원이므로 배치(target)된 가·나·다 각 1개로 조합을 구성.
     * 합격 확률(독립 가정): P(0곳)=Π(1−pᵢ), P(≥1)=1−P(0), P(≥2), P(3곳).
     */
    fun summary(deviceDbId: Long): MockSummaryDto {
        val cards = listMock(deviceDbId)
        val groups = listOf("GA", "NA", "DA", "OUT").mapNotNull { g ->
            val gc = cards.filter { it.recruitGroup == g }.sortedByDescending { it.admission.positionDelta ?: -99.0 }
            if (gc.isEmpty()) null else ReportGroupDto(g, Labels.groupName(g), gc)
        }
        // 조합 = 군별 "실제 지원 대학"(사용자가 직접 등록). 없으면 비어 있음(자동 추천 안 함).
        val actualPickIds = actualService.picksByGroup(deviceDbId)  // group → unitId
        val picks = listOf("GA", "NA", "DA").mapNotNull { g ->
            val uid = actualPickIds[g] ?: return@mapNotNull null
            cards.firstOrNull { it.unitId == uid }
        }
        val probs = picks.mapNotNull { it.admission.probability?.let { p -> p / 100.0 } }

        val pNone = probs.fold(1.0) { acc, p -> acc * (1 - p) }
        val pAll = if (probs.isEmpty()) 0.0 else probs.fold(1.0) { acc, p -> acc * p }
        val pAtLeast1 = if (probs.isEmpty()) 0.0 else 1 - pNone
        // P(≥2) = 1 − P(0) − P(정확히1)
        val pExactly1 = probs.indices.sumOf { i -> probs[i] * probs.filterIndexed { j, _ -> j != i }.fold(1.0) { a, p -> a * (1 - p) } }
        val pAtLeast2 = (1 - pNone - pExactly1).coerceAtLeast(0.0)

        val composition = picks.mapNotNull { it.admission.labelName }.groupingBy { it }.eachCount()
        val compText = listOf("안정", "적정", "소신", "상향", "위험").mapNotNull { l ->
            composition[l]?.let { "$l ${it}개" }
        }.joinToString(" · ")

        val summary = when {
            cards.isEmpty() -> "찾아보기·검색에서 모의지원을 담아보세요."
            picks.isEmpty() -> "가·나·다 버튼에서 실제 지원 대학을 등록하면 조합 합격 확률을 분석해 드려요."
            probs.isEmpty() -> "성적을 입력하면 조합 합격 확률이 계산됩니다."
            else -> "내 조합: ${compText.ifBlank { "-" }}. 한 곳 이상 합격 확률 ${(pAtLeast1 * 100).roundToInt()}%."
        }
        // ⑥b 분석 ----------------------------------------------------------
        val stability = if (picks.isEmpty()) null else ComboMath.stability(picks)
        val verdict = stability?.let { ComboMath.verdict(it) } ?: ""

        // 빈 군 채우기 / 조정 추천
        val emptyGroups = listOf("GA", "NA", "DA").filter { g -> picks.none { it.recruitGroup == g } }
        var fillAdvice = ""
        var fillCandidate: UnitCardDto? = null
        if (emptyGroups.isNotEmpty()) {
            val g = emptyGroups.first()
            fillCandidate = cards.filter { it.recruitGroup == g }.maxByOrNull { it.admission.positionDelta ?: -99.0 }
            fillAdvice = if (fillCandidate != null)
                "${Labels.groupName(g)}이 비었어요. ${fillCandidate.university.name} ${fillCandidate.departmentName}을(를) 추천해요."
            else "${Labels.groupName(g)} 모의지원을 담아 조합을 채워보세요."
        } else if (picks.isNotEmpty()) {
            val reach = picks.count { it.admission.labelCode == "HARD" || it.admission.labelCode == "RISK" }
            fillAdvice = if (reach >= 2) "상향·위험이 많아요. 안정권 한 곳으로 조정을 고려하세요." else "균형 잡힌 조합이에요. 좋아요!"
        }

        // 비슷한 성적대 인기 조합 — 군별 모의지원 최다 학과
        val peerCombo = run {
            val counts = mockApps.findAll().groupingBy { it.recruitmentUnitId }.eachCount()
            val topIds = listOf("GA", "NA", "DA").mapNotNull { g ->
                units.findAll().filter { it.recruitGroup == g }
                    .maxByOrNull { counts[it.id] ?: 0 }?.id
            }
            if (topIds.isEmpty()) emptyList() else catalog.buildCards(units.findAllById(topIds), deviceDbId)
                .sortedBy { listOf("GA", "NA", "DA").indexOf(it.recruitGroup) }
        }

        return MockSummaryDto(
            totalCount = cards.size,
            groups = groups,
            picks = picks,
            probNone = pct(pNone),
            probAtLeast1 = pct(pAtLeast1),
            probAtLeast2 = pct(pAtLeast2),
            probAll = pct(pAll),
            composition = compText,
            summary = summary,
            remainingChanges = actualService.remainingChanges(deviceDbId),
            stabilityScore = stability,
            stabilityVerdict = verdict,
            fillAdvice = fillAdvice,
            fillCandidate = fillCandidate,
            peerCombo = peerCombo,
        )
    }

    private fun pct(p: Double): Int? = if (p <= 0.0) null else (p * 100).roundToInt().coerceIn(1, 99)
}
