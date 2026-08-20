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

    @Query("SELECT * FROM clothing_usage")
    fun observeAllUsages(): Flow<List<ClothingUsageEntity>>

    /**
     * Fetches usage statistics by joining [ClothingUsageEntity] with 
     * canonical [ClothingItemEntity] category metadata.
     */
    @Query("""
        SELECT usage.* FROM clothing_usage AS usage
        INNER JOIN clothing_items AS item ON usage.productId = item.remoteId
        WHERE item.category = :categoryId
    """)
    suspend fun getUsageForCategory(categoryId: String): List<ClothingUsageEntity>

    @Query("SELECT * FROM clothing_usage WHERE productId = :productId LIMIT 1")
    suspend fun getUsageForProduct(productId: String): ClothingUsageEntity?

    @Query("SELECT * FROM clothing_usage WHERE productId IN (:productIds)")
    suspend fun getUsagesForProducts(productIds: List<String>): List<ClothingUsageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateGlobalMetrics(metrics: GlobalRotationMetricsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateGarmentUsages(usages: List<ClothingUsageEntity>)
    
    @Update
    suspend fun updateUsage(usage: ClothingUsageEntity)
}
