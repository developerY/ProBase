package com.zoewave.probase.kocolor.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.core.model.ritual.Formality
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingDao {
    @Query("SELECT * FROM clothing_items ORDER BY timestamp DESC")
    fun getAllClothing(): Flow<List<ClothingItemEntity>>

    @Query("SELECT * FROM clothing_items WHERE category = :category ORDER BY timestamp DESC")
    fun getClothingByCategory(category: String): Flow<List<ClothingItemEntity>>

    @Query("SELECT * FROM clothing_items WHERE id = :id")
    fun getClothingById(id: Long): Flow<ClothingItemEntity?>

    @Query("SELECT * FROM clothing_items WHERE formality >= :minFormality ORDER BY timestamp DESC")
    fun getClothingByMinFormality(minFormality: Formality): Flow<List<ClothingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothing(item: ClothingItemEntity)

    @Update
    suspend fun updateClothing(item: ClothingItemEntity)

    @Query("DELETE FROM clothing_items WHERE id = :id")
    suspend fun deleteClothing(id: Long)
}
