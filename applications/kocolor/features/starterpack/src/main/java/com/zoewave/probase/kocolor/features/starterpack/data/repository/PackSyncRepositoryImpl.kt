package com.zoewave.probase.kocolor.features.starterpack.data.repository

import android.util.Log
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.InstalledPackDao
import com.zoewave.probase.kocolor.db.entity.InstalledPackEntity
import com.zoewave.probase.kocolor.db.entity.PackStatus
import com.zoewave.probase.kocolor.features.starterpack.data.StarterPackRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackSyncRepositoryImpl @Inject constructor(
    private val starterPackRepository: StarterPackRepository,
    private val cosmeticDao: CosmeticDao,
    private val clothingDao: ClothingDao,
    private val installedPackDao: InstalledPackDao
) : PackSyncRepository {

    override fun getInstalledPacks(): Flow<List<InstalledPackEntity>> {
        return installedPackDao.getAllInstalledPacks()
    }

    override suspend fun fetchManifest(): Result<List<PackInfo>> = runCatching {
        val envelope = starterPackRepository.getManifest()
        envelope.data.packs
    }

    override suspend fun ingestPack(pack: PackInfo): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d("PackSyncRepo", "ingestPack: Starting secure ingestion for ${pack.id}")
            
            // 1. Mark as Downloading
            installedPackDao.insertPack(InstalledPackEntity(
                packId = pack.id,
                name = pack.name,
                description = pack.description,
                version = pack.version,
                status = PackStatus.DOWNLOADING,
                itemCount = pack.itemCount,
                sizeBytes = pack.compressedSizeBytes,
                hash = pack.sha256,
                packageHash = pack.sha256,
                heroImageUrl = pack.heroImageUrl,
                expiresAt = pack.expiresAt
            ))

            // 2. Fetch and Verify Package (the new binary pipeline)
            val items = starterPackRepository.fetchVerifiedPackage(pack)

            // 3. Map and Persist (Atomic Transaction)
            starterPackRepository.importItems(items)

            // 4. Finalize Installation
            installedPackDao.insertPack(InstalledPackEntity(
                packId = pack.id,
                name = pack.name,
                description = pack.description,
                version = pack.version,
                status = PackStatus.INSTALLED,
                itemCount = pack.itemCount,
                sizeBytes = pack.compressedSizeBytes,
                hash = pack.sha256,
                packageHash = pack.sha256,
                heroImageUrl = pack.heroImageUrl,
                expiresAt = pack.expiresAt
            ))
        }
    }

    override suspend fun wipePack(packId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d("PackSyncRepo", "wipePack: Deleting data for $packId")
            cosmeticDao.deleteCosmeticsByPackId(packId)
            clothingDao.deleteClothingByPackId(packId)
            installedPackDao.deletePackRecord(packId)
        }
    }
}
