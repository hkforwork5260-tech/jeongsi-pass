package com.jeongsi.backend.service

import com.jeongsi.backend.dto.AnalysisDto
import com.jeongsi.backend.dto.Labels
import com.jeongsi.backend.dto.ReflectTypeRow
import com.jeongsi.backend.entity.UserScore
import com.jeongsi.backend.repository.RecruitmentUnitRepository
import com.jeongsi.backend.repository.ScoreRuleRepository
import com.jeongsi.backend.repository.UserScoreRepository
import org.springframework.stereotype.Service
import kotlin.math.roundToInt

/**
 * 성적 분석 (심화) — 메가 "성적 분석" 재현.
 *  - 반영유형별(국수영탐/과/사/국영탐 × 탐1·2) 내 총점(표준·백분위) + 전국 상위누적%.
 *  - 유불리 분석: 상위누적%가 낮을수록(전국에서 앞설수록) 유리 → TOP3 유리/불리.
 */
@Service
class AnalysisService(
    private val userScores: UserScoreRepository,
    private val units: RecruitmentUnitRepository,
    private val rules: ScoreRuleRepository,
    private val scoring: ScoringService,
) {
    private data class TypeSpec(val reflectType: String, val includeMath: Boolean, val inquiryCount: Int, val areaCount: Int)

    // 표에 보일 반영유형 (메가 표와 유사). areaCount = 반영 영역 수.
    private val specs = listOf(
        TypeSpec("국수영탐", true, 2, 4),
        TypeSpec("국수영탐", true, 1, 4),
        TypeSpec("국수영과", true, 2, 4),
        TypeSpec("국수영과", true, 1, 4),
        TypeSpec("국수영사", true, 2, 4),
        TypeSpec("국영탐", false, 2, 3),
    )

    fun analysis(deviceDbId: Long): AnalysisDto {
        val s = userScores.findByDeviceId(deviceDbId)
            ?: return AnalysisDto(false, null, null, null, null, null, null, null, null, "성적을 입력하면 분석이 제공됩니다.")

        val inquiryAvg = listOfNotNull(s.inquiry1Pct, s.inquiry2Pct).takeIf { it.isNotEmpty() }?.average()?.roundToInt()
        val parts = listOfNotNull(s.koreanPct, s.mathPct, inquiryAvg)
        val avg = parts.takeIf { it.isNotEmpty() }?.average()
        val favored = when {
            s.mathPct != null && s.koreanPct != null -> if (s.mathPct!! >= s.koreanPct!!) "natural" else "humanities"
            else -> null
        }
        val topPct = avg?.let { ((100.0 - it) * 10).roundToInt() / 10.0 }

        // 반영유형별 행 계산
        val univCounts = countUnitsByReflectType()
        val rows = specs.map { spec ->
            val eligible = eligibleFor(spec.reflectType, s)
            val stdTotal = scoring.reflectStdTotal(s, spec.includeMath, spec.inquiryCount)
            val pctTotal = scoring.reflectPctTotal(s, spec.includeMath, spec.inquiryCount)
            ReflectTypeRow(
                reflectType = spec.reflectType,
                areaCount = spec.areaCount,
                inquiryCount = spec.inquiryCount,
                stdTotal = round1(stdTotal),
                stdTopPercent = if (eligible) scoring.cumulativeTopPercent(spec.reflectType, "std", stdTotal) else null,
                pctTotal = round1(pctTotal),
                pctTopPercent = if (eligible) scoring.cumulativeTopPercent(spec.reflectType, "pct", pctTotal) else null,
                univCount = univCounts[spec.reflectType] ?: 0,
                eligible = eligible,
            )
        }

        // 유불리: 지원 가능 행을 백분위 상위누적%로 정렬(낮을수록 유리)
        val ranked = rows.filter { it.eligible && it.pctTopPercent != null }
            .sortedBy { it.pctTopPercent }
        val label = { r: ReflectTypeRow -> "${r.reflectType}(탐${r.inquiryCount})" }
        val best = ranked.take(3).map(label)
        val worst = ranked.takeLast(3).reversed().map(label)

        val comment = buildString {
            if (avg != null) append("국·수·탐 평균 백분위 ${avg.roundToInt()}. ")
            if (best.isNotEmpty()) append("가장 유리한 반영유형은 ${best.first()}.")
            if (isEmpty()) append("일부 영역만 입력되었습니다.")
        }

        return AnalysisDto(
            hasScore = true,
            koreanPct = s.koreanPct, mathPct = s.mathPct, inquiryAvgPct = inquiryAvg, englishGrade = s.englishGrade,
            averagePct = avg?.let { (it * 10).roundToInt() / 10.0 },
            favoredTrack = favored, favoredTrackName = favored?.let { Labels.trackName(it) },
            nationalTopPercent = topPct,
            comment = comment,
            reflectTypes = rows,
            bestTypes = best,
            worstTypes = worst,
        )
    }

    private fun eligibleFor(reflectType: String, s: UserScore): Boolean = when (reflectType) {
        "국수영과" -> s.inquiry1Type == "science" || s.inquiry2Type == "science"
        "국수영사" -> s.inquiry1Type == "social" || s.inquiry2Type == "social"
        else -> true
    }

    /** 모집단위를 반영유형으로 분류해 개수 집계. */
    private fun countUnitsByReflectType(): Map<String, Int> {
        val ruleMap = rules.findAll().associateBy { it.recruitmentUnitId }
        return units.findAll().mapNotNull { u ->
            val r = ruleMap[u.id] ?: return@mapNotNull null
            when {
                r.weightMath.toDouble() == 0.0 -> "국영탐"
                r.inquiryRequired == "science" -> "국수영과"
                r.inquiryRequired == "social" -> "국수영사"
                else -> "국수영탐"
            }
        }.groupingBy { it }.eachCount()
    }

    private fun round1(v: Double): Double = (v * 10).roundToInt() / 10.0
}
