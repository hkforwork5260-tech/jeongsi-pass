package com.jeongsi.app.data

import kotlinx.serialization.Serializable

/** 백엔드 DTO 미러. JSON은 snake_case (ApiClient의 Json namingStrategy로 자동 매핑). */

@Serializable
data class UniversityModel(
    val id: Long = 0,
    val name: String = "",
    val region: String = "",
    val estType: String = "private",
    val logoUrl: String? = null,
    val homepageUrl: String? = null,
)

@Serializable
data class AdmissionModel(
    val convertedScore: Double? = null,
    val cutPercentile: Double = 0.0,
    val positionDelta: Double? = null,   // 환산 − 배치컷 (▲양수/▼음수)
    val labelCode: String? = null,       // SAFE/MODERATE/REACH/HARD/RISK
    val labelName: String? = null,       // 안정/적정/소신/상향/위험
    val probability: Int? = null,        // 합격 확률 %
    val eligible: Boolean = true,
    val eligibleReason: String? = null,
)

@Serializable
data class UnitCard(
    val unitId: Long = 0,
    val university: UniversityModel = UniversityModel(),
    val departmentName: String = "",
    val admissionType: String = "",
    val recruitGroup: String = "",
    val recruitGroupName: String = "",
    val track: String = "",
    val trackName: String = "",
    val field: String? = null,
    val quota: Int = 0,
    val competitionRate: Double? = null,
    val applicantAvg: Double? = null,   // 모의지원자 평균
    val targetAvg: Double? = null,      // 목표등록자 평균
    val admissionYear: Int = 2027,
    val reflectAreas: String = "",      // 국수영탐 등
    val indexType: String = "both",
    val indexName: String = "",         // 표준/백분위/표+백
    val suneungRatio: Double = 100.0,
    val naesinRatio: Double = 0.0,
    val admission: AdmissionModel = AdmissionModel(),
    val isFavorited: Boolean = false,
    val isMockApplied: Boolean = false,
)

@Serializable
data class UnitDetail(
    val card: UnitCard = UnitCard(),
    val weightKorean: Double = 0.0,
    val weightMath: Double = 0.0,
    val weightEnglish: Double = 0.0,
    val weightInquiry: Double = 0.0,
    val mathRequired: String = "any",
    val inquiryRequired: String = "any",
    val intro: String = "",
)

@Serializable
data class ScoreModel(
    val examTrack: String? = null,
    val gender: String? = null,
    val gradYear: String? = null,
    val koreanSubject: String? = null,
    val koreanCommon: Int? = null, val koreanSelect: Int? = null, val koreanRaw: Int? = null,
    val koreanStd: Int? = null, val koreanPct: Int? = null, val koreanGrade: Int? = null,
    val mathSubject: String? = null,
    val mathCommon: Int? = null, val mathSelect: Int? = null, val mathRaw: Int? = null,
    val mathStd: Int? = null, val mathPct: Int? = null, val mathGrade: Int? = null,
    val englishRaw: Int? = null, val englishGrade: Int? = null,
    val historyRaw: Int? = null, val historyGrade: Int? = null,
    val inquiry1Subject: String? = null, val inquiry1Type: String? = null, val inquiry1Raw: Int? = null,
    val inquiry1Std: Int? = null, val inquiry1Pct: Int? = null, val inquiry1Grade: Int? = null,
    val inquiry2Subject: String? = null, val inquiry2Type: String? = null, val inquiry2Raw: Int? = null,
    val inquiry2Std: Int? = null, val inquiry2Pct: Int? = null, val inquiry2Grade: Int? = null,
)

@Serializable
data class ReflectTypeRow(
    val reflectType: String = "",
    val areaCount: Int = 0,
    val inquiryCount: Int = 0,
    val stdTotal: Double? = null,
    val stdTopPercent: Double? = null,
    val pctTotal: Double? = null,
    val pctTopPercent: Double? = null,
    val univCount: Int = 0,
    val eligible: Boolean = true,
)

@Serializable
data class AnalysisModel(
    val hasScore: Boolean = false,
    val koreanPct: Int? = null,
    val mathPct: Int? = null,
    val inquiryAvgPct: Int? = null,
    val englishGrade: Int? = null,
    val averagePct: Double? = null,
    val favoredTrack: String? = null,
    val favoredTrackName: String? = null,
    val nationalTopPercent: Double? = null,
    val comment: String = "",
    val reflectTypes: List<ReflectTypeRow> = emptyList(),
    val bestTypes: List<String> = emptyList(),
    val worstTypes: List<String> = emptyList(),
)

@Serializable
data class TargetModel(
    val group: String = "",
    val groupName: String = "",
    val priority: Int = 1,
    val unit: UnitCard = UnitCard(),
)

@Serializable
data class CompareModel(
    val group: String = "",
    val groupName: String = "",
    val units: List<UnitCard> = emptyList(),
    val safest: UnitCard? = null,
    val summary: String = "",
)

@Serializable
data class PopularItem(
    val rank: Int = 0,
    val university: UniversityModel = UniversityModel(),
    val unitId: Long = 0,
    val departmentName: String = "",
    val recruitGroup: String = "",
    val recruitGroupName: String = "",
    val cutPercentile: Double = 0.0,
    val competitionRate: Double? = null,
    val mockApplyCount: Long = 0,
)

/** 연도별 배치컷 추이 (LAG). yoyChange=작년 대비 변화. */
@Serializable
data class CutoffTrend(
    val year: Int = 0,
    val cutPercentile: Double = 0.0,
    val competitionRate: Double? = null,
    val prevCut: Double? = null,
    val yoyChange: Double? = null,
)

/** 지역 내 배치컷 순위 (RANK/PERCENT_RANK). */
@Serializable
data class RegionRank(
    val region: String = "",
    val unitId: Long = 0,
    val universityName: String = "",
    val departmentName: String = "",
    val recruitGroup: String = "",
    val recruitGroupName: String = "",
    val cutPercentile: Double = 0.0,
    val regionRank: Int = 0,
    val regionTotal: Int = 0,
    val topPercent: Double = 0.0,
)

@Serializable
data class ReportGroup(
    val group: String = "",
    val groupName: String = "",
    val units: List<UnitCard> = emptyList(),
)

@Serializable
data class Report(
    val hasScore: Boolean = false,
    val groups: List<ReportGroup> = emptyList(),
    val summary: String = "",
)

@Serializable
data class MockSummary(
    val totalCount: Int = 0,
    val groups: List<ReportGroup> = emptyList(),
    val picks: List<UnitCard> = emptyList(),
    val probNone: Int? = null,
    val probAtLeast1: Int? = null,
    val probAtLeast2: Int? = null,
    val probAll: Int? = null,
    val composition: String = "",
    val summary: String = "",
    val remainingChanges: Map<String, Int> = emptyMap(),
    val stabilityScore: Int? = null,
    val stabilityVerdict: String = "",
    val fillAdvice: String = "",
    val fillCandidate: UnitCard? = null,
    val peerCombo: List<UnitCard> = emptyList(),
)

@Serializable
data class Combo(
    val picks: List<UnitCard> = emptyList(),
    val probAtLeast1: Int? = null,
    val stabilityScore: Int = 0,
    val composition: String = "",
)

@Serializable
data class StrategyCombos(
    val gaLabel: String = "",
    val naLabel: String = "",
    val daLabel: String = "",
    val combos: List<Combo> = emptyList(),
    val hasMore: Boolean = false,
)

@Serializable
data class Strategy(
    val preset: String = "",
    val presetName: String = "",
    val description: String = "",
    val picks: List<UnitCard> = emptyList(),
)

@Serializable
data class Home(
    val admissionYear: Int = 2027,
    val dDay: Int = 0,
    val hasScore: Boolean = false,
    val analysis: AnalysisModel? = null,
    val heroUnits: List<UnitCard> = emptyList(),
    val groupRecommend: List<UnitCard> = emptyList(),
    val popularTop5: List<PopularItem> = emptyList(),
)
