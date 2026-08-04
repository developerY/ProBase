package com.zoewave.probase.kocolor.features.starterpack.data.repository

import android.util.Log
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.InstalledPackDao
import com.zoewave.probase.kocolor.db.entity.InstalledPackEntity
import com.zoewave.probase.kocolor.db.entity.PackStatus
import com.zoewave.probase.kocolor.features.starterpack.data.remote.KocolorApiService
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo
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
    private val wardrobeRepository: WardrobeRepository
) : PackSyncRepository {

    override fun getInstalledPacks(): Flow<List<InstalledPackEntity>> {
        return installedPackDao.getAllInstalledPacks()
    }

    override suspend fun fetchManifest(): Result<List<PackInfo>> = runCatching {
        Log.d("PackSyncRepo", "fetchManifest: Querying CDN...")
        apiService.getManifest().packs
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
                itemCount = pack.item_count
            ))

            // 2. Fetch the specific pack JSON
            val response = apiService.getPack(pack.endpoint)
            val sourceType = try { InventorySource.valueOf(pack.type) } catch (e: Exception) { InventorySource.UNKNOWN }

            // 3. Ingest Cosmetics
            response.cosmetics.forEach { dto ->
                val macro = MacroCategory.entries.find { it.displayName == dto.macroCategory } ?: MacroCategory.TOOLS
                val micro = try { MicroCategory.valueOf(dto.microCategory.uppercase()) } catch (e: Exception) { MicroCategory.OTHER }
                
                val item = CosmeticItem(
                    name = dto.name,
                    brand = dto.brand,
                    macroCategory = macro,
                    microCategory = micro,
                    formulation = try { Formulation.valueOf(dto.formulation.uppercase()) } catch (e: Exception) { Formulation.UNKNOWN },
                    chemistryBase = try { ChemistryBase.valueOf(dto.chemistryBase.uppercase()) } catch (e: Exception) { ChemistryBase.UNKNOWN },
                    finish = try { Finish.valueOf(dto.finish.uppercase()) } catch (e: Exception) { Finish.UNKNOWN },
                    coverage = try { Coverage.valueOf(dto.coverage.uppercase()) } catch (e: Exception) { Coverage.NOT_APPLICABLE },
                    temperature = try { Temperature.valueOf(dto.temperature.uppercase()) } catch (e: Exception) { Temperature.UNKNOWN },
                    colorHex = dto.colorHex,
                    shadeName = dto.shadeName,
                    imageUrl = dto.imageUrl,
                    notes = dto.notes,
                    instructions = dto.instructions,
                    batchCode = dto.batchCode,
                    paoMonths = dto.paoMonths,
                    price = dto.price,
                    volume = dto.volume,
                    heroIngredient = dto.heroIngredient,
                    skinCompatibility = dto.skinCompatibility,
                    containsFragrance = dto.containsFragrance,
                    ingredients = dto.ingredients,
                    allergens = dto.allergens,
                    ecoScore = dto.ecoScore,
                    isVegan = dto.isVegan,
                    isCrueltyFree = dto.isCrueltyFree,
                    recyclingInstructions = dto.recyclingInstructions,
                    ritualPlacement = dto.ritualPlacement,
                    sourceType = sourceType,
                    sourceName = pack.name,
                    sourcePackId = pack.id,
                    isFdaChecked = dto.isFdaChecked,
                    fdaRecallStatus = dto.fdaRecallStatus,
                    fdaAdverseEventCount = dto.fdaAdverseEventCount,
                    fdaClinicalWarnings = dto.fdaClinicalWarnings,
                    fdaTopReactions = dto.fdaTopReactions,
                    fdaActiveIngredients = dto.fdaActiveIngredients
                )
                cosmeticRepository.saveCosmeticItem(item)
            }

            // 4. Ingest Clothing
            response.clothing.forEach { dto ->
                val item = ClothingItem(
                    name = dto.name,
                    brand = dto.brand,
                    category = try { ClothingCategory.valueOf(dto.microCategory.uppercase()) } catch (e: Exception) { ClothingCategory.OTHER },
                    formality = try { Formality.valueOf(dto.formality.uppercase()) } catch (e: Exception) { Formality.CASUAL },
                    colorHex = dto.colorHex,
                    size = dto.size,
                    material = dto.material,
                    price = dto.price,
                    imageUrl = dto.imageUrl,
                    notes = dto.notes,
                    dominantHex = dto.dominantHex,
                    vibrantHex = dto.vibrantHex,
                    mutedHex = dto.mutedHex,
                    paletteHexes = dto.paletteHexes,
                    colorTemperature = dto.colorTemperature,
                    seasonalPalette = dto.seasonalPalette,
                    contrastLevel = dto.contrastLevel,
                    koColorGroup = dto.koColorGroup,
                    sourceType = sourceType,
                    sourceName = pack.name,
                    sourcePackId = pack.id
                )
                wardrobeRepository.saveClothingItem(item)
            }

            // 5. Finalize Installation
            installedPackDao.insertPack(InstalledPackEntity(
                packId = pack.id,
                name = pack.name,
                description = pack.description,
                version = pack.version,
                status = PackStatus.INSTALLED,
                itemCount = pack.item_count
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
