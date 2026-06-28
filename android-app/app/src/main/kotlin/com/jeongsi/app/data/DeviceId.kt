package com.jeongsi.app.data

import android.content.Context
import java.util.UUID

/** 익명 기기 식별자(UUID). 최초 1회 생성 후 SharedPreferences에 영속. X-Device-Id 헤더로 사용. */
object DeviceId {
    private const val PREF = "jeongsi_device"
    private const val KEY = "device_id"
    @Volatile private var cached: String? = null

    /** Application/Activity onCreate에서 1회 호출. */
    fun init(context: Context) {
        if (cached != null) return
        val prefs = context.applicationContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        cached = prefs.getString(KEY, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY, it).apply()
        }
    }

    val value: String
        get() = cached ?: "uninitialized-device"
}
