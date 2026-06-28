package com.jeongsi.backend.service

import com.jeongsi.backend.dto.CutoffTrendDto
import com.jeongsi.backend.dto.Labels
import com.jeongsi.backend.dto.RegionRankDto
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

/**
 * 윈도우 함수 기반 분석 (SQL 쇼케이스).
 *  - 지역 내 배치컷 순위: RANK() / PERCENT_RANK() / COUNT() OVER (PARTITION BY region)
 *  - 연도별 배치컷 추이:  CTE + LAG() OVER (ORDER BY year) → 작년 대비 변화
 * 모두 손으로 쓴 SQL(JdbcTemplate). JPA 파생 쿼리로는 표현 불가능한 분석 계층.
 */
@Service
class TrendService(private val jdbc: JdbcTemplate) {

    /**
     * 지역 내 배치컷 순위. region=null이면 전 지역을 각자 파티션으로 묶어 반환.
     * PARTITION BY region 으로 "같은 지역끼리" 묶어 순위/상위%를 계산한다.
     */
    fun regionRanking(region: String?, track: String?): List<RegionRankDto> {
        val sql = """
            SELECT u.region, ru.id AS unit_id, u.name AS uname, ru.department_name,
                   ru.recruit_group, c.cut_percentile,
                   RANK()   OVER (PARTITION BY u.region ORDER BY c.cut_percentile DESC) AS region_rank,
                   COUNT(*) OVER (PARTITION BY u.region)                                 AS region_total,
                   ROUND((PERCENT_RANK() OVER (PARTITION BY u.region ORDER BY c.cut_percentile DESC) * 100)::numeric, 1) AS top_pct
            FROM recruitment_units ru
            JOIN universities u ON u.id = ru.university_id
            JOIN cutoffs c      ON c.recruitment_unit_id = ru.id AND c.year = 2027
            WHERE (CAST(? AS text) IS NULL OR u.region = ?)
              AND (CAST(? AS text) IS NULL OR ru.track = ?)
            ORDER BY u.region, region_rank
        """.trimIndent()
        return jdbc.query(sql, { rs, _ ->
            RegionRankDto(
                region = rs.getString("region"),
                unitId = rs.getLong("unit_id"),
                universityName = rs.getString("uname"),
                departmentName = rs.getString("department_name"),
                recruitGroup = rs.getString("recruit_group"),
                recruitGroupName = Labels.groupName(rs.getString("recruit_group")),
                cutPercentile = rs.getBigDecimal("cut_percentile").toDouble(),
                regionRank = rs.getInt("region_rank"),
                regionTotal = rs.getInt("region_total"),
                topPercent = rs.getBigDecimal("top_pct").toDouble(),
            )
        }, region, region, track, track)
    }

    /**
     * 한 모집단위의 연도별 배치컷 추이. LAG로 작년 값을 끌어와 변화량(yoy)을 계산.
     */
    fun cutoffTrend(unitId: Long): List<CutoffTrendDto> {
        val sql = """
            WITH trend AS (
                SELECT c.year, c.cut_percentile, c.competition_rate,
                       LAG(c.cut_percentile) OVER (ORDER BY c.year) AS prev_cut
                FROM cutoffs c
                WHERE c.recruitment_unit_id = ?
            )
            SELECT year, cut_percentile, competition_rate, prev_cut,
                   ROUND((cut_percentile - prev_cut)::numeric, 1) AS yoy_change
            FROM trend
            ORDER BY year
        """.trimIndent()
        return jdbc.query(sql, { rs, _ ->
            CutoffTrendDto(
                year = rs.getInt("year"),
                cutPercentile = rs.getBigDecimal("cut_percentile").toDouble(),
                competitionRate = rs.getBigDecimal("competition_rate")?.toDouble(),
                prevCut = rs.getBigDecimal("prev_cut")?.toDouble(),
                yoyChange = rs.getBigDecimal("yoy_change")?.toDouble(),
            )
        }, unitId)
    }
}
