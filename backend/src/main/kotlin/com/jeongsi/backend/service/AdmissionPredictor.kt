package com.jeongsi.backend.service

import org.springframework.stereotype.Service
import kotlin.math.exp
import kotlin.math.roundToInt

/**
 * ★ 합격예측 "모델"의 교체 지점 (전략 패턴).
 *
 * 환산점수 + 배치컷 → 합격 라벨(5단계) + 합격 확률(%)을 산출하는 단 하나의 책임.
 * 환산점수 계산·지원자격·상위누적% 같은 공용 로직은 [ScoringService]에 남고,
 * "예측 방식" 자체만 이 인터페이스로 분리했다.
 *
 * 실서비스 전환 시 두 갈래 모두 이 한 곳에서 끝난다:
 *   (1) 회사 방식이 "환산점수 vs 배치컷"과 같으면 → 데이터(cutoffs/score_rules)만 교체. 기본 구현 그대로.
 *   (2) 회사가 자체 모델(ML 등)을 쓰면 → 이 인터페이스를 구현한 빈을 하나 추가하면 자동 주입된다.
 *       (@Primary 또는 기본 구현 제거로 교체)
 */
interface AdmissionPredictor {
    fun predict(convertedScore: Double, cutPercentile: Double): Prediction

    data class Prediction(
        val labelCode: String,   // SAFE/MODERATE/REACH/HARD/RISK
        val labelName: String,   // 안정/적정/소신/상향/위험
        val probability: Int,    // 합격 확률 % (1~99)
    )
}

/**
 * 기본 구현 (placeholder, 메가 합격예측 방식 근사).
 *  - 내 위치 = 환산점수 − 배치컷 → 5단계 라벨.
 *  - 확률 = 내 위치 기반 로지스틱(delta=0 → 50%, +3 → ~88%, −3 → ~12%).
 * ⚠️ 실제 공식은 비공개. 실모델 도착 시 이 클래스만 갈아끼우면 됨.
 */
@Service
class PlaceholderAdmissionPredictor : AdmissionPredictor {
    override fun predict(convertedScore: Double, cutPercentile: Double): AdmissionPredictor.Prediction {
        val delta = convertedScore - cutPercentile
        val (code, name) = label5(delta)
        return AdmissionPredictor.Prediction(code, name, probability(delta))
    }

    /** 5단계: 환산−배치컷 구간. 경계는 placeholder. */
    private fun label5(delta: Double): Pair<String, String> = when {
        delta >= 3.0 -> "SAFE" to "안정"
        delta >= 1.0 -> "MODERATE" to "적정"
        delta >= -1.0 -> "REACH" to "소신"
        delta >= -3.0 -> "HARD" to "상향"
        else -> "RISK" to "위험"
    }

    /** 합격 확률% = 로지스틱(내 위치). placeholder. */
    private fun probability(delta: Double): Int {
        val p = 1.0 / (1.0 + exp(-delta / 1.5))
        return (p * 100).roundToInt().coerceIn(1, 99)
    }
}
