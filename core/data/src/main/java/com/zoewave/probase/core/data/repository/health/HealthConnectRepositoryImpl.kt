package com.zoewave.probase.core.data.repository.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Volume
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HealthConnectRepository {

    private val isSupported: Boolean by lazy {
        try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            false
        }
    }

    private val client: HealthConnectClient? by lazy {
        if (isSupported) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    override suspend fun insert(records: List<Record>) {
        client?.insertRecords(records)
    }

    override suspend fun readExerciseSessions(
        startTime: Instant,
        endTime: Instant
    ): List<ExerciseSessionRecord> {
        val currentClient = client ?: return emptyList()
        val request = ReadRecordsRequest(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return currentClient.readRecords(request).records
    }

    override suspend fun readSleepSessions(
        startTime: Instant,
        endTime: Instant
    ): List<SleepSessionRecord> {
        val currentClient = client ?: return emptyList()
        val request = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return currentClient.readRecords(request).records
    }

    override suspend fun readHydrationRecords(
        startTime: Instant,
        endTime: Instant
    ): List<HydrationRecord> {
        val currentClient = client ?: return emptyList()
        val request = ReadRecordsRequest(
            recordType = HydrationRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return currentClient.readRecords(request).records
    }

    override suspend fun insertHydrationRecord(volume: Double, timestamp: Instant) {
        val currentClient = client ?: return
        val record = HydrationRecord(
            startTime = timestamp,
            startZoneOffset = null,
            endTime = timestamp.plusSeconds(1),
            endZoneOffset = null,
            volume = Volume.liters(volume),
            metadata = Metadata.manualEntry()
        )
        currentClient.insertRecords(listOf(record))
    }

    override suspend fun readNutritionRecords(
        startTime: Instant,
        endTime: Instant
    ): List<NutritionRecord> {
        val currentClient = client ?: return emptyList()
        val request = ReadRecordsRequest(
            recordType = NutritionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return currentClient.readRecords(request).records
    }

    override suspend fun insertNutritionRecord(foodName: String, calories: Double, timestamp: Instant) {
        val currentClient = client ?: return
        val record = NutritionRecord(
            startTime = timestamp,
            startZoneOffset = null,
            endTime = timestamp.plusSeconds(1),
            endZoneOffset = null,
            name = foodName,
            energy = Energy.kilocalories(calories),
            metadata = Metadata.manualEntry()
        )
        currentClient.insertRecords(listOf(record))
    }

    override suspend fun deleteAllSessions(before: Instant) {
        val currentClient = client ?: return
        currentClient.deleteRecords(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.before(before)
        )
    }
}
