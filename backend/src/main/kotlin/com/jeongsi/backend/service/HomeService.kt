package com.jeongsi.backend.service

import com.jeongsi.backend.dto.HomeDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** 홈 대시보드 묶음. */
@Service
class HomeService(
    private val catalog: CatalogService,
    private val analysisService: AnalysisService,
    private val stats: StatsService,
    private val userScores: com.jeongsi.backend.repository.UserScoreRepository,
    @Value("\${jeongsi.suneung-date}") private val suneungDate: String,
    @Value("\${jeongsi.admission-year}") private val admissionYear: Int,
) {
    private val kst = ZoneId.of("Asia/Seoul")

    fun home(deviceDbId: Long?): HomeDto {
        val hasScore = deviceDbId != null && userScores.findByDeviceId(deviceDbId) != null
        val analysis = if (hasScore) analysisService.analysis(deviceDbId!!) else null
        return HomeDto(
            admissionYear = admissionYear,
            dDay = dDay(),
            hasScore = hasScore,
            analysis = analysis,
            heroUnits = catalog.discover(deviceDbId, limit = 5),
            groupRecommend = catalog.groupRecommend(deviceDbId),
            popularTop5 = stats.popular(band = null, track = null, group = null, limit = 5),
        )
    }

    /** 수능일까지 남은 일수 (KST 기준). */
    fun dDay(): Int {
        val today = LocalDate.now(kst)
        val target = LocalDate.parse(suneungDate)
        return ChronoUnit.DAYS.between(today, target).toInt()
    }
}
