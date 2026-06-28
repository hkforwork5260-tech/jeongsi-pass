package com.jeongsi.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.jeongsi.backend.entity.Cutoff
import com.jeongsi.backend.entity.ScoreRule
import com.jeongsi.backend.entity.UserScore
import com.jeongsi.backend.repository.ScoreDistParamRepository
import org.springframework.stereotype.Service
import kotlin.math.roundToInt

/**
 * 합격 판정 로직 (placeholder, 메가 합격예측 방식 근사).
 *
 * ⚠️ 메가 등의 실제 공식·데이터는 비공개. 여기 계산은 합리적 근사다.
 *   - 환산점수(평균백분위) = 영역 백분위를 대학 반영비율로 가중평균(영어는 등급→점수 환산표).
 *   - 내 위치 = 환산점수 − 배치컷(백분위) → ▲/▼ 점수차.
 *   - 5단계 = 위치 구간(안정/적정/소신/상향/위험).
 *   - 상위누적% = 반영유형별 전국 분포(score_dist_params, 정규분포 가정)의 CDF.
 *   실모델/실데이터 도착 시 이 클래스 + score_dist_params만 교체.
 */
@Service
class ScoringService(
    private val objectMapper: ObjectMapper,
    private val distParams: ScoreDistParamRepository,
    private val predictor: AdmissionPredictor,   // ★ 합격예측 모델(교체 지점)
) {
    data class Verdict(
        val convertedScore: Double?,   // 환산점수(평균백분위 0~100), 성적 없으면 null
        val cutPercentile: Double,     // 배치컷
        val positionDelta: Double?,    // 환산 − 배치컷 (▲양수/▼음수)
        val labelCode: String?,        // SAFE/MODERATE/REACH/HARD/RISK
        val labelName: String?,        // 안정/적정/소신/상향/위험
        val probability: Int?,         // 합격 확률 % (로지스틱, placeholder)
        val eligible: Boolean,
        val eligibleReason: String?,
    )

    /** 한 모집단위 합격 판정. score=null이면 배치컷만. */
    fun evaluate(rule: ScoreRule, cutoff: Cutoff, score: UserScore?): Verdict {
        val cut = cutoff.cutPercentile.toDouble()
        if (score == null || !hasMinimumScore(score)) {
            return Verdict(null, cut, null, null, null, null, eligible = true, eligibleReason = null)
        }
        val (eligible, reason) = checkEligibility(rule, score)
        val converted = convertedScore(rule, score)
        val delta = converted - cut
        // ★ 라벨·확률 산출은 교체가능한 모델(AdmissionPredictor)에 위임
        val pred = predictor.predict(converted, cut)
        val prob = if (eligible) pred.probability else (pred.probability / 4).coerceAtLeast(1)
        return Verdict(round1(converted), cut, round1(delta), pred.labelCode, pred.labelName, prob, eligible, reason)
    }

    /** 환산점수(평균백분위) = Σ(영역 백분위 × 반영비율) / Σ반영비율. 배치컷과 같은 백분위 스케일. */
    fun convertedScore(rule: ScoreRule, score: UserScore): Double {
        val wK = rule.weightKorean.toDouble()
        val wM = rule.weightMath.toDouble()
        val wE = rule.weightEnglish.toDouble()
        val wI = rule.weightInquiry.toDouble()
        val korean = (score.koreanPct ?: 0).toDouble()
        val math = (score.mathPct ?: 0).toDouble()
        val english = englishConverted(rule, score.englishGrade)
        val inquiry = inquiryAvgPct(score, rule.inquiryCount)
        val sumW = (wK + wM + wE + wI).takeIf { it > 0 } ?: 1.0
        return (korean * wK + math * wM + english * wE + inquiry * wI) / sumW
    }

    // ---- 반영유형별 총점 (성적분석용) — 메가 방식 ---------------------------
    // 표준점수 총점: 국 + (수, 반영 시) + 탐(2과목=합 / 1과목=높은1과목×2). 영어 절평 제외.
    // 백분위 총점:   국 + (수, 반영 시) + 탐(2과목=평균 / 1과목=높은1과목).
    fun reflectStdTotal(score: UserScore, includeMath: Boolean, inquiryCount: Int): Double {
        val k = (score.koreanStd ?: 0).toDouble()
        val m = if (includeMath) (score.mathStd ?: 0).toDouble() else 0.0
        val i1 = (score.inquiry1Std ?: 0); val i2 = (score.inquiry2Std ?: 0)
        val inq = if (inquiryCount >= 2) (i1 + i2).toDouble() else maxOf(i1, i2).toDouble() * 2
        return k + m + inq
    }

    fun reflectPctTotal(score: UserScore, includeMath: Boolean, inquiryCount: Int): Double {
        val k = (score.koreanPct ?: 0).toDouble()
        val m = if (includeMath) (score.mathPct ?: 0).toDouble() else 0.0
        val i1 = (score.inquiry1Pct ?: 0); val i2 = (score.inquiry2Pct ?: 0)
        val inq = if (inquiryCount >= 2) (i1 + i2) / 2.0 else maxOf(i1, i2).toDouble()
        return k + m + inq
    }

    /** 상위누적% = (1 − Φ((총점−mean)/stddev)) × 100. 분포 파라미터 없으면 null. */
    fun cumulativeTopPercent(reflectType: String, indexType: String, total: Double): Double? {
        val p = distParams.findByReflectTypeAndIndexType(reflectType, indexType) ?: return null
        val z = (total - p.mean.toDouble()) / p.stddev.toDouble().coerceAtLeast(0.0001)
        val top = (1.0 - normalCdf(z)) * 100.0
        return (top.coerceIn(0.01, 99.99) * 100).roundToInt() / 100.0
    }

    // ---- 내부 -------------------------------------------------------------

    private fun englishConverted(rule: ScoreRule, grade: Int?): Double {
        if (grade == null) return 0.0
        return runCatching {
            val table: Map<String, Int> = objectMapper.readValue(
                rule.englishGradeScore,
                objectMapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Int::class.java),
            )
            table[grade.toString()]?.toDouble() ?: 0.0
        }.getOrDefault(0.0)
    }

    private fun inquiryAvgPct(score: UserScore, inquiryCount: Int): Double {
        val a = score.inquiry1Pct; val b = score.inquiry2Pct
        return when {
            a != null && b != null -> if (inquiryCount >= 2) (a + b) / 2.0 else maxOf(a, b).toDouble()
            a != null -> a.toDouble()
            b != null -> b.toDouble()
            else -> 0.0
        }
    }

    private fun checkEligibility(rule: ScoreRule, score: UserScore): Pair<Boolean, String?> {
        if (rule.mathRequired == "calculus_geometry" && score.mathSubject == "probability") {
            return false to "미적분·기하 지정 (확률과통계 응시는 지원 불가)"
        }
        if (rule.inquiryRequired == "science") {
            val sci = score.inquiry1Type == "science" || score.inquiry2Type == "science"
            if (!sci) return false to "과학탐구 지정 (사회탐구만 응시 시 지원 불가)"
        }
        return true to null
    }

    private fun hasMinimumScore(s: UserScore): Boolean =
        s.koreanPct != null || s.mathPct != null || s.englishGrade != null

    /** 표준정규 누적분포 Φ(z) — Abramowitz & Stegun 근사. */
    private fun normalCdf(z: Double): Double {
        val t = 1.0 / (1.0 + 0.2316419 * kotlin.math.abs(z))
        val d = 0.3989422804014327 * kotlin.math.exp(-z * z / 2.0)
        val p = d * t * (0.319381530 + t * (-0.356563782 + t * (1.781477937 + t * (-1.821255978 + t * 1.330274429))))
        return if (z >= 0) 1.0 - p else p
    }

    private fun round1(v: Double): Double = (v * 10).roundToInt() / 10.0
}
