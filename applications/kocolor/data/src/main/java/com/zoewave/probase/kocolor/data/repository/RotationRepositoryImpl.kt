package com.zoewave.probase.kocolor.data.repository

import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.data.model.ClothingWithUsage
import com.zoewave.probase.kocolor.db.KoColorDatabase
import com.zoewave.probase.kocolor.db.dao.GarmentRotationDao
import com.zoewave.probase.kocolor.db.entity.ClothingUsageEntity
import com.zoewave.probase.kocolor.db.entity.GlobalRotationMetricsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RotationRepositoryImpl @Inject constructor(
    private val database: KoColorDatabase,
    private val rotationDao: GarmentRotationDao
) : RotationRepository {

    override fun observeGlobalMetrics(): Flow<GlobalRotationMetricsEntity?> {
        return rotationDao.observeGlobalMetrics()
    }

    override fun observeAllUsages(): Flow<List<ClothingUsageEntity>> {
        return rotationDao.observeAllUsages()
    }

    override fun observeAllClothingWithUsage(): Flow<List<ClothingWithUsage>> {
        return rotationDao.observeAllClothingWithUsage().map { list ->
            list.map { wrapper ->
                ClothingWithUsage(
                    garment = wrapper.garment.toModel(),
                    usage = wrapper.usage
                )
            }
        }
    }

    override suspend fun getUsageForCategory(categoryId: String): List<ClothingUsageEntity> = withContext(Dispatchers.IO) {
        rotationDao.getUsageForCategory(categoryId)
    }

    override suspend fun commitOutfitUsage(productIds: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            database.commitOutfitUsage(productIds)
        }
    }
}
