package com.zoewave.probase.kocolor.db.dao

import androidx.room3.*
import com.zoewave.probase.kocolor.db.entity.InventoryItemEntity
import com.zoewave.probase.kocolor.db.entity.InventoryType
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE type = :type ORDER BY timestamp DESC")
    fun getItemsByType(type: InventoryType): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)
}
