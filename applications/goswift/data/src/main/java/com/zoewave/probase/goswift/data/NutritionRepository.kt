package com.zoewave.probase.goswift.data

import androidx.health.connect.client.records.NutritionRecord
import java.time.Instant

interface NutritionRepository {
    suspend fun getNutritionRecords(startTime: Instant, endTime: Instant): List<NutritionRecord>
    suspend fun addNutritionRecord(foodName: String, calories: Double, timestamp: Instant)
}
