package com.jeongsi.backend.service

import com.jeongsi.backend.dto.Labels
import com.jeongsi.backend.dto.TargetDto
import com.jeongsi.backend.entity.TargetUnit
import com.jeongsi.backend.repository.RecruitmentUnitRepository
import com.jeongsi.backend.repository.TargetUnitRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** 목표 대학 (군별 1지망/2지망). 정시는 군당 1개 지원이므로 군×지망 슬롯으로 관리. */
@Service
class TargetService(
    private val targets: TargetUnitRepository,
    private val units: RecruitmentUnitRepository,
    private val catalog: CatalogService,
) {
    @Transactional
    fun set(deviceDbId: Long, unitId: Long, priority: Int): Boolean {
        val unit = units.findById(unitId).orElse(null) ?: return false
        // 같은 군·지망 슬롯에 기존 목표가 있으면 교체
        targets.findByDeviceIdAndRecruitGroupAndPriority(deviceDbId, unit.recruitGroup, priority)
            ?.let { targets.delete(it) }
        targets.save(
            TargetUnit(deviceId = deviceDbId, recruitmentUnitId = unitId, recruitGroup = unit.recruitGroup, priority = priority),
        )
        return true
    }

    @Transactional
    fun remove(deviceDbId: Long, unitId: Long) = targets.deleteByDeviceIdAndRecruitmentUnitId(deviceDbId, unitId)

    fun list(deviceDbId: Long): List<TargetDto> {
        val ts = targets.findByDeviceId(deviceDbId)
        if (ts.isEmpty()) return emptyList()
        val cards = catalog.buildCards(units.findAllById(ts.map { it.recruitmentUnitId }), deviceDbId)
            .associateBy { it.unitId }
        return ts.mapNotNull { t ->
            val card = cards[t.recruitmentUnitId] ?: return@mapNotNull null
            TargetDto(t.recruitGroup, Labels.groupName(t.recruitGroup), t.priority, card)
        }.sortedWith(compareBy({ it.group }, { it.priority }))
    }
}
