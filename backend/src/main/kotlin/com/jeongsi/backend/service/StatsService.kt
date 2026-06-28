package com.jeongsi.backend.service

import com.jeongsi.backend.dto.Labels
import com.jeongsi.backend.dto.PopularItemDto
import com.jeongsi.backend.dto.UniversityDto
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

/**
 * 성적대별 인기 학과 TOP5 (SQL 집계 쇼케이스).
 * 모의지원 수를 JOIN + GROUP BY로 집계. 배치컷 구간(band)·계열·군으로 필터.
 */
@Service
class StatsService(private val jdbc: JdbcTemplate) {

    /**
     * @param band   배치컷 구간 라벨: high(>=95) / upper(90~95) / mid(85~90) / low(<85) / null=전체
     * @param track  humanities/natural/null
     * @param group  GA/NA/DA/null
     */
    fun popular(band: String?, track: String?, group: String?, limit: Int): List<PopularItemDto> {
        val (cutMin, cutMax) = bandRange(band)
        val sql = """
            SELECT u.id AS uid, u.name AS uname, u.region, u.est_type, u.logo_url,
                   ru.id AS unit_id, ru.department_name, ru.recruit_group,
                   c.cut_percentile, c.competition_rate,
                   COUNT(ma.id) AS mock_cnt
            FROM recruitment_units ru
            JOIN universities u ON u.id = ru.university_id
            JOIN cutoffs c      ON c.recruitment_unit_id = ru.id AND c.year = 2027
            LEFT JOIN mock_applications ma ON ma.recruitment_unit_id = ru.id
            WHERE c.cut_percentile >= ? AND c.cut_percentile < ?
              AND (CAST(? AS text) IS NULL OR ru.track = ?)
              AND (CAST(? AS text) IS NULL OR ru.recruit_group = ?)
            GROUP BY u.id, u.name, u.region, u.est_type, u.logo_url,
                     ru.id, ru.department_name, ru.recruit_group, c.cut_percentile, c.competition_rate
            ORDER BY mock_cnt DESC, c.cut_percentile DESC
            LIMIT ?
        """.trimIndent()

        val rows = jdbc.query(
            sql,
            { rs, _ ->
            PopularItemDto(
                rank = 0,
                university = UniversityDto(
                    id = rs.getLong("uid"),
                    name = rs.getString("uname"),
                    region = rs.getString("region"),
                    estType = rs.getString("est_type"),
                    logoUrl = rs.getString("logo_url"),
                ),
                unitId = rs.getLong("unit_id"),
                departmentName = rs.getString("department_name"),
                recruitGroup = rs.getString("recruit_group"),
                recruitGroupName = Labels.groupName(rs.getString("recruit_group")),
                cutPercentile = rs.getBigDecimal("cut_percentile").toDouble(),
                competitionRate = rs.getBigDecimal("competition_rate")?.toDouble(),
                mockApplyCount = rs.getLong("mock_cnt"),
            )
        },
            cutMin, cutMax, track, track, group, group, limit,
        )
        return rows.mapIndexed { i, item -> item.copy(rank = i + 1) }
    }

    private fun bandRange(band: String?): Pair<Double, Double> = when (band) {
        "high" -> 95.0 to 101.0
        "upper" -> 90.0 to 95.0
        "mid" -> 85.0 to 90.0
        "low" -> 0.0 to 85.0
        else -> 0.0 to 101.0
    }
}
