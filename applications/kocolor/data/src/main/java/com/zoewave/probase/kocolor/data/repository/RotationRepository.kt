package com.zoewave.probase.kocolor.data.repository

import com.zoewave.probase.kocolor.data.model.ClothingWithUsage
import com.zoewave.probase.kocolor.db.entity.ClothingUsageEntity
import com.zoewave.probase.kocolor.db.entity.GlobalRotationMetricsEntity
import kotlinx.coroutines.flow.Flow

interface RotationRepository {
    fun observeGlobalMetrics(): Flow<GlobalRotationMetricsEntity?>
    fun observeAllUsages(): Flow<List<ClothingUsageEntity>>
    fun observeAllClothingWithUsage(): Flow<List<ClothingWithUsage>>
    suspend fun getUsageForCategory(categoryId: String): List<ClothingUsageEntity>
    suspend fun commitOutfitUsage(productIds: List<String>): Result<Unit>
}
