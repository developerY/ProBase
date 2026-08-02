package com.zoewave.probase.kocolor.data.repository

import com.zoewave.probase.core.model.ritual.ClothingItem
import kotlinx.coroutines.flow.Flow

interface WardrobeRepository {
    fun getAllClothing(): Flow<List<ClothingItem>>
    fun getShortlistByIntent(intent: String): Flow<List<ClothingItem>>
    fun getClothingById(id: Long): Flow<ClothingItem?>
    suspend fun saveClothingItem(item: ClothingItem)
    suspend fun wearClothingItem(id: Long)
    suspend fun deleteClothing(id: Long)
    suspend fun ingestStarterPack(): Result<Unit>
}
