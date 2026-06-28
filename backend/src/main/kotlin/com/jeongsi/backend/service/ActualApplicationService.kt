package com.jeongsi.backend.service

import com.jeongsi.backend.entity.ActualApplication
import com.jeongsi.backend.repository.ActualApplicationRepository
import com.jeongsi.backend.repository.MockApplicationRepository
import com.jeongsi.backend.repository.RecruitmentUnitRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 실제 지원 대학 — 모의지원한 학과 중 군별 1개를 등록. 하루 3번/군 변경 제한.
 * (정시 실제 원서접수를 흉내. 제한 횟수는 placeholder.)
 */
@Service
class ActualApplicationService(
    private val actuals: ActualApplicationRepository,
    private val mockApps: MockApplicationRepository,
    private val units: RecruitmentUnitRepository,
) {
    enum class SetResult { OK, NOT_FOUND, NOT_MOCK, LIMIT }

    companion object { const val MAX_CHANGES_PER_DAY = 3 }
    private val kst = ZoneId.of("Asia/Seoul")

    @Transactional
    fun set(deviceDbId: Long, unitId: Long): SetResult {
        val unit = units.findById(unitId).orElse(null) ?: return SetResult.NOT_FOUND
        // 실제 지원은 모의지원한 학과 중에서만 가능
        if (mockApps.findByDeviceIdAndRecruitmentUnitId(deviceDbId, unitId) == null) return SetResult.NOT_MOCK
        val today = LocalDate.now(kst)
        val existing = actuals.findByDeviceIdAndRecruitGroup(deviceDbId, unit.recruitGroup)
        if (existing == null) {
            actuals.save(ActualApplication(deviceId = deviceDbId, recruitGroup = unit.recruitGroup,
                recruitmentUnitId = unitId, changeCount = 1, changeDate = today))
            return SetResult.OK
        }
        // 날짜 바뀌면 카운트 리셋
        if (existing.changeDate != today) { existing.changeCount = 0; existing.changeDate = today }
        if (existing.recruitmentUnitId == unitId) return SetResult.OK   // 동일 — 변경 아님
        if (existing.changeCount >= MAX_CHANGES_PER_DAY) return SetResult.LIMIT
        existing.recruitmentUnitId = unitId
        existing.changeCount += 1
        existing.updatedAt = Instant.now()
        actuals.save(existing)
        return SetResult.OK
    }

    @Transactional
    fun remove(deviceDbId: Long, group: String) {
        actuals.findByDeviceIdAndRecruitGroup(deviceDbId, group)?.let { actuals.delete(it) }
    }

    /** 군별 실제 지원 unitId (가/나/다). */
    fun picksByGroup(deviceDbId: Long): Map<String, Long> =
        actuals.findByDeviceId(deviceDbId).associate { it.recruitGroup to it.recruitmentUnitId }

    /** 군별 오늘 남은 변경 횟수 (3 − count). */
    fun remainingChanges(deviceDbId: Long): Map<String, Int> {
        val today = LocalDate.now(kst)
        return listOf("GA", "NA", "DA").associateWith { g ->
            val a = actuals.findByDeviceIdAndRecruitGroup(deviceDbId, g)
            if (a == null || a.changeDate != today) MAX_CHANGES_PER_DAY else (MAX_CHANGES_PER_DAY - a.changeCount).coerceAtLeast(0)
        }
    }
}
