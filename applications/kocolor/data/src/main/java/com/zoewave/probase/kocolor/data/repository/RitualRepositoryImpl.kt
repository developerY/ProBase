package com.zoewave.probase.kocolor.data.repository

import com.zoewave.probase.core.data.repository.RitualRepository
import com.zoewave.probase.core.model.ritual.BeautyRoutine
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RitualRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao,
    private val cosmeticDao: CosmeticDao
) : RitualRepository {

    override fun getRoutinesForDay(start: Long, end: Long): Flow<List<BeautyRoutine>> {
        return routineDao.getRoutinesForDay(start, end).map { list ->
            list.map { it.toModel() }
        }
    }

    override suspend fun updateRoutine(routine: BeautyRoutine) {
        routineDao.updateRoutine(routine.toEntity())
    }

    override fun getCosmeticById(id: Long): Flow<CosmeticItem?> {
        return cosmeticDao.getCosmeticById(id).map { it?.toModel() }
    }

    override suspend fun updateCosmetic(item: CosmeticItem) {
        cosmeticDao.updateCosmetic(item.toEntity())
    }
}
