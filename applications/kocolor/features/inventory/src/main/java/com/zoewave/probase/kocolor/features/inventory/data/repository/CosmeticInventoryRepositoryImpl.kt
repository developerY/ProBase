package com.zoewave.probase.kocolor.features.inventory.data.repository

import android.util.Log
import com.zoewave.probase.core.util.color.ColorQuantizer
import com.zoewave.probase.kocolor.features.obf.data.repository.ObfRepository
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.remote.KocolorApiService
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.core.model.ritual.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CosmeticInventoryRepositoryImpl @Inject constructor(
    private val obfRepository: ObfRepository,
    private val cosmeticDao: CosmeticDao,
    private val apiService: KocolorApiService
) : CosmeticInventoryRepository {

    override fun getAllCosmetics(): Flow<List<CosmeticItem>> {
        return cosmeticDao.getAllCosmetics().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun fetchProductByBarcode(barcode: String): Result<CosmeticItem> {
        android.util.Log.d("CosmeticRepo", "fetchProductByBarcode: Fetching $barcode from OBF")
        return obfRepository.fetchProductByBarcode(barcode)
    }

    override suspend fun saveCosmeticItem(item: CosmeticItem): Long {
        val bucketedItem = item.copy(
            colorFamily = ColorQuantizer.snapToFamily(item.colorHex)
        )
        return if (bucketedItem.id == 0L) {
            cosmeticDao.insertCosmetic(bucketedItem.toEntity())
        } else {
            cosmeticDao.updateCosmetic(bucketedItem.toEntity())
            bucketedItem.id
        }
    }

    override suspend fun deleteCosmeticItem(id: Long) {
        cosmeticDao.deleteCosmetic(id)
    }

    override suspend fun ingestStarterPack(): Result<Unit> = runCatching {
        Log.d("CosmeticRepo", "ingestStarterPack: Starting fetch from ${KocolorApiService.BASE_URL}")
        val response = apiService.getStarterPack()
        Log.d("CosmeticRepo", "ingestStarterPack: Received ${response.cosmetics.size} cosmetics and ${response.clothing.size} clothing items")
        
        response.cosmetics.forEach { dto ->
            Log.d("CosmeticRepo", "ingestStarterPack: Processing cosmetic: ${dto.name} (${dto.id})")
            val macro = MacroCategory.entries.find { it.displayName == dto.macroCategory } ?: MacroCategory.TOOLS
            val micro = try { MicroCategory.valueOf(dto.microCategory.uppercase()) } catch (e: Exception) { 
                Log.w("CosmeticRepo", "ingestStarterPack: Unknown microCategory ${dto.microCategory}, falling back to OTHER")
                MicroCategory.OTHER 
            }
            
            val item = CosmeticItem(
                name = dto.name,
                brand = dto.brand,
                macroCategory = macro,
                microCategory = micro,
                formulation = try { Formulation.valueOf(dto.formulation.uppercase()) } catch (e: Exception) { 
                    Log.w("CosmeticRepo", "ingestStarterPack: Unknown formulation ${dto.formulation}")
                    Formulation.UNKNOWN 
                },
                chemistryBase = try { ChemistryBase.valueOf(dto.chemistryBase.uppercase()) } catch (e: Exception) { 
                    Log.w("CosmeticRepo", "ingestStarterPack: Unknown chemistryBase ${dto.chemistryBase}")
                    ChemistryBase.UNKNOWN 
                },
                finish = try { Finish.valueOf(dto.finish.uppercase()) } catch (e: Exception) { 
                    Log.w("CosmeticRepo", "ingestStarterPack: Unknown finish ${dto.finish}")
                    Finish.UNKNOWN 
                },
                coverage = try { Coverage.valueOf(dto.coverage.uppercase()) } catch (e: Exception) { 
                    Log.w("CosmeticRepo", "ingestStarterPack: Unknown coverage ${dto.coverage}")
                    Coverage.NOT_APPLICABLE 
                },
                temperature = try { Temperature.valueOf(dto.temperature.uppercase()) } catch (e: Exception) { 
                    Log.w("CosmeticRepo", "ingestStarterPack: Unknown temperature ${dto.temperature}")
                    Temperature.UNKNOWN 
                },
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
                expiryDate = dto.expiryDate,
                fdaRecallStatus = dto.fdaRecallStatus,
                fdaAdverseEventCount = dto.fdaAdverseEventCount,
                fdaClinicalWarnings = dto.fdaClinicalWarnings,
                fdaTopReactions = dto.fdaTopReactions,
                fdaActiveIngredients = dto.fdaActiveIngredients,
                isFdaChecked = dto.isFdaChecked
            )
            val savedId = saveCosmeticItem(item)
            android.util.Log.d("CosmeticRepo", "ingestStarterPack: Saved cosmetic ${dto.name} with local ID: $savedId")
        }
    }.onFailure { e ->
        android.util.Log.e("CosmeticRepo", "ingestStarterPack: FAILED", e)
    }
}
