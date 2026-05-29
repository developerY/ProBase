package com.zoewave.probase.kocolor.data.repository

import com.zoewave.probase.kocolor.model.CosmeticItem
import kotlinx.coroutines.flow.Flow

interface CosmeticInventoryRepository {
    fun getAllCosmetics(): Flow<List<CosmeticItem>>
    suspend fun fetchProductByBarcode(barcode: String): Result<CosmeticItem>
    suspend fun saveCosmeticItem(item: CosmeticItem)
    suspend fun deleteCosmeticItem(id: Long)
}
