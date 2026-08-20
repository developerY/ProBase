package com.zoewave.probase.kocolor.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.zoewave.probase.kocolor.db.entity.ClothingUsageEntity
import com.zoewave.probase.kocolor.db.entity.GlobalRotationMetricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GarmentRotationDao {

    @Query("SELECT * FROM global_rotation_metrics WHERE metricsId = 0")
    fun observeGlobalMetrics(): Flow<GlobalRotationMetricsEntity?>

    @Query("SELECT * FROM global_rotation_metrics WHERE metricsId = 0")
    suspend fun getGlobalMetrics(): GlobalRotationMetricsEntity?

    /**
     * Fetches usage stats for all items within a specific rotation category.
     */
    @Query("SELECT * FROM clothing_usage WHERE rotationCategoryId = :rotationCategoryId")
    suspend fun getUsageForCategory(rotationCategoryId: String): List<ClothingUsageEntity>

    @Query("SELECT * FROM clothing_usage WHERE productId IN (:productIds)")
    suspend fun getUsagesForProducts(productIds: List<String>): List<ClothingUsageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateGlobalMetrics(metrics: GlobalRotationMetricsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateGarmentUsages(usages: List<ClothingUsageEntity>)
    
    @Update
    suspend fun updateUsage(usage: ClothingUsageEntity)
}
