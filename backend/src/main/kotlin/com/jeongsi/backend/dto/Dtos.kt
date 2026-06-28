package com.jeongsi.backend.dto

/** 군/계열/활용지표 코드 ↔ 한글 매핑 + 반영영역 문자열. */
object Labels {
    fun groupName(code: String) = when (code) {
        "GA" -> "가군"; "NA" -> "나군"; "DA" -> "다군"; "OUT" -> "군외"; else -> code
    }
    fun trackName(code: String) = when (code) {
        "humanities" -> "인문"; "natural" -> "자연"; "arts" -> "예체능"; else -> code
    }
    fun indexName(code: String) = when (code) {
        "std" -> "표준"; "pct" -> "백분위"; "both" -> "표+백"; else -> code
    }

    /** 반영영역 문자열: 가중치>0 영역 조합 + 탐구 지정(과/사). 예: 국수영탐 / 국수영과 / 국영탐 */
    fun reflectAreas(
        wK: Double, wM: Double, wE: Double, wI: Double, inquiryRequired: String,
    ): String = buildString {
        if (wK > 0) append("국")
        if (wM > 0) append("수")
        if (wE > 0) append("영")
        if (wI > 0) append(if (inquiryRequired == "science") "과" else if (inquiryRequired == "social") "사" else "탐")
    }
}

data class UniversityDto(
    val id: Long, val name: String, val region: String, val estType: String,
    val logoUrl: String?, val homepageUrl: String? = null,
)

/** 합격 판정(카드/상세 임베드). 성적 미입력 시 label/positionDelta는 null. */
data class AdmissionDto(
    val convertedScore: Double?,   // 환산점수(평균백분위)
    val cutPercentile: Double,     // 배치컷
    val positionDelta: Double?,    // 환산 − 배치컷 (▲양수/▼음수)
    val labelCode: String?,        // SAFE/MODERATE/REACH/HARD/RISK
    val labelName: String?,        // 안정/적정/소신/상향/위험
    val probability: Int?,         // 합격 확률 %
    val eligible: Boolean,
    val eligibleReason: String?,
)

/** 모집단위 카드 (검색·discover·추천·홈·리포트 공용) */
data class UnitCardDto(
    val unitId: Long,
    val university: UniversityDto,
    val departmentName: String,
    val admissionType: String,
    val recruitGroup: String,
    val recruitGroupName: String,
    val track: String,
    val trackName: String,
    val field: String?,
    val quota: Int,
    val competitionRate: Double?,
    val applicantAvg: Double?,     // 모의지원자 평균
    val targetAvg: Double?,        // 목표등록자 평균
    val admissionYear: Int,
    // 반영 정보
    val reflectAreas: String,      // 국수영탐 등
    val indexType: String,         // std/pct/both
    val indexName: String,         // 표준/백분위/표+백
    val suneungRatio: Double,      // 수능 반영비율 %
    val naesinRatio: Double,       // 내신 %
    val admission: AdmissionDto,
    val isFavorited: Boolean = false,
    val isMockApplied: Boolean = false,
)

data class UnitDetailDto(
    val card: UnitCardDto,
    val weightKorean: Double,
    val weightMath: Double,
    val weightEnglish: Double,
    val weightInquiry: Double,
    val mathRequired: String,
    val inquiryRequired: String,
    val intro: String,
)

/** 내 수능 성적 (입력/조회 공용) */
data class ScoreDto(
    // 필수정보
    val examTrack: String? = null,   // humanities/natural
    val gender: String? = null,
    val gradYear: String? = null,
    // 국어
    val koreanSubject: String? = null,
    val koreanCommon: Int? = null, val koreanSelect: Int? = null, val koreanRaw: Int? = null,
    val koreanStd: Int? = null, val koreanPct: Int? = null, val koreanGrade: Int? = null,
    // 수학
    val mathSubject: String? = null,
    val mathCommon: Int? = null, val mathSelect: Int? = null, val mathRaw: Int? = null,
    val mathStd: Int? = null, val mathPct: Int? = null, val mathGrade: Int? = null,
    // 영어/한국사
    val englishRaw: Int? = null, val englishGrade: Int? = null,
    val historyRaw: Int? = null, val historyGrade: Int? = null,
    // 탐구 1·2
    val inquiry1Subject: String? = null, val inquiry1Type: String? = null,
    val inquiry1Raw: Int? = null, val inquiry1Std: Int? = null, val inquiry1Pct: Int? = null, val inquiry1Grade: Int? = null,
    val inquiry2Subject: String? = null, val inquiry2Type: String? = null,
    val inquiry2Raw: Int? = null, val inquiry2Std: Int? = null, val inquiry2Pct: Int? = null, val inquiry2Grade: Int? = null,
)

/** 반영유형별 상위누적% 한 행 (성적분석) */
data class ReflectTypeRow(
    val reflectType: String,    // 국수영탐 / 국수영과 / 국수영사 / 국영탐
    val areaCount: Int,         // 반영 영역 수
    val inquiryCount: Int,      // 반영 탐구 수
    val stdTotal: Double?,      // 표준점수 총점
    val stdTopPercent: Double?, // 표준 기준 상위누적%
    val pctTotal: Double?,      // 백분위 총점
    val pctTopPercent: Double?, // 백분위 기준 상위누적%
    val univCount: Int,         // 이 유형으로 지원 가능한 대학(모집단위) 수
    val eligible: Boolean,      // 내 응시과목으로 이 유형 지원 가능?
)

/** 성적 분석 (심화) */
data class AnalysisDto(
    val hasScore: Boolean,
    val koreanPct: Int?, val mathPct: Int?, val inquiryAvgPct: Int?, val englishGrade: Int?,
    val averagePct: Double?,
    val favoredTrack: String?, val favoredTrackName: String?,
    val nationalTopPercent: Double?,
    val comment: String,
    val reflectTypes: List<ReflectTypeRow> = emptyList(),
    val bestTypes: List<String> = emptyList(),   // 유리 조합 TOP3(백분위 상위누적% 기준)
    val worstTypes: List<String> = emptyList(),  // 불리 조합 TOP3
)

data class PopularItemDto(
    val rank: Int, val university: UniversityDto, val unitId: Long, val departmentName: String,
    val recruitGroup: String, val recruitGroupName: String,
    val cutPercentile: Double, val competitionRate: Double?, val mockApplyCount: Long,
)

/** 지역 내 배치컷 순위 — 윈도우 함수 RANK/PERCENT_RANK 쇼케이스 */
data class RegionRankDto(
    val region: String, val unitId: Long, val universityName: String, val departmentName: String,
    val recruitGroup: String, val recruitGroupName: String,
    val cutPercentile: Double,
    val regionRank: Int,        // 지역 내 등수 (RANK)
    val regionTotal: Int,       // 지역 내 모집단위 수 (COUNT OVER)
    val topPercent: Double,     // 지역 내 상위 % (PERCENT_RANK)
)

/** 연도별 배치컷 추이 한 행 — CTE + LAG 쇼케이스 (작년 대비 변화) */
data class CutoffTrendDto(
    val year: Int, val cutPercentile: Double, val competitionRate: Double?,
    val prevCut: Double?,       // 작년 배치컷 (LAG)
    val yoyChange: Double?,     // 작년 대비 변화 (▲/▼)
)

data class ReportGroupDto(
    val group: String, val groupName: String, val units: List<UnitCardDto>,
)

data class ReportDto(
    val hasScore: Boolean, val groups: List<ReportGroupDto>, val summary: String,
)

/** 비교분석 리포트 (한 군 내 모의지원/목표 대학 비교) */
data class CompareDto(
    val group: String, val groupName: String,
    val units: List<UnitCardDto>,
    val safest: UnitCardDto?,   // 가장 안정적(positionDelta 최대)
    val summary: String,
)

/** 모의지원 탭 — 목록 + 가/나/다 조합 + 합격 확률 분석 */
data class MockSummaryDto(
    val totalCount: Int,
    val groups: List<ReportGroupDto>,        // 군별 모의지원 목록
    val picks: List<UnitCardDto>,            // 배치된 가/나/다 조합(1지망)
    val probNone: Int?,                      // 전부 불합격 %
    val probAtLeast1: Int?,                  // 1곳 이상 합격 %
    val probAtLeast2: Int?,                  // 2곳 이상 합격 %
    val probAll: Int?,                       // 3곳 모두 합격 %
    val composition: String,                 // "안정 1 · 적정 1 · 소신 1"
    val summary: String,
    val remainingChanges: Map<String, Int> = emptyMap(),  // 군별 오늘 남은 변경 횟수
    // ⑥b 분석
    val stabilityScore: Int? = null,         // 조합 안정성 지수 0~100
    val stabilityVerdict: String = "",       // 한줄 평
    val fillAdvice: String = "",             // 빈 군 채우기·조정 조언
    val fillCandidate: UnitCardDto? = null,  // 추천 후보(빈 군/조정용)
    val peerCombo: List<UnitCardDto> = emptyList(),  // 비슷한 성적대 인기 조합(군별)
)

/** 전략 조합 1개 (가/나/다) */
data class ComboDto(
    val picks: List<UnitCardDto>,
    val probAtLeast1: Int?,
    val stabilityScore: Int,
    val composition: String,
)

/** 전략 조합 목록 (페이지네이션) */
data class StrategyCombosDto(
    val gaLabel: String, val naLabel: String, val daLabel: String,
    val combos: List<ComboDto>,
    val hasMore: Boolean,
)

/** 전략 추천 (프리셋 → 가/나/다 조합) */
data class StrategyDto(
    val preset: String,
    val presetName: String,
    val description: String,
    val picks: List<UnitCardDto>,            // 가/나/다 추천
)

/** 목표 대학 */
data class TargetDto(
    val group: String, val groupName: String, val priority: Int, val unit: UnitCardDto,
)

data class HomeDto(
    val admissionYear: Int,
    @get:com.fasterxml.jackson.annotation.JsonProperty("d_day")
    val dDay: Int,
    val hasScore: Boolean,
    val analysis: AnalysisDto?,
    val heroUnits: List<UnitCardDto>,
    val groupRecommend: List<UnitCardDto>,
    val popularTop5: List<PopularItemDto>,
)
