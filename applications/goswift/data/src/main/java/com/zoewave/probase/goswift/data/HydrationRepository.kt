package com.zoewave.probase.goswift.data

import androidx.health.connect.client.records.HydrationRecord
import java.time.Instant

interface HydrationRepository {
    suspend fun getHydrationRecords(startTime: Instant, endTime: Instant): List<HydrationRecord>
    suspend fun addHydrationRecord(volumeLiters: Double, timestamp: Instant)
}
