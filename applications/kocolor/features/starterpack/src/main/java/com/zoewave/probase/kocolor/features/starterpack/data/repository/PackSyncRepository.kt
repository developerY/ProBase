package com.zoewave.probase.kocolor.features.starterpack.data.repository

import com.zoewave.probase.kocolor.db.entity.InstalledPackEntity
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo
import kotlinx.coroutines.flow.Flow

interface PackSyncRepository {
    fun getInstalledPacks(): Flow<List<InstalledPackEntity>>
    suspend fun fetchManifest(): Result<List<PackInfo>>
    suspend fun ingestPack(pack: PackInfo): Result<Unit>
    suspend fun wipePack(packId: String): Result<Unit>
}
