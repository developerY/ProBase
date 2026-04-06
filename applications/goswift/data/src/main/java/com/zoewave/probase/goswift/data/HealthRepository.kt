package com.zoewave.probase.goswift.data

import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import java.time.Instant

interface HealthRepository {
    suspend fun getSleepSessions(startTime: Instant, endTime: Instant): List<SleepSessionRecord>
    suspend fun getExerciseSessions(startTime: Instant, endTime: Instant): List<ExerciseSessionRecord>
}
