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
     * Fetches usage stats for all items within a specific category by joining
     * with the canonical clothing items table.
     */
    @Query("""
        SELECT usage.* FROM clothing_usage AS usage
        INNER JOIN clothing_items AS item ON usage.productId = item.remoteId
        WHERE item.category = :category
    """)
    suspend fun getUsageForCategory(category: String): List<ClothingUsageEntity>

    @Query("SELECT * FROM clothing_usage WHERE productId IN (:productIds)")
    suspend fun getUsagesForProducts(productIds: List<String>): List<ClothingUsageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateGlobalMetrics(metrics: GlobalRotationMetricsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateGarmentUsages(usages: List<ClothingUsageEntity>)
    
    @Update
    suspend fun updateUsage(usage: ClothingUsageEntity)
}
