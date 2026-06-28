package com.jeongsi.backend.service

import com.jeongsi.backend.dto.ScoreDto
import com.jeongsi.backend.entity.UserScore
import com.jeongsi.backend.repository.UserScoreRepository
import org.springframework.stereotype.Service
import java.time.Instant

/** 내 수능 성적 입력/조회 (기기당 1행 upsert). 분석은 AnalysisService. */
@Service
class ScoreService(private val userScores: UserScoreRepository) {

    fun save(deviceDbId: Long, dto: ScoreDto): ScoreDto {
        val s = userScores.findByDeviceId(deviceDbId) ?: UserScore(deviceId = deviceDbId)
        // 필수정보
        s.examTrack = dto.examTrack; s.gender = dto.gender; s.gradYear = dto.gradYear
        // 국어
        s.koreanSubject = dto.koreanSubject
        s.koreanCommon = dto.koreanCommon; s.koreanSelect = dto.koreanSelect; s.koreanRaw = dto.koreanRaw
        s.koreanStd = dto.koreanStd; s.koreanPct = dto.koreanPct; s.koreanGrade = dto.koreanGrade
        // 수학
        s.mathSubject = dto.mathSubject
        s.mathCommon = dto.mathCommon; s.mathSelect = dto.mathSelect; s.mathRaw = dto.mathRaw
        s.mathStd = dto.mathStd; s.mathPct = dto.mathPct; s.mathGrade = dto.mathGrade
        // 영어/한국사
        s.englishRaw = dto.englishRaw; s.englishGrade = dto.englishGrade
        s.historyRaw = dto.historyRaw; s.historyGrade = dto.historyGrade
        // 탐구
        s.inquiry1Subject = dto.inquiry1Subject; s.inquiry1Type = dto.inquiry1Type
        s.inquiry1Raw = dto.inquiry1Raw; s.inquiry1Std = dto.inquiry1Std; s.inquiry1Pct = dto.inquiry1Pct; s.inquiry1Grade = dto.inquiry1Grade
        s.inquiry2Subject = dto.inquiry2Subject; s.inquiry2Type = dto.inquiry2Type
        s.inquiry2Raw = dto.inquiry2Raw; s.inquiry2Std = dto.inquiry2Std; s.inquiry2Pct = dto.inquiry2Pct; s.inquiry2Grade = dto.inquiry2Grade
        s.updatedAt = Instant.now()
        return toDto(userScores.save(s))
    }

    fun get(deviceDbId: Long): ScoreDto? = userScores.findByDeviceId(deviceDbId)?.let { toDto(it) }

    private fun toDto(s: UserScore) = ScoreDto(
        examTrack = s.examTrack, gender = s.gender, gradYear = s.gradYear,
        koreanSubject = s.koreanSubject, koreanCommon = s.koreanCommon, koreanSelect = s.koreanSelect, koreanRaw = s.koreanRaw,
        koreanStd = s.koreanStd, koreanPct = s.koreanPct, koreanGrade = s.koreanGrade,
        mathSubject = s.mathSubject, mathCommon = s.mathCommon, mathSelect = s.mathSelect, mathRaw = s.mathRaw,
        mathStd = s.mathStd, mathPct = s.mathPct, mathGrade = s.mathGrade,
        englishRaw = s.englishRaw, englishGrade = s.englishGrade, historyRaw = s.historyRaw, historyGrade = s.historyGrade,
        inquiry1Subject = s.inquiry1Subject, inquiry1Type = s.inquiry1Type, inquiry1Raw = s.inquiry1Raw,
        inquiry1Std = s.inquiry1Std, inquiry1Pct = s.inquiry1Pct, inquiry1Grade = s.inquiry1Grade,
        inquiry2Subject = s.inquiry2Subject, inquiry2Type = s.inquiry2Type, inquiry2Raw = s.inquiry2Raw,
        inquiry2Std = s.inquiry2Std, inquiry2Pct = s.inquiry2Pct, inquiry2Grade = s.inquiry2Grade,
    )
}
