package com.jeongsi.backend.service

import com.jeongsi.backend.dto.UnitCardDto
import kotlin.math.roundToInt

/** 가/나/다 조합 분석 공통 계산 (독립 확률 가정, placeholder). */
object ComboMath {
    private fun probs(picks: List<UnitCardDto>): List<Double> =
        picks.mapNotNull { it.admission.probability?.let { p -> p / 100.0 } }

    /** 한 곳 이상 합격 % = 1 − Π(1−pᵢ). 확률 없으면 null. */
    fun pAtLeast1(picks: List<UnitCardDto>): Int? {
        val ps = probs(picks)
        if (ps.isEmpty()) return null
        val none = ps.fold(1.0) { acc, p -> acc * (1 - p) }
        return ((1 - none) * 100).roundToInt().coerceIn(1, 99)
    }

    fun pAtLeast2(picks: List<UnitCardDto>): Int? {
        val ps = probs(picks)
        if (ps.size < 2) return if (ps.isEmpty()) null else 0
        val none = ps.fold(1.0) { acc, p -> acc * (1 - p) }
        val exactly1 = ps.indices.sumOf { i -> ps[i] * ps.filterIndexed { j, _ -> j != i }.fold(1.0) { a, p -> a * (1 - p) } }
        return ((1 - none - exactly1).coerceAtLeast(0.0) * 100).roundToInt().coerceIn(0, 99)
    }

    fun pAll(picks: List<UnitCardDto>): Int? {
        val ps = probs(picks)
        if (ps.isEmpty()) return null
        return (ps.fold(1.0) { acc, p -> acc * p } * 100).roundToInt().coerceIn(1, 99)
    }

    fun pNone(picks: List<UnitCardDto>): Int? {
        val ps = probs(picks)
        if (ps.isEmpty()) return null
        return (ps.fold(1.0) { acc, p -> acc * (1 - p) } * 100).roundToInt().coerceIn(1, 99)
    }

    private fun labelSafety(code: String?): Double = when (code) {
        "SAFE" -> 1.0; "MODERATE" -> 0.75; "REACH" -> 0.5; "HARD" -> 0.25; "RISK" -> 0.1; else -> 0.5
    }

    /** 조합 안정성 지수 0~100 = (한곳이상합격 0.5 + 평균 안전도 0.5) × (채운 군 / 3). */
    fun stability(picks: List<UnitCardDto>): Int {
        if (picks.isEmpty()) return 0
        val p1 = (pAtLeast1(picks) ?: 0) / 100.0
        val safety = picks.map { labelSafety(it.admission.labelCode) }.average()
        val filledRatio = picks.size / 3.0
        return ((p1 * 0.5 + safety * 0.5) * filledRatio * 100).roundToInt().coerceIn(0, 100)
    }

    fun verdict(score: Int): String = when {
        score >= 80 -> "매우 안정적인 조합이에요. 한 곳쯤 상향을 노려봐도 좋아요."
        score >= 60 -> "안정적인 조합이에요."
        score >= 40 -> "안정과 도전이 균형 잡혔어요."
        score >= 20 -> "도전적인 조합이에요. 안정권을 보강해보세요."
        else -> "매우 공격적이에요. 합격 안정성을 위해 안정권을 추가하세요."
    }

    fun composition(picks: List<UnitCardDto>): String {
        val byName = picks.mapNotNull { it.admission.labelName }.groupingBy { it }.eachCount()
        return listOf("안정", "적정", "소신", "상향", "위험").mapNotNull { l -> byName[l]?.let { "$l ${it}개" } }
            .joinToString(" · ")
    }
}
