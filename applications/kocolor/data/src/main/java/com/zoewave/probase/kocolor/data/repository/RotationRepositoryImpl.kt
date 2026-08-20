package com.zoewave.probase.kocolor.data.repository

import com.zoewave.probase.kocolor.db.KoColorDatabase
import com.zoewave.probase.kocolor.db.dao.GarmentRotationDao
import com.zoewave.probase.kocolor.db.entity.ClothingUsageEntity
import com.zoewave.probase.kocolor.db.entity.GlobalRotationMetricsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

    override suspend fun getUsageForCategory(rotationCategoryId: String): List<ClothingUsageEntity> = withContext(Dispatchers.IO) {
        rotationDao.getUsageForCategory(rotationCategoryId)
    }

    override suspend fun commitOutfit(selectedProductIds: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            database.commitOutfitUsage(selectedProductIds)
        }
    }
}
