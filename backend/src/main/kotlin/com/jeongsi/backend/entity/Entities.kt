package com.jeongsi.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.Instant

/** 대학 */
@Entity
@Table(name = "universities")
class University(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false) var name: String = "",
    @Column(nullable = false) var region: String = "",
    @Column(name = "est_type", nullable = false) var estType: String = "private",
    @Column(name = "logo_url") var logoUrl: String? = null,
    @Column(name = "homepage_url") var homepageUrl: String? = null,
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
)

/** 모집단위 = 대학 × 학과 × 전형 × 군 (핵심 아이템) */
@Entity
@Table(name = "recruitment_units")
class RecruitmentUnit(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "university_id", nullable = false) var universityId: Long = 0,
    @Column(name = "department_name", nullable = false) var departmentName: String = "",
    @Column(name = "admission_type", nullable = false) var admissionType: String = "일반전형",
    @Column(name = "recruit_group", nullable = false) var recruitGroup: String = "",  // GA/NA/DA/OUT
    @Column(nullable = false) var track: String = "",                                  // humanities/natural/arts
    @Column var field: String? = null,
    @Column(nullable = false) var quota: Int = 0,
    @Column(name = "admission_year", nullable = false) var admissionYear: Int = 2027,
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),
)

/** 대학별 환산 규칙 (반영비율 + 가산/지정 + 영어 환산표) */
@Entity
@Table(name = "score_rules")
class ScoreRule(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "recruitment_unit_id", nullable = false) var recruitmentUnitId: Long = 0,
    @Column(name = "weight_korean", nullable = false) var weightKorean: BigDecimal = BigDecimal.ZERO,
    @Column(name = "weight_math", nullable = false) var weightMath: BigDecimal = BigDecimal.ZERO,
    @Column(name = "weight_english", nullable = false) var weightEnglish: BigDecimal = BigDecimal.ZERO,
    @Column(name = "weight_inquiry", nullable = false) var weightInquiry: BigDecimal = BigDecimal.ZERO,
    @Column(name = "math_required", nullable = false) var mathRequired: String = "any",
    @Column(name = "inquiry_required", nullable = false) var inquiryRequired: String = "any",
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "english_grade_score", nullable = false) var englishGradeScore: String = "{}",
    @Column(name = "base_score", nullable = false) var baseScore: BigDecimal = BigDecimal(100),
    @Column(name = "index_type", nullable = false) var indexType: String = "both",      // std/pct/both 활용지표
    @Column(name = "inquiry_count", nullable = false) var inquiryCount: Int = 2,          // 반영 탐구 과목 수
    @Column(name = "suneung_ratio", nullable = false) var suneungRatio: BigDecimal = BigDecimal(100),
    @Column(name = "naesin_ratio", nullable = false) var naesinRatio: BigDecimal = BigDecimal.ZERO,
    @Column(name = "etc_ratio", nullable = false) var etcRatio: BigDecimal = BigDecimal.ZERO,
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
)

/** 배치컷·지표 (연도별) */
@Entity
@Table(name = "cutoffs")
class Cutoff(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "recruitment_unit_id", nullable = false) var recruitmentUnitId: Long = 0,
    @Column(nullable = false) var year: Int = 2027,
    @Column(name = "cut_percentile", nullable = false) var cutPercentile: BigDecimal = BigDecimal.ZERO,
    @Column(name = "competition_rate") var competitionRate: BigDecimal? = null,
    @Column(name = "applicant_avg") var applicantAvg: BigDecimal? = null,
    @Column(name = "target_avg") var targetAvg: BigDecimal? = null,
    @Column(nullable = false) var source: String = "dummy",
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
)

/** 익명 기기 (X-Device-Id) */
@Entity
@Table(name = "devices")
class Device(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "device_id", nullable = false) var deviceId: String = "",
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
)

/** 내 수능 성적 (기기당 1행) */
@Entity
@Table(name = "user_scores")
class UserScore(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "device_id", nullable = false) var deviceId: Long = 0,
    @Column(name = "korean_subject") var koreanSubject: String? = null,
    @Column(name = "korean_std") var koreanStd: Int? = null,
    @Column(name = "korean_pct") var koreanPct: Int? = null,
    @Column(name = "korean_grade") var koreanGrade: Int? = null,
    @Column(name = "math_subject") var mathSubject: String? = null,
    @Column(name = "math_std") var mathStd: Int? = null,
    @Column(name = "math_pct") var mathPct: Int? = null,
    @Column(name = "math_grade") var mathGrade: Int? = null,
    @Column(name = "english_grade") var englishGrade: Int? = null,
    @Column(name = "history_grade") var historyGrade: Int? = null,
    @Column(name = "inquiry1_subject") var inquiry1Subject: String? = null,
    @Column(name = "inquiry1_type") var inquiry1Type: String? = null,
    @Column(name = "inquiry1_std") var inquiry1Std: Int? = null,
    @Column(name = "inquiry1_pct") var inquiry1Pct: Int? = null,
    @Column(name = "inquiry1_grade") var inquiry1Grade: Int? = null,
    @Column(name = "inquiry2_subject") var inquiry2Subject: String? = null,
    @Column(name = "inquiry2_type") var inquiry2Type: String? = null,
    @Column(name = "inquiry2_std") var inquiry2Std: Int? = null,
    @Column(name = "inquiry2_pct") var inquiry2Pct: Int? = null,
    @Column(name = "inquiry2_grade") var inquiry2Grade: Int? = null,
    // 필수정보
    @Column(name = "exam_track") var examTrack: String? = null,   // humanities/natural
    @Column(name = "gender") var gender: String? = null,
    @Column(name = "grad_year") var gradYear: String? = null,
    // 원점수(공통/선택 분리)
    @Column(name = "korean_common") var koreanCommon: Int? = null,
    @Column(name = "korean_select") var koreanSelect: Int? = null,
    @Column(name = "korean_raw") var koreanRaw: Int? = null,
    @Column(name = "math_common") var mathCommon: Int? = null,
    @Column(name = "math_select") var mathSelect: Int? = null,
    @Column(name = "math_raw") var mathRaw: Int? = null,
    @Column(name = "english_raw") var englishRaw: Int? = null,
    @Column(name = "history_raw") var historyRaw: Int? = null,
    @Column(name = "inquiry1_raw") var inquiry1Raw: Int? = null,
    @Column(name = "inquiry2_raw") var inquiry2Raw: Int? = null,
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),
)

/** 목표 대학 (군별 1지망/2지망) */
@Entity
@Table(name = "target_units")
class TargetUnit(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "device_id", nullable = false) var deviceId: Long = 0,
    @Column(name = "recruitment_unit_id", nullable = false) var recruitmentUnitId: Long = 0,
    @Column(name = "recruit_group", nullable = false) var recruitGroup: String = "",
    @Column(nullable = false) var priority: Int = 1,
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
)

/** 실제 지원 대학 (군별 1개, 하루 3번 변경 제한) */
@Entity
@Table(name = "actual_applications")
class ActualApplication(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "device_id", nullable = false) var deviceId: Long = 0,
    @Column(name = "recruit_group", nullable = false) var recruitGroup: String = "",
    @Column(name = "recruitment_unit_id", nullable = false) var recruitmentUnitId: Long = 0,
    @Column(name = "change_count", nullable = false) var changeCount: Int = 0,
    @Column(name = "change_date") var changeDate: java.time.LocalDate? = null,
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),
)

/** 반영유형별 전국 분포 파라미터 (상위누적% 산출용) */
@Entity
@Table(name = "score_dist_params")
class ScoreDistParam(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "reflect_type", nullable = false) var reflectType: String = "",
    @Column(name = "index_type", nullable = false) var indexType: String = "",
    @Column(nullable = false) var mean: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false) var stddev: BigDecimal = BigDecimal.ONE,
    @Column(name = "max_score", nullable = false) var maxScore: BigDecimal = BigDecimal.ZERO,
)

/** 모의지원 */
@Entity
@Table(name = "mock_applications")
class MockApplication(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "device_id", nullable = false) var deviceId: Long = 0,
    @Column(name = "recruitment_unit_id", nullable = false) var recruitmentUnitId: Long = 0,
    @Column(name = "recruit_group", nullable = false) var recruitGroup: String = "",
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
)

/** 관심 학과 */
@Entity
@Table(name = "favorites")
class Favorite(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "device_id", nullable = false) var deviceId: Long = 0,
    @Column(name = "recruitment_unit_id", nullable = false) var recruitmentUnitId: Long = 0,
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
)
