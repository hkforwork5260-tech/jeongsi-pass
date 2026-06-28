package com.jeongsi.app.data

/** 백엔드 호출 래퍼. 화면 ViewModel이 사용. 실패는 Result로 감싸 호출부에서 처리. */
object Repository {
    private val api get() = ApiClient.api

    suspend fun home(): Home = api.home()
    suspend fun saveScore(score: ScoreModel): ScoreModel = api.saveScore(score)
    suspend fun getScore(): ScoreModel = api.getScore()
    suspend fun analysis(): AnalysisModel = api.analysis()

    suspend fun discover(limit: Int = 30): List<UnitCard> = api.discover(limit)
    suspend fun search(
        group: String? = null, track: String? = null, region: String? = null,
        q: String? = null, minCut: Double? = null, maxCut: Double? = null, sort: String? = null,
        university: String? = null, department: String? = null,
    ): List<UnitCard> = api.search(group, track, region, q, minCut, maxCut, sort, university, department)
    suspend fun departments(): List<String> = api.departments()
    suspend fun unitDetail(id: Long): UnitDetail = api.unitDetail(id)

    suspend fun targets(): List<TargetModel> = api.targets()
    suspend fun setTarget(unitId: Long, priority: Int = 1) = api.setTarget(unitId, priority)
    suspend fun removeTarget(unitId: Long) = api.removeTarget(unitId)
    suspend fun compare(group: String): CompareModel = api.compare(group)

    suspend fun mockApplications(): List<UnitCard> = api.mockApplications()
    suspend fun mockSummary(): MockSummary = api.mockSummary()
    suspend fun recommend(): Map<String, List<UnitCard>> = api.recommend()
    suspend fun strategy(preset: String, track: String? = null): Strategy = api.strategy(preset, track)
    suspend fun strategyCombos(
        preset: String, ga: String?, na: String?, da: String?, track: String?,
        region: String? = null, university: String? = null, department: String? = null,
        offset: Int, limit: Int = 12,
    ): StrategyCombos = api.strategyCombos(preset, ga, na, da, track, region, university, department, offset, limit)
    suspend fun universities(): List<String> = api.universities()

    /** 실제 지원 등록. 성공 시 null, 실패(한도·모의지원 아님 등) 시 메시지. */
    suspend fun setActual(unitId: Long): String? {
        val res = api.setActual(unitId)
        return if (res.isSuccessful) null
        else res.errorBody()?.let { runCatching { it.string() }.getOrNull() }?.let {
            runCatching { kotlinx.serialization.json.Json.parseToJsonElement(it) }.getOrNull()
        }?.let { (it as? kotlinx.serialization.json.JsonObject)?.get("message")?.toString()?.trim('"') }
            ?: "변경할 수 없어요."
    }
    suspend fun removeActual(group: String) = api.removeActual(group)

    /** 모의지원 담기. 성공 시 null, 한도 초과 등 실패 시 메시지 반환. */
    suspend fun addMock(unitId: Long): String? {
        val res = api.addMock(unitId)
        return if (res.isSuccessful) null
        else res.errorBody()?.let { runCatching { it.string() } }?.getOrNull()?.let {
            runCatching { kotlinx.serialization.json.Json.parseToJsonElement(it) }.getOrNull()
        }?.let { (it as? kotlinx.serialization.json.JsonObject)?.get("message")?.toString()?.trim('"') }
            ?: "한 군에 최대 20개까지 담을 수 있어요."
    }

    suspend fun removeMock(unitId: Long) = api.removeMock(unitId)

    suspend fun report(): Report = api.report()
    suspend fun popular(band: String? = null): List<PopularItem> = api.popular(band = band)

    suspend fun cutoffTrend(unitId: Long): List<CutoffTrend> = api.cutoffTrend(unitId)
    suspend fun regionRank(region: String?, track: String?): List<RegionRank> = api.regionRank(region, track)
}
