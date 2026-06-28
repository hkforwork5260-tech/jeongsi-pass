package com.jeongsi.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * 합격각 백엔드 — 정시 합격 분석 + 학과 탐색 API.
 * Spring Boot + JPA + Flyway + PostgreSQL. 익명 기기 인증(X-Device-Id).
 */
@SpringBootApplication
class HapgyeokApplication

fun main(args: Array<String>) {
    runApplication<HapgyeokApplication>(*args)
}
