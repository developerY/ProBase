package com.zoewave.probase.goswift.data

import androidx.health.connect.client.records.HydrationRecord
import com.zoewave.probase.core.data.repository.health.HealthConnectRepository
import java.time.Instant
import javax.inject.Inject

class HydrationRepositoryImpl @Inject constructor(
    private val healthConnectRepository: HealthConnectRepository
) : HydrationRepository {
    override suspend fun getHydrationRecords(startTime: Instant, endTime: Instant): List<HydrationRecord> {
        return healthConnectRepository.readHydrationRecords(startTime, endTime)
    }

    override suspend fun addHydrationRecord(volumeLiters: Double, timestamp: Instant) {
        healthConnectRepository.insertHydrationRecord(volumeLiters, timestamp)
    }
}
