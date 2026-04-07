package com.zoewave.probase.goswift.data

import androidx.health.connect.client.records.NutritionRecord
import com.zoewave.probase.core.data.repository.health.HealthConnectRepository
import java.time.Instant
import javax.inject.Inject

class NutritionRepositoryImpl @Inject constructor(
    private val healthConnectRepository: HealthConnectRepository
) : NutritionRepository {
    override suspend fun getNutritionRecords(startTime: Instant, endTime: Instant): List<NutritionRecord> {
        return healthConnectRepository.readNutritionRecords(startTime, endTime)
    }

    override suspend fun addNutritionRecord(foodName: String, calories: Double, timestamp: Instant) {
        healthConnectRepository.insertNutritionRecord(foodName, calories, timestamp)
    }
}
