package com.zoewave.probase.goswift.data

import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import com.zoewave.probase.core.data.repository.health.HealthConnectRepository
import java.time.Instant
import javax.inject.Inject

class HealthRepositoryImpl @Inject constructor(
    private val healthConnectRepository: HealthConnectRepository
) : HealthRepository {
    override suspend fun getSleepSessions(startTime: Instant, endTime: Instant): List<SleepSessionRecord> {
        return healthConnectRepository.readSleepSessions(startTime, endTime)
    }

    override suspend fun getExerciseSessions(startTime: Instant, endTime: Instant): List<ExerciseSessionRecord> {
        return healthConnectRepository.readExerciseSessions(startTime, endTime)
    }
}
