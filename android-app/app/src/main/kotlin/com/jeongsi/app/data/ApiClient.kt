package com.jeongsi.app.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Retrofit 싱글톤. X-Device-Id 헤더 자동 주입. JSON은 snake_case ↔ camelCase 자동 변환. */
object ApiClient {
    // 클라우드(Railway) — 실기기·에뮬 어디서나 동작.
    private const val BASE_URL = "https://backend-production-286d.up.railway.app/"
    // 로컬 백엔드 개발 시: 에뮬레이터=10.0.2.2, 실기기=PC의 LAN IP
    // private const val BASE_URL = "http://10.0.2.2:8080/"

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("X-Device-Id", DeviceId.value)
                .build()
            chain.proceed(req)
        }
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }
}
