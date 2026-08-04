package com.zoewave.probase.kocolor.features.starterpack.data.repository

import android.util.Log
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.KocolorApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StarterPackRepositoryImpl @Inject constructor(
    private val apiService: KocolorApiService,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val wardrobeRepository: WardrobeRepository
) : StarterPackRepository {

    override suspend fun ingestStarterPack(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d("StarterPackRepo", "ingestStarterPack: Starting fetch from ${KocolorApiService.BASE_URL}")
            val response = apiService.getStarterPack()
            Log.d("StarterPackRepo", "ingestStarterPack: Received ${response.cosmetics.size} cosmetics and ${response.clothing.size} clothing items")

            // Ingest Cosmetics
            response.cosmetics.forEach { dto ->
                val macro = MacroCategory.entries.find { it.displayName == dto.macroCategory } ?: MacroCategory.TOOLS
                val micro = try { MicroCategory.valueOf(dto.microCategory.uppercase()) } catch (e: Exception) { 
                    MicroCategory.OTHER 
                }
                
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
                    sourcePackId = "starter_pack_v1",
                    expiryDate = dto.expiryDate,
                    fdaRecallStatus = dto.fdaRecallStatus,
                    fdaAdverseEventCount = dto.fdaAdverseEventCount,
                    fdaClinicalWarnings = dto.fdaClinicalWarnings,
                    fdaTopReactions = dto.fdaTopReactions,
                    fdaActiveIngredients = dto.fdaActiveIngredients,
                    isFdaChecked = dto.isFdaChecked
                )
                cosmeticRepository.saveCosmeticItem(item)
            }

            // Ingest Clothing
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
                    sourcePackId = "starter_pack_v1"
                )
                wardrobeRepository.saveClothingItem(item)
            }
        }
    }

    override suspend fun wipeStarterPack(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d("StarterPackRepo", "wipeStarterPack: Wiping starter pack items")
            val cosmeticResult = cosmeticRepository.deleteCosmeticsByPack("starter_pack_v1")
            val wardrobeResult = wardrobeRepository.deleteClothingByPack("starter_pack_v1")
            
            if (cosmeticResult.isFailure) throw cosmeticResult.exceptionOrNull()!!
            if (wardrobeResult.isFailure) throw wardrobeResult.exceptionOrNull()!!
        }
    }
}
