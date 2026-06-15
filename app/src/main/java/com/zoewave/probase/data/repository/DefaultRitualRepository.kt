package com.zoewave.probase.data.repository

import com.zoewave.probase.core.data.repository.RitualRepository
import com.zoewave.probase.core.model.ritual.BeautyRoutine
import com.zoewave.probase.core.model.ritual.CosmeticItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultRitualRepository @Inject constructor() : RitualRepository {
    override fun getRoutinesForDay(start: Long, end: Long): Flow<List<BeautyRoutine>> = flowOf(emptyList())

    override suspend fun updateRoutine(routine: BeautyRoutine) {
        // No-op implementation for main app shell
    }

    override fun getCosmeticById(id: Long): Flow<CosmeticItem?> = flowOf(null)

    override suspend fun updateCosmetic(item: CosmeticItem) {
        // No-op implementation for main app shell
    }
}
