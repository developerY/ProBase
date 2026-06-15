package com.zoewave.probase.core.data.repository

import com.zoewave.probase.core.model.ritual.BeautyRoutine
import com.zoewave.probase.core.model.ritual.CosmeticItem
import kotlinx.coroutines.flow.Flow

interface RitualRepository {
    fun getRoutinesForDay(start: Long, end: Long): Flow<List<BeautyRoutine>>
    suspend fun updateRoutine(routine: BeautyRoutine)
    fun getCosmeticById(id: Long): Flow<CosmeticItem?>
    suspend fun updateCosmetic(item: CosmeticItem)
}
