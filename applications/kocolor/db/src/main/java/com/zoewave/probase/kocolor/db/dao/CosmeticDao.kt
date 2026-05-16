package com.zoewave.probase.kocolor.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CosmeticDao {
    @Query("SELECT * FROM cosmetic_items ORDER BY timestamp DESC")
    fun getAllCosmetics(): Flow<List<CosmeticItemEntity>>

    /**
     * Retrieves all items sorted by their expiration date (FEFO: First Expired, First Out).
     * Items without an expiry date are listed last.
     */
    @Query("SELECT * FROM cosmetic_items ORDER BY CASE WHEN expiryDate IS NULL THEN 1 ELSE 0 END, expiryDate ASC")
    fun getAllCosmeticsFEFO(): Flow<List<CosmeticItemEntity>>

    @Query("SELECT * FROM cosmetic_items WHERE category = :category ORDER BY timestamp DESC")
    fun getCosmeticsByCategory(category: String): Flow<List<CosmeticItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCosmetic(item: CosmeticItemEntity)

    @Update
    suspend fun updateCosmetic(item: CosmeticItemEntity)

    @Query("DELETE FROM cosmetic_items WHERE id = :id")
    suspend fun deleteCosmetic(id: Long)
}
