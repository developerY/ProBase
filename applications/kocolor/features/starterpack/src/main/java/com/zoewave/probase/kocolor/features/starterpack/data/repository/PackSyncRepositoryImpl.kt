package com.zoewave.probase.kocolor.features.starterpack.data.repository

import android.util.Log
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.InstalledPackDao
import com.zoewave.probase.kocolor.db.entity.InstalledPackEntity
import com.zoewave.probase.kocolor.db.entity.PackStatus
import com.zoewave.probase.kocolor.features.starterpack.data.PackException
import com.zoewave.probase.kocolor.features.starterpack.data.remote.KocolorApiService
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo
import com.zoewave.probase.kocolor.features.starterpack.domain.security.SignatureVerifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackSyncRepositoryImpl @Inject constructor(
    private val apiService: KocolorApiService,
    private val cosmeticDao: CosmeticDao,
    private val clothingDao: ClothingDao,
    private val installedPackDao: InstalledPackDao,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val signatureVerifier: SignatureVerifier
) : PackSyncRepository {

    override fun getInstalledPacks(): Flow<List<InstalledPackEntity>> {
        return installedPackDao.getAllInstalledPacks()
    }

    override suspend fun fetchManifest(): Result<List<PackInfo>> = runCatching {
        Log.d("PackSyncRepo", "fetchManifest: Querying CDN...")
        val envelope = apiService.getManifest()
        
        // 1. Verify signature
        if (!signatureVerifier.verify(envelope.data.toString(), envelope.signature)) {
            throw PackException.SignatureException("Manifest signature verification failed!")
        }
        
        envelope.data.packs
    }

    override suspend fun ingestPack(pack: PackInfo): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d("PackSyncRepo", "ingestPack: Starting ingestion for ${pack.id}")
            
            // 1. Mark as Downloading
            installedPackDao.insertPack(InstalledPackEntity(
                packId = pack.id,
                name = pack.name,
                description = pack.description,
                version = pack.version,
                status = PackStatus.DOWNLOADING,
                itemCount = pack.itemCount,
                sizeBytes = pack.sizeBytes ?: 0L,
                hash = pack.hash,
                heroImageUrl = pack.heroImageUrl,
                expiresAt = pack.expiresAt
            ))

            // 2. Fetch the specific pack JSON via envelope
            val envelope = try {
                apiService.getPack(pack.endpoint)
            } catch (e: Exception) {
                throw PackException.DownloadException("Failed to fetch pack ${pack.id}", e)
            }

            // 3. Verify signature
            if (!signatureVerifier.verify(envelope.data.toString(), envelope.signature)) {
                throw PackException.SignatureException("Pack ${pack.id} signature verification failed!")
            }

            val items = envelope.data
            val sourceType = try { InventorySource.valueOf(pack.type) } catch (e: Exception) { InventorySource.UNKNOWN }

            val provenance = Provenance(
                packId = pack.id,
                packageVersion = envelope.packageVersion,
                schemaVersion = envelope.schemaVersion,
                publisher = "KoColor Official",
                installedAtTimestamp = System.currentTimeMillis(),
                verificationState = VerificationState.VERIFIED
            )

            // 4. Ingest Cosmetics
            items.forEach { dto ->
                val macro = MacroCategory.entries.find { it.name == (dto.macroCategory?.uppercase() ?: "") } ?: MacroCategory.COMPLEXION
                val micro = try { MicroCategory.valueOf(dto.microCategory?.uppercase() ?: "") } catch (e: Exception) { MicroCategory.FOUNDATION }
                
                val item = CosmeticItem(
                    name = dto.name,
                    brand = dto.brand,
                    macroCategory = macro,
                    microCategory = micro,
                    formulation = try { Formulation.valueOf(dto.formulation?.uppercase() ?: "") } catch (e: Exception) { Formulation.UNKNOWN },
                    chemistryBase = try { ChemistryBase.valueOf(dto.chemistryBase?.uppercase() ?: "") } catch (e: Exception) { ChemistryBase.UNKNOWN },
                    finish = try { Finish.valueOf(dto.finish?.uppercase() ?: "") } catch (e: Exception) { Finish.UNKNOWN },
                    coverage = try { Coverage.valueOf(dto.coverage?.uppercase() ?: "") } catch (e: Exception) { Coverage.NOT_APPLICABLE },
                    temperature = try { Temperature.valueOf(dto.temperature?.uppercase() ?: "") } catch (e: Exception) { Temperature.UNKNOWN },
                    colorHex = dto.hexColor,
                    shadeName = dto.shade,
                    imageUrl = dto.imageUrl,
                    sourceType = sourceType,
                    sourceName = pack.name,
                    provenance = provenance
                )
                cosmeticRepository.saveCosmeticItem(item)
            }

            // 5. Finalize Installation
            installedPackDao.insertPack(InstalledPackEntity(
                packId = pack.id,
                name = pack.name,
                description = pack.description,
                version = pack.version,
                status = PackStatus.INSTALLED,
                itemCount = pack.itemCount,
                sizeBytes = pack.sizeBytes ?: 0L,
                hash = pack.hash,
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
