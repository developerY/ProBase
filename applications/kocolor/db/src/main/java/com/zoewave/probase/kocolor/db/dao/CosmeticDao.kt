package com.zoewave.probase.kocolor.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.zoewave.probase.core.model.ritual.MicroCategory
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

    @Query("SELECT * FROM cosmetic_items WHERE microCategory = :microCategory ORDER BY timestamp DESC")
    fun getCosmeticsByMicroCategory(microCategory: MicroCategory): Flow<List<CosmeticItemEntity>>

    @Query("SELECT * FROM cosmetic_items WHERE internalId = :id")
    fun getCosmeticById(id: Long): Flow<CosmeticItemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCosmetic(item: CosmeticItemEntity): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCosmetics(items: List<CosmeticItemEntity>)

    @Update
    suspend fun updateCosmetic(item: CosmeticItemEntity)

    @Query("DELETE FROM cosmetic_items WHERE internalId = :id")
    suspend fun deleteCosmetic(id: Long)

    @Transaction
    @Query("DELETE FROM cosmetic_items WHERE provenance_packId = :packId")
    suspend fun deleteCosmeticsByPackId(packId: String)

    @Query("DELETE FROM cosmetic_items")
    suspend fun deleteAllCosmetics()

    /**
     * "MAKE IT MINE" Action: Clones a product and detaches it from the collection lifecycle.
     * The new item has no provenance (provenance_packId is NULL), protecting it from collection wipes.
     */
    @Transaction
    @Query("""
        INSERT INTO cosmetic_items (
            name, brand, macroCategory, microCategory, formulation, chemistryBase, finish, coverage, 
            temperature, colorHex, colorFamily, shadeName, imageUrl, notes, instructions, timestamp,
            paoMonths, price, volume, ingredients, allergens, isVegan, isCrueltyFree, fdaDataVerified,
            sourceType, sourceName, provenance_packId
        )
        SELECT 
            name, brand, macroCategory, microCategory, formulation, chemistryBase, finish, coverage, 
            temperature, colorHex, colorFamily, shadeName, imageUrl, notes, instructions, :timestamp,
            paoMonths, price, volume, ingredients, allergens, isVegan, isCrueltyFree, fdaDataVerified,
            'USER_SCAN', sourceName, NULL
        FROM cosmetic_items 
        WHERE internalId = :sourceInternalId
    """)
    suspend fun cloneToPersonalArchive(sourceInternalId: Long, timestamp: Long = System.currentTimeMillis())
}
