package com.jeongsi.backend.service

import com.jeongsi.backend.entity.Device
import com.jeongsi.backend.repository.DeviceRepository
import org.springframework.stereotype.Component

/** X-Device-Id 헤더 → Device 엔티티 (없으면 생성). 익명 기기 인증. */
@Component
class DeviceResolver(private val devices: DeviceRepository) {

    /** 헤더값으로 기기 조회·생성. 헤더가 비면 null(성적 없는 게스트로 취급). */
    fun resolveOrNull(deviceId: String?): Device? {
        if (deviceId.isNullOrBlank()) return null
        return resolve(deviceId)
    }

    fun resolve(deviceId: String): Device =
        devices.findByDeviceId(deviceId) ?: devices.save(Device(deviceId = deviceId))
}
