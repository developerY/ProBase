package com.zoewave.probase.kocolor.data.repository

import com.zoewave.probase.kocolor.db.entity.ClothingUsageEntity
import com.zoewave.probase.kocolor.db.entity.GlobalRotationMetricsEntity
import kotlinx.coroutines.flow.Flow

interface RotationRepository {
    fun observeGlobalMetrics(): Flow<GlobalRotationMetricsEntity?>
    suspend fun getUsageForCategory(category: String): List<ClothingUsageEntity>
    suspend fun commitOutfit(selectedProductIds: List<String>): Result<Unit>
}
