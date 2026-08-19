package com.zoewave.probase.kocolor.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.zoewave.probase.core.model.ritual.Formality
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingDao {
    @Query("SELECT * FROM clothing_items ORDER BY timestamp DESC")
    fun getAllClothing(): Flow<List<ClothingItemEntity>>

    @Query("SELECT * FROM clothing_items WHERE category = :category ORDER BY timestamp DESC")
    fun getClothingByCategory(category: String): Flow<List<ClothingItemEntity>>

    @Query("SELECT * FROM clothing_items WHERE internalId = :id")
    fun getClothingById(id: Long): Flow<ClothingItemEntity?>

    @Query("SELECT remoteId FROM clothing_items WHERE remoteId IS NOT NULL")
    fun getOwnedClothingIds(): Flow<List<String>>

    @Query("SELECT * FROM clothing_items WHERE formality >= :minFormality ORDER BY timestamp DESC")
    fun getClothingByMinFormality(minFormality: Formality): Flow<List<ClothingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothing(item: ClothingItemEntity)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothingList(items: List<ClothingItemEntity>)

    @Update
    suspend fun updateClothing(item: ClothingItemEntity)

    @Query("DELETE FROM clothing_items WHERE internalId = :id")
    suspend fun deleteClothing(id: Long)

    @Transaction
    @Query("DELETE FROM clothing_items WHERE provenance_packId = :packId")
    suspend fun deleteClothingByPackId(packId: String)

    @Query("DELETE FROM clothing_items")
    suspend fun deleteAllClothing()

    /**
     * "MAKE IT MINE" Action: Clones a clothing item and detaches it from the collection lifecycle.
     */
    @Transaction
    @Query("""
        INSERT INTO clothing_items (
            name, brand, category, formality, colorHex, colorFamily, size, material, price, 
            imageUrl, notes, timestamp, dominantHex, vibrantHex, mutedHex, paletteHexes, 
            colorTemperature, seasonalPalette, contrastLevel, koColorGroup, sourceType, 
            sourceName, provenance_packId
        )
        SELECT 
            name, brand, category, formality, colorHex, colorFamily, size, material, price, 
            imageUrl, notes, :timestamp, dominantHex, vibrantHex, mutedHex, paletteHexes, 
            colorTemperature, seasonalPalette, contrastLevel, koColorGroup, 'USER_SCAN', 
            sourceName, NULL
        FROM clothing_items 
        WHERE internalId = :sourceInternalId
    """)
    suspend fun cloneToPersonalArchive(sourceInternalId: Long, timestamp: Long = System.currentTimeMillis())
}
