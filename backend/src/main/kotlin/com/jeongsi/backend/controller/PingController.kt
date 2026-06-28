package com.jeongsi.backend.controller

import com.jeongsi.backend.repository.CutoffRepository
import com.jeongsi.backend.repository.RecruitmentUnitRepository
import com.jeongsi.backend.repository.UniversityRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** 스캐폴드 검증용 — 더미 데이터 적재 확인. (Phase 2에서 실 API로 대체) */
@RestController
@RequestMapping("/api/v1/ping")
class PingController(
    private val universities: UniversityRepository,
    private val units: RecruitmentUnitRepository,
    private val cutoffs: CutoffRepository,
) {
    @GetMapping
    fun ping(): Map<String, Any> = mapOf(
        "status" to "ok",
        "universities" to universities.count(),
        "recruitment_units" to units.count(),
        "cutoffs" to cutoffs.count(),
    )
}
