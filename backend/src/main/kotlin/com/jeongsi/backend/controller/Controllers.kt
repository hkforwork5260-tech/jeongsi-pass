package com.jeongsi.backend.controller

import com.jeongsi.backend.dto.AnalysisDto
import com.jeongsi.backend.dto.CompareDto
import com.jeongsi.backend.dto.HomeDto
import com.jeongsi.backend.dto.MockSummaryDto
import com.jeongsi.backend.dto.ReportDto
import com.jeongsi.backend.dto.ScoreDto
import com.jeongsi.backend.dto.StrategyCombosDto
import com.jeongsi.backend.dto.StrategyDto
import com.jeongsi.backend.dto.TargetDto
import com.jeongsi.backend.dto.UnitCardDto
import com.jeongsi.backend.dto.UnitDetailDto
import com.jeongsi.backend.service.ActualApplicationService
import com.jeongsi.backend.service.AnalysisService
import com.jeongsi.backend.service.ApplicationService
import com.jeongsi.backend.service.CatalogService
import com.jeongsi.backend.service.DeviceResolver
import com.jeongsi.backend.service.HomeService
import com.jeongsi.backend.service.ScoreService
import com.jeongsi.backend.service.StatsService
import com.jeongsi.backend.service.TargetService
import com.jeongsi.backend.service.TrendService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private const val DEVICE_HEADER = "X-Device-Id"

/** 성적 입력/조회/분석 */
@RestController
@RequestMapping("/api/v1/scores")
class ScoreController(
    private val scoreService: ScoreService,
    private val analysisService: AnalysisService,
    private val devices: DeviceResolver,
) {
    @PostMapping
    fun save(
        @RequestHeader(DEVICE_HEADER) deviceId: String,
        @RequestBody dto: ScoreDto,
    ): ScoreDto = scoreService.save(devices.resolve(deviceId).id, dto)

    @GetMapping
    fun get(@RequestHeader(DEVICE_HEADER) deviceId: String): ResponseEntity<ScoreDto> =
        scoreService.get(devices.resolve(deviceId).id)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.noContent().build()

    @GetMapping("/analysis")
    fun analysis(@RequestHeader(DEVICE_HEADER) deviceId: String): AnalysisDto =
        analysisService.analysis(devices.resolve(deviceId).id)
}

/** 모집단위 검색·상세·찾아보기·추천 */
@RestController
@RequestMapping("/api/v1/units")
class UnitController(
    private val catalog: CatalogService,
    private val devices: DeviceResolver,
) {
    @GetMapping("/search")
    fun search(
        @RequestHeader(DEVICE_HEADER, required = false) deviceId: String?,
        @RequestParam(required = false) group: String?,
        @RequestParam(required = false) track: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) q: String?,
        @RequestParam(name = "min_cut", required = false) minCut: Double?,
        @RequestParam(name = "max_cut", required = false) maxCut: Double?,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false) university: String?,
        @RequestParam(required = false) department: String?,
    ): List<UnitCardDto> =
        catalog.search(devices.resolveOrNull(deviceId)?.id, group, track, region, q, minCut, maxCut, sort, university, department)

    @GetMapping("/recommend")
    fun recommend(
        @RequestHeader(DEVICE_HEADER, required = false) deviceId: String?,
        @RequestParam(required = false) track: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) university: String?,
        @RequestParam(required = false) department: String?,
    ): Map<String, List<UnitCardDto>> =
        catalog.recommend(devices.resolveOrNull(deviceId)?.id, track, region, university, department)

    @GetMapping("/strategy")
    fun strategy(
        @RequestHeader(DEVICE_HEADER, required = false) deviceId: String?,
        @RequestParam(required = false, defaultValue = "balanced") preset: String,
        @RequestParam(required = false) track: String?,
    ): StrategyDto = catalog.strategy(devices.resolveOrNull(deviceId)?.id, preset, track)

    /** 전략 조합 여러 개. 군별 라벨 직접 지정(없으면 preset에서 파생) + track + 페이지네이션. */
    @GetMapping("/strategy-combos")
    fun strategyCombos(
        @RequestHeader(DEVICE_HEADER, required = false) deviceId: String?,
        @RequestParam(required = false, defaultValue = "balanced") preset: String,
        @RequestParam(required = false) ga: String?,
        @RequestParam(required = false) na: String?,
        @RequestParam(required = false) da: String?,
        @RequestParam(required = false) track: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) university: String?,
        @RequestParam(required = false) department: String?,
        @RequestParam(required = false, defaultValue = "0") offset: Int,
        @RequestParam(required = false, defaultValue = "12") limit: Int,
    ): StrategyCombosDto {
        val (pg, pn, pd) = catalog.presetLabels(preset)
        return catalog.strategyCombos(
            devices.resolveOrNull(deviceId)?.id, ga ?: pg, na ?: pn, da ?: pd,
            track, region, university, department, offset, limit,
        )
    }

    @GetMapping("/universities")
    fun universities(): List<String> = catalog.universityNames()

    @GetMapping("/departments")
    fun departments(): List<String> = catalog.departmentNames()

    @GetMapping("/{id}")
    fun detail(
        @RequestHeader(DEVICE_HEADER, required = false) deviceId: String?,
        @PathVariable id: Long,
    ): ResponseEntity<UnitDetailDto> =
        catalog.detail(id, devices.resolveOrNull(deviceId)?.id)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
}

/** ★ 인스타 찾아보기 피드 */
@RestController
@RequestMapping("/api/v1/discover")
class DiscoverController(
    private val catalog: CatalogService,
    private val devices: DeviceResolver,
) {
    @GetMapping
    fun discover(
        @RequestHeader(DEVICE_HEADER, required = false) deviceId: String?,
        @RequestParam(required = false, defaultValue = "30") limit: Int,
    ): List<UnitCardDto> = catalog.discover(devices.resolveOrNull(deviceId)?.id, limit)
}

/** 모의지원 */
@RestController
@RequestMapping("/api/v1/mock-applications")
class MockApplicationController(
    private val apps: ApplicationService,
    private val devices: DeviceResolver,
) {
    @GetMapping
    fun list(@RequestHeader(DEVICE_HEADER) deviceId: String): List<UnitCardDto> =
        apps.listMock(devices.resolve(deviceId).id)

    /** 모의지원 탭 요약(군별 목록 + 조합 + 합격확률 분석). */
    @GetMapping("/summary")
    fun summary(@RequestHeader(DEVICE_HEADER) deviceId: String): MockSummaryDto =
        apps.summary(devices.resolve(deviceId).id)

    @PostMapping("/{unitId}")
    fun add(@RequestHeader(DEVICE_HEADER) deviceId: String, @PathVariable unitId: Long): ResponseEntity<Map<String, String>> =
        when (apps.addMock(devices.resolve(deviceId).id, unitId)) {
            ApplicationService.AddResult.OK -> ResponseEntity.ok(mapOf("status" to "ok"))
            ApplicationService.AddResult.NOT_FOUND -> ResponseEntity.notFound().build()
            ApplicationService.AddResult.LIMIT ->
                ResponseEntity.status(409).body(mapOf("status" to "limit", "message" to "한 군에 최대 ${ApplicationService.MAX_PER_GROUP}개까지 담을 수 있어요."))
        }

    @DeleteMapping("/{unitId}")
    fun remove(@RequestHeader(DEVICE_HEADER) deviceId: String, @PathVariable unitId: Long): ResponseEntity<Void> {
        apps.removeMock(devices.resolve(deviceId).id, unitId)
        return ResponseEntity.ok().build()
    }
}

/** 실제 지원 대학 (군별 1개, 하루 3번 변경) */
@RestController
@RequestMapping("/api/v1/actual-applications")
class ActualApplicationController(
    private val actual: ActualApplicationService,
    private val devices: DeviceResolver,
) {
    @PostMapping("/{unitId}")
    fun set(@RequestHeader(DEVICE_HEADER) deviceId: String, @PathVariable unitId: Long): ResponseEntity<Map<String, String>> =
        when (actual.set(devices.resolve(deviceId).id, unitId)) {
            ActualApplicationService.SetResult.OK -> ResponseEntity.ok(mapOf("status" to "ok"))
            ActualApplicationService.SetResult.NOT_FOUND -> ResponseEntity.notFound().build()
            ActualApplicationService.SetResult.NOT_MOCK ->
                ResponseEntity.status(400).body(mapOf("status" to "not_mock", "message" to "모의지원한 학과만 실제 지원으로 등록할 수 있어요."))
            ActualApplicationService.SetResult.LIMIT ->
                ResponseEntity.status(409).body(mapOf("status" to "limit", "message" to "하루 ${ActualApplicationService.MAX_CHANGES_PER_DAY}번까지 변경할 수 있어요."))
        }

    @DeleteMapping("/{group}")
    fun remove(@RequestHeader(DEVICE_HEADER) deviceId: String, @PathVariable group: String): ResponseEntity<Void> {
        actual.remove(devices.resolve(deviceId).id, group)
        return ResponseEntity.ok().build()
    }
}

/** 관심 학과 */
@RestController
@RequestMapping("/api/v1/favorites")
class FavoriteController(
    private val apps: ApplicationService,
    private val devices: DeviceResolver,
) {
    @GetMapping
    fun list(@RequestHeader(DEVICE_HEADER) deviceId: String): List<UnitCardDto> =
        apps.listFavorites(devices.resolve(deviceId).id)

    @PostMapping("/{unitId}")
    fun add(@RequestHeader(DEVICE_HEADER) deviceId: String, @PathVariable unitId: Long): ResponseEntity<Void> =
        if (apps.addFavorite(devices.resolve(deviceId).id, unitId)) ResponseEntity.ok().build()
        else ResponseEntity.notFound().build()

    @DeleteMapping("/{unitId}")
    fun remove(@RequestHeader(DEVICE_HEADER) deviceId: String, @PathVariable unitId: Long): ResponseEntity<Void> {
        apps.removeFavorite(devices.resolve(deviceId).id, unitId)
        return ResponseEntity.ok().build()
    }
}

/** 합격예측 리포트 */
@RestController
@RequestMapping("/api/v1/report")
class ReportController(
    private val apps: ApplicationService,
    private val devices: DeviceResolver,
) {
    @GetMapping
    fun report(@RequestHeader(DEVICE_HEADER) deviceId: String): ReportDto =
        apps.report(devices.resolve(deviceId).id)

    @GetMapping("/compare")
    fun compare(
        @RequestHeader(DEVICE_HEADER) deviceId: String,
        @RequestParam group: String,
    ): CompareDto = apps.compare(devices.resolve(deviceId).id, group)
}

/** 목표 대학 (군별 1지망/2지망) */
@RestController
@RequestMapping("/api/v1/target-units")
class TargetController(
    private val targetService: TargetService,
    private val devices: DeviceResolver,
) {
    @GetMapping
    fun list(@RequestHeader(DEVICE_HEADER) deviceId: String): List<TargetDto> =
        targetService.list(devices.resolve(deviceId).id)

    @PostMapping("/{unitId}")
    fun set(
        @RequestHeader(DEVICE_HEADER) deviceId: String,
        @PathVariable unitId: Long,
        @RequestParam(required = false, defaultValue = "1") priority: Int,
    ): ResponseEntity<Void> =
        if (targetService.set(devices.resolve(deviceId).id, unitId, priority)) ResponseEntity.ok().build()
        else ResponseEntity.notFound().build()

    @DeleteMapping("/{unitId}")
    fun remove(@RequestHeader(DEVICE_HEADER) deviceId: String, @PathVariable unitId: Long): ResponseEntity<Void> {
        targetService.remove(devices.resolve(deviceId).id, unitId)
        return ResponseEntity.ok().build()
    }
}

/** 성적대별 인기 학과 TOP5 (SQL 집계) */
@RestController
@RequestMapping("/api/v1/stats")
class StatsController(private val stats: StatsService) {
    @GetMapping("/popular")
    fun popular(
        @RequestParam(required = false) band: String?,
        @RequestParam(required = false) track: String?,
        @RequestParam(required = false) group: String?,
        @RequestParam(required = false, defaultValue = "5") limit: Int,
    ) = stats.popular(band, track, group, limit)
}

/** 윈도우 함수 분석 — 지역 내 순위(RANK/PERCENT_RANK), 연도별 추이(LAG) */
@RestController
@RequestMapping("/api/v1/analytics")
class AnalyticsController(private val trend: TrendService) {
    @GetMapping("/region-rank")
    fun regionRank(
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) track: String?,
    ) = trend.regionRanking(region, track)

    @GetMapping("/cutoff-trend/{unitId}")
    fun cutoffTrend(@PathVariable unitId: Long) = trend.cutoffTrend(unitId)
}

/** 홈 대시보드 */
@RestController
@RequestMapping("/api/v1/home")
class HomeController(
    private val home: HomeService,
    private val devices: DeviceResolver,
) {
    @GetMapping
    fun home(@RequestHeader(DEVICE_HEADER, required = false) deviceId: String?): HomeDto =
        home.home(devices.resolveOrNull(deviceId)?.id)
}
