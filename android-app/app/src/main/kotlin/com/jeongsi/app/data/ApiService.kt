package com.jeongsi.app.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // 성적
    @POST("api/v1/scores")
    suspend fun saveScore(@Body score: ScoreModel): ScoreModel

    @GET("api/v1/scores")
    suspend fun getScore(): ScoreModel

    @GET("api/v1/scores/analysis")
    suspend fun analysis(): AnalysisModel

    // 모집단위
    @GET("api/v1/units/search")
    suspend fun search(
        @Query("group") group: String? = null,
        @Query("track") track: String? = null,
        @Query("region") region: String? = null,
        @Query("q") q: String? = null,
        @Query("min_cut") minCut: Double? = null,
        @Query("max_cut") maxCut: Double? = null,
        @Query("sort") sort: String? = null,
        @Query("university") university: String? = null,
        @Query("department") department: String? = null,
    ): List<UnitCard>

    @GET("api/v1/units/{id}")
    suspend fun unitDetail(@Path("id") id: Long): UnitDetail

    @GET("api/v1/units/recommend")
    suspend fun recommend(): Map<String, List<UnitCard>>

    @GET("api/v1/units/strategy")
    suspend fun strategy(@Query("preset") preset: String, @Query("track") track: String? = null): Strategy

    @GET("api/v1/units/strategy-combos")
    suspend fun strategyCombos(
        @Query("preset") preset: String = "balanced",
        @Query("ga") ga: String? = null,
        @Query("na") na: String? = null,
        @Query("da") da: String? = null,
        @Query("track") track: String? = null,
        @Query("region") region: String? = null,
        @Query("university") university: String? = null,
        @Query("department") department: String? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 12,
    ): StrategyCombos

    @GET("api/v1/units/universities")
    suspend fun universities(): List<String>

    @GET("api/v1/units/departments")
    suspend fun departments(): List<String>

    // 실제 지원 대학
    @POST("api/v1/actual-applications/{unitId}")
    suspend fun setActual(@Path("unitId") unitId: Long): retrofit2.Response<Map<String, String>>

    @DELETE("api/v1/actual-applications/{group}")
    suspend fun removeActual(@Path("group") group: String)

    // 인스타 찾아보기
    @GET("api/v1/discover")
    suspend fun discover(@Query("limit") limit: Int = 30): List<UnitCard>

    // 모의지원
    @GET("api/v1/mock-applications")
    suspend fun mockApplications(): List<UnitCard>

    @GET("api/v1/mock-applications/summary")
    suspend fun mockSummary(): MockSummary

    @POST("api/v1/mock-applications/{unitId}")
    suspend fun addMock(@Path("unitId") unitId: Long): retrofit2.Response<Map<String, String>>

    @DELETE("api/v1/mock-applications/{unitId}")
    suspend fun removeMock(@Path("unitId") unitId: Long)

    // 관심
    @GET("api/v1/favorites")
    suspend fun favorites(): List<UnitCard>

    @POST("api/v1/favorites/{unitId}")
    suspend fun addFavorite(@Path("unitId") unitId: Long)

    @DELETE("api/v1/favorites/{unitId}")
    suspend fun removeFavorite(@Path("unitId") unitId: Long)

    // 목표 대학
    @GET("api/v1/target-units")
    suspend fun targets(): List<TargetModel>

    @POST("api/v1/target-units/{unitId}")
    suspend fun setTarget(@Path("unitId") unitId: Long, @Query("priority") priority: Int = 1)

    @DELETE("api/v1/target-units/{unitId}")
    suspend fun removeTarget(@Path("unitId") unitId: Long)

    // 리포트 / 통계 / 홈
    @GET("api/v1/report")
    suspend fun report(): Report

    @GET("api/v1/report/compare")
    suspend fun compare(@Query("group") group: String): CompareModel

    @GET("api/v1/stats/popular")
    suspend fun popular(
        @Query("band") band: String? = null,
        @Query("track") track: String? = null,
        @Query("group") group: String? = null,
        @Query("limit") limit: Int = 5,
    ): List<PopularItem>

    @GET("api/v1/home")
    suspend fun home(): Home

    // 윈도우 함수 분석
    @GET("api/v1/analytics/cutoff-trend/{unitId}")
    suspend fun cutoffTrend(@Path("unitId") unitId: Long): List<CutoffTrend>

    @GET("api/v1/analytics/region-rank")
    suspend fun regionRank(
        @Query("region") region: String? = null,
        @Query("track") track: String? = null,
    ): List<RegionRank>
}
