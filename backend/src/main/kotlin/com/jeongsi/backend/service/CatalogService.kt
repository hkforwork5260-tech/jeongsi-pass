package com.jeongsi.backend.service

import com.jeongsi.backend.dto.AdmissionDto
import com.jeongsi.backend.dto.Labels
import com.jeongsi.backend.dto.UnitCardDto
import com.jeongsi.backend.dto.UnitDetailDto
import com.jeongsi.backend.dto.UniversityDto
import com.jeongsi.backend.entity.RecruitmentUnit
import com.jeongsi.backend.entity.University
import com.jeongsi.backend.entity.UserScore
import com.jeongsi.backend.repository.CutoffRepository
import com.jeongsi.backend.repository.FavoriteRepository
import com.jeongsi.backend.repository.MockApplicationRepository
import com.jeongsi.backend.repository.RecruitmentUnitRepository
import com.jeongsi.backend.repository.ScoreRuleRepository
import com.jeongsi.backend.repository.UniversityRepository
import com.jeongsi.backend.repository.UserScoreRepository
import org.springframework.stereotype.Service

/**
 * 모집단위 카드 조립 + 검색/상세/discover/추천.
 * 모든 카드에 (성적이 있으면) 합격 판정(라벨·확률)을 붙인다.
 */
@Service
class CatalogService(
    private val units: RecruitmentUnitRepository,
    private val universities: UniversityRepository,
    private val rules: ScoreRuleRepository,
    private val cutoffs: CutoffRepository,
    private val userScores: UserScoreRepository,
    private val mockApps: MockApplicationRepository,
    private val favorites: FavoriteRepository,
    private val scoring: ScoringService,
) {
    private val year = 2027

    // ---- 단건/목록 카드 조립 -------------------------------------------------

    fun detail(unitId: Long, deviceDbId: Long?): UnitDetailDto? {
        val unit = units.findById(unitId).orElse(null) ?: return null
        val card = buildCards(listOf(unit), deviceDbId).firstOrNull() ?: return null
        val rule = rules.findByRecruitmentUnitId(unitId)
        val uni = universities.findById(unit.universityId).orElse(null)
        return UnitDetailDto(
            card = card,
            weightKorean = rule?.weightKorean?.toDouble() ?: 0.0,
            weightMath = rule?.weightMath?.toDouble() ?: 0.0,
            weightEnglish = rule?.weightEnglish?.toDouble() ?: 0.0,
            weightInquiry = rule?.weightInquiry?.toDouble() ?: 0.0,
            mathRequired = rule?.mathRequired ?: "any",
            inquiryRequired = rule?.inquiryRequired ?: "any",
            intro = buildIntro(unit, uni),
        )
    }

    /** 검색·필터. 빈 조건이면 전체. */
    fun search(
        deviceDbId: Long?,
        group: String?,
        track: String?,
        region: String?,
        q: String?,
        minCut: Double?,
        maxCut: Double?,
        sort: String?,
        university: String?,
        department: String?,
    ): List<UnitCardDto> {
        var cards = buildCards(units.findAll(), deviceDbId)
        if (group != null) cards = cards.filter { it.recruitGroup == group }
        if (track != null) cards = cards.filter { it.track == track }
        if (region != null) cards = cards.filter { it.university.region == region }
        if (university != null) cards = cards.filter { it.university.name == university }
        if (department != null) cards = cards.filter { it.departmentName == department }
        if (!q.isNullOrBlank()) {
            val needle = q.trim()
            cards = cards.filter {
                it.departmentName.contains(needle) || it.university.name.contains(needle)
            }
        }
        if (minCut != null) cards = cards.filter { it.admission.cutPercentile >= minCut }
        if (maxCut != null) cards = cards.filter { it.admission.cutPercentile <= maxCut }
        // 정렬: fit(내 성적 맞춤=현실적 도전) / competition(경쟁률 낮은순) / popular(배치컷 높은순, 기본)
        return when (sort) {
            "fit" -> cards.sortedWith(
                compareByDescending<UnitCardDto> { it.admission.eligible }.thenByDescending { rankScore(it) },
            )
            "competition" -> cards.sortedBy { it.competitionRate ?: Double.MAX_VALUE }
            else -> cards.sortedByDescending { it.admission.cutPercentile }
        }
    }

    /**
     * ★ 인스타 찾아보기 피드 — 점수 기반 랭킹.
     * 성적 있으면: 합격확률이 적당히 도전적인(소신·적정·안정) 순으로 + 자격 충족 우선.
     * 성적 없으면: 배치컷 높은 인기 순.
     */
    fun discover(deviceDbId: Long?, limit: Int): List<UnitCardDto> {
        val cards = buildCards(units.findAll(), deviceDbId)
        val hasScore = cards.any { it.admission.positionDelta != null }
        val ranked = if (hasScore) {
            cards.sortedWith(
                compareByDescending<UnitCardDto> { it.admission.eligible }
                    .thenByDescending { rankScore(it) },
            )
        } else {
            cards.sortedByDescending { it.admission.cutPercentile }
        }
        return ranked.take(limit)
    }

    /** discover 랭킹 점수: 내 위치(환산−배치컷)가 +1 근처(소신·적정)인 "현실적 도전"을 위로. */
    private fun rankScore(c: UnitCardDto): Double {
        val d = c.admission.positionDelta ?: return -100.0
        // delta=+1을 정점으로 — 너무 안전(델타 큼)하거나 너무 무리(델타 많이 음수)면 후순위
        return 10.0 - kotlin.math.abs(d - 1.0)
    }

    /** 합격 라벨별 추천 — 라벨 코드별로 묶어 반환(안정/적정/소신/상향). */
    fun recommend(deviceDbId: Long?): Map<String, List<UnitCardDto>> {
        val cards = buildCards(units.findAll(), deviceDbId)
            .filter { it.admission.labelCode != null }
        return cards.groupBy { it.admission.labelCode!! }
            .mapValues { (_, v) -> v.sortedByDescending { it.admission.cutPercentile } }
    }

    /** 군별 추천 1개씩(가/나/다) — 상위 후보 중 랜덤(화면 들어올 때마다 변동). */
    fun groupRecommend(deviceDbId: Long?): List<UnitCardDto> {
        val cards = buildCards(units.findAll(), deviceDbId)
        return listOf("GA", "NA", "DA").mapNotNull { g ->
            cards.filter { it.recruitGroup == g }
                .sortedByDescending { rankScore(it) }
                .take(6)
                .randomOrNull()
        }
    }

    /** 전략 추천 — 프리셋(안정형/균형형/소신형)에 맞는 가/나/다 조합. track 필터 선택. */
    fun strategy(deviceDbId: Long?, preset: String, track: String?): com.jeongsi.backend.dto.StrategyDto {
        val (name, desc, labels) = when (preset) {
            "stable" -> Triple("안정형", "합격을 우선 — 안정·안정·적정으로 안전하게.", listOf("SAFE", "SAFE", "MODERATE"))
            "reach" -> Triple("소신형", "상향 도전 — 적정·소신·소신으로 욕심내기.", listOf("MODERATE", "REACH", "REACH"))
            else -> Triple("균형형", "안정과 도전 균형 — 안정·적정·소신 한 장씩.", listOf("SAFE", "MODERATE", "REACH"))
        }
        val cards = buildCards(units.findAll(), deviceDbId)
        val picks = listOf("GA", "NA", "DA").mapIndexedNotNull { i, g ->
            val want = labels[i]
            val pool = cards.filter { it.recruitGroup == g && it.admission.eligible && (track == null || it.track == track) }
            // 원하는 라벨 우선, 없으면 그 라벨에 가장 가까운(positionDelta 기준) 것
            pool.filter { it.admission.labelCode == want }.randomOrNull()
                ?: pool.minByOrNull { kotlin.math.abs((it.admission.positionDelta ?: -99.0) - labelTargetDelta(want)) }
        }
        return com.jeongsi.backend.dto.StrategyDto(preset, name, desc, picks)
    }

    private fun labelTargetDelta(code: String): Double = when (code) {
        "SAFE" -> 4.0; "MODERATE" -> 2.0; "REACH" -> 0.0; "HARD" -> -2.0; else -> -4.0
    }

    /** 전체 대학명(가나다순). 학교 필터용. */
    fun universityNames(): List<String> = universities.findAll().map { it.name }.sorted()

    /** 전체 학과명(중복 제거·가나다순). 학과 필터용. */
    fun departmentNames(): List<String> = units.findAll().map { it.departmentName }.distinct().sorted()

    /** 프리셋 → 가/나/다 목표 라벨. */
    fun presetLabels(preset: String): Triple<String, String, String> = when (preset) {
        "stable" -> Triple("SAFE", "SAFE", "MODERATE")
        "reach" -> Triple("MODERATE", "REACH", "REACH")
        else -> Triple("SAFE", "MODERATE", "REACH")
    }

    /** 군별 목표 라벨에 맞는 조합 여러 개 (페이지네이션). 각 군 풀을 인덱스로 zip. */
    fun strategyCombos(
        deviceDbId: Long?, gaLabel: String, naLabel: String, daLabel: String,
        track: String?, offset: Int, limit: Int,
    ): com.jeongsi.backend.dto.StrategyCombosDto {
        val cards = buildCards(units.findAll(), deviceDbId)
        fun pool(g: String, want: String) = cards
            .filter { it.recruitGroup == g && it.admission.eligible && (track == null || it.track == track) }
            .sortedWith(
                // 원하는 라벨 우선, 그 다음 라벨 근접도, 그 다음 배치컷
                compareByDescending<com.jeongsi.backend.dto.UnitCardDto> { it.admission.labelCode == want }
                    .thenBy { kotlin.math.abs((it.admission.positionDelta ?: -99.0) - labelTargetDelta(want)) },
            )
        // 각 군 상위 풀(최대 8)로 카르테시안 조합 생성 → 다양한 조합 다수.
        val ga = pool("GA", gaLabel).take(8); val na = pool("NA", naLabel).take(8); val da = pool("DA", daLabel).take(8)
        val triples = ArrayList<Triple<Int, Int, Int>>()
        for (i in ga.indices) for (j in na.indices) for (k in da.indices) triples.add(Triple(i, j, k))
        // 안정성 높은 순으로 정렬해 좋은 조합부터
        val ordered = triples.map { (i, j, k) -> listOf(ga[i], na[j], da[k]) }
            .sortedByDescending { ComboMath.stability(it) }
        val total = ordered.size
        val combos = ordered.drop(offset).take(limit).map { picks ->
            com.jeongsi.backend.dto.ComboDto(
                picks = picks,
                probAtLeast1 = ComboMath.pAtLeast1(picks),
                stabilityScore = ComboMath.stability(picks),
                composition = ComboMath.composition(picks),
            )
        }
        return com.jeongsi.backend.dto.StrategyCombosDto(gaLabel, naLabel, daLabel, combos, hasMore = offset + limit < total)
    }

    // ---- 내부: 카드 빌더 (N+1 회피 위해 배치 조회) ---------------------------

    fun buildCards(unitList: List<RecruitmentUnit>, deviceDbId: Long?): List<UnitCardDto> {
        if (unitList.isEmpty()) return emptyList()
        val unitIds = unitList.map { it.id }
        val uniMap = universities.findAllById(unitList.map { it.universityId }).associateBy { it.id }
        val ruleMap = rules.findByRecruitmentUnitIdIn(unitIds).associateBy { it.recruitmentUnitId }
        val cutMap = cutoffs.findByRecruitmentUnitIdInAndYear(unitIds, year).associateBy { it.recruitmentUnitId }
        val score: UserScore? = deviceDbId?.let { userScores.findByDeviceId(it) }
        val favSet = deviceDbId?.let { favorites.findByDeviceId(it).map { f -> f.recruitmentUnitId }.toSet() } ?: emptySet()
        val mockSet = deviceDbId?.let { mockApps.findByDeviceId(it).map { m -> m.recruitmentUnitId }.toSet() } ?: emptySet()

        return unitList.mapNotNull { unit ->
            val uni = uniMap[unit.universityId] ?: return@mapNotNull null
            val rule = ruleMap[unit.id] ?: return@mapNotNull null
            val cut = cutMap[unit.id] ?: return@mapNotNull null
            val v = scoring.evaluate(rule, cut, score)
            UnitCardDto(
                unitId = unit.id,
                university = uniDto(uni),
                departmentName = unit.departmentName,
                admissionType = unit.admissionType,
                recruitGroup = unit.recruitGroup,
                recruitGroupName = Labels.groupName(unit.recruitGroup),
                track = unit.track,
                trackName = Labels.trackName(unit.track),
                field = unit.field,
                quota = unit.quota,
                competitionRate = cut.competitionRate?.toDouble(),
                applicantAvg = cut.applicantAvg?.toDouble(),
                targetAvg = cut.targetAvg?.toDouble(),
                admissionYear = unit.admissionYear,
                reflectAreas = Labels.reflectAreas(
                    rule.weightKorean.toDouble(), rule.weightMath.toDouble(),
                    rule.weightEnglish.toDouble(), rule.weightInquiry.toDouble(), rule.inquiryRequired,
                ),
                indexType = rule.indexType,
                indexName = Labels.indexName(rule.indexType),
                suneungRatio = rule.suneungRatio.toDouble(),
                naesinRatio = rule.naesinRatio.toDouble(),
                admission = AdmissionDto(
                    convertedScore = v.convertedScore,
                    cutPercentile = v.cutPercentile,
                    positionDelta = v.positionDelta,
                    labelCode = v.labelCode,
                    labelName = v.labelName,
                    probability = v.probability,
                    eligible = v.eligible,
                    eligibleReason = v.eligibleReason,
                ),
                isFavorited = unit.id in favSet,
                isMockApplied = unit.id in mockSet,
            )
        }
    }

    private fun uniDto(u: University) = UniversityDto(u.id, u.name, u.region, u.estType, u.logoUrl, u.homepageUrl)

    private fun buildIntro(unit: RecruitmentUnit, uni: University?): String {
        val region = uni?.region ?: ""
        val track = Labels.trackName(unit.track)
        return "${uni?.name ?: ""} ${unit.departmentName} · $region · $track 계열 · 모집 ${unit.quota}명"
    }
}
