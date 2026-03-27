package com.zoewave.probase.goswift.data

import com.zoewave.probase.goswift.model.CaffeineShot
import kotlinx.coroutines.flow.Flow

interface ShotRepository {
    fun getAllShots(): Flow<List<CaffeineShot>>
    suspend fun addShot(shot: CaffeineShot)
    suspend fun deleteShot(id: String)
}
