package com.jeongsi.backend.repository

import com.jeongsi.backend.entity.Cutoff
import com.jeongsi.backend.entity.Device
import com.jeongsi.backend.entity.Favorite
import com.jeongsi.backend.entity.MockApplication
import com.jeongsi.backend.entity.RecruitmentUnit
import com.jeongsi.backend.entity.ActualApplication
import com.jeongsi.backend.entity.ScoreDistParam
import com.jeongsi.backend.entity.ScoreRule
import com.jeongsi.backend.entity.TargetUnit
import com.jeongsi.backend.entity.University
import com.jeongsi.backend.entity.UserScore
import org.springframework.data.jpa.repository.JpaRepository

interface UniversityRepository : JpaRepository<University, Long>

interface RecruitmentUnitRepository : JpaRepository<RecruitmentUnit, Long> {
    fun findByRecruitGroup(recruitGroup: String): List<RecruitmentUnit>
    fun findByTrack(track: String): List<RecruitmentUnit>
}

interface ScoreRuleRepository : JpaRepository<ScoreRule, Long> {
    fun findByRecruitmentUnitId(recruitmentUnitId: Long): ScoreRule?
    fun findByRecruitmentUnitIdIn(ids: Collection<Long>): List<ScoreRule>
}

interface CutoffRepository : JpaRepository<Cutoff, Long> {
    fun findByRecruitmentUnitIdAndYear(recruitmentUnitId: Long, year: Int): Cutoff?
    fun findByRecruitmentUnitIdInAndYear(ids: Collection<Long>, year: Int): List<Cutoff>
}

interface DeviceRepository : JpaRepository<Device, Long> {
    fun findByDeviceId(deviceId: String): Device?
}

interface UserScoreRepository : JpaRepository<UserScore, Long> {
    fun findByDeviceId(deviceId: Long): UserScore?
}

interface MockApplicationRepository : JpaRepository<MockApplication, Long> {
    fun findByDeviceId(deviceId: Long): List<MockApplication>
    fun findByDeviceIdAndRecruitmentUnitId(deviceId: Long, recruitmentUnitId: Long): MockApplication?
    fun deleteByDeviceIdAndRecruitmentUnitId(deviceId: Long, recruitmentUnitId: Long)
}

interface FavoriteRepository : JpaRepository<Favorite, Long> {
    fun findByDeviceId(deviceId: Long): List<Favorite>
    fun findByDeviceIdAndRecruitmentUnitId(deviceId: Long, recruitmentUnitId: Long): Favorite?
    fun deleteByDeviceIdAndRecruitmentUnitId(deviceId: Long, recruitmentUnitId: Long)
}

interface TargetUnitRepository : JpaRepository<TargetUnit, Long> {
    fun findByDeviceId(deviceId: Long): List<TargetUnit>
    fun findByDeviceIdAndRecruitGroupAndPriority(deviceId: Long, recruitGroup: String, priority: Int): TargetUnit?
    fun deleteByDeviceIdAndRecruitmentUnitId(deviceId: Long, recruitmentUnitId: Long)
}

interface ScoreDistParamRepository : JpaRepository<ScoreDistParam, Long> {
    fun findByReflectTypeAndIndexType(reflectType: String, indexType: String): ScoreDistParam?
}

interface ActualApplicationRepository : JpaRepository<ActualApplication, Long> {
    fun findByDeviceId(deviceId: Long): List<ActualApplication>
    fun findByDeviceIdAndRecruitGroup(deviceId: Long, recruitGroup: String): ActualApplication?
}
