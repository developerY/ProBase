package com.zoewave.probase.kocolor.features.starterpack.data.repository

import android.util.Log
import com.zoewave.probase.kocolor.db.KoColorDatabase
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.InstalledPackDao
import com.zoewave.probase.kocolor.db.entity.InstalledPackEntity
import com.zoewave.probase.kocolor.db.entity.PackStatus
import com.zoewave.probase.kocolor.features.starterpack.data.StarterPackRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItem
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.core.util.color.ColorQuantizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackSyncRepositoryImpl @Inject constructor(
    private val starterPackRepository: StarterPackRepository,
    private val database: KoColorDatabase,
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
            
            // 1. Mark as Downloading (Pre-transaction, so UI updates immediately)
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

            // 2. Fetch and Verify Package (Phone Hub - heavy compute/network outside transaction)
            val items = starterPackRepository.fetchVerifiedPackage(pack)
            
            // 3. Map and Persist
            importSelectedItems(pack.id, items).getOrThrow()

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

    override suspend fun importSelectedItems(packId: String, items: List<PackItem>): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d("PackSyncRepo", "importSelectedItems: Importing ${items.size} items from $packId")
            
            // 1. Get the PackInfo from manifest for metadata
            val envelope = starterPackRepository.getManifest()
            val packInfo = envelope.data.packs.find { it.id == packId } 
                ?: throw Exception("Pack $packId not found in manifest.")

            // 2. Map to domain (Preparation)
            val (cosmeticItems, clothingItems) = starterPackRepository.mapToDomainItems(items, packInfo)

            // 3. Map to entities with quantization
            val cosmeticEntities = cosmeticItems.map { item ->
                item.copy(colorFamily = ColorQuantizer.snapToFamily(item.colorHex)).toEntity()
            }
            val clothingEntities = clothingItems.map { item ->
                item.copy(colorFamily = ColorQuantizer.snapToFamily(item.colorHex)).toEntity()
            }
            
            // 4. Commit to DB
            cosmeticDao.insertCosmetics(cosmeticEntities)
            clothingDao.insertClothingList(clothingEntities)

            // 5. Pre-fetch Images (Post-ingestion)
            starterPackRepository.prefetchImages(items)
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
