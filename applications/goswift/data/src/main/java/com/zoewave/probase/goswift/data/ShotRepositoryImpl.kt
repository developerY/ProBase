package com.zoewave.probase.goswift.data

import com.zoewave.probase.goswift.database.ShotDao
import com.zoewave.probase.goswift.database.asEntity
import com.zoewave.probase.goswift.database.asExternalModel
import com.zoewave.probase.goswift.model.CaffeineShot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShotRepositoryImpl @Inject constructor(
    private val shotDao: ShotDao
) : ShotRepository {
    override fun getAllShots(): Flow<List<CaffeineShot>> {
        return shotDao.getAllShots().map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override suspend fun addShot(shot: CaffeineShot) {
        shotDao.insertShot(shot.asEntity())
    }

    override suspend fun deleteShot(id: String) {
        shotDao.deleteShot(id)
    }
}
