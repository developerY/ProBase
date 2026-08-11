package com.zoewave.probase.kocolor.features.colors.data.repository

import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.features.colors.domain.engine.StylistEditEngine
import com.zoewave.probase.kocolor.features.colors.domain.model.ColorSignature
import com.zoewave.probase.kocolor.features.colors.domain.model.HarmonyMode
import com.zoewave.probase.kocolor.features.colors.domain.model.SourceType
import com.zoewave.probase.kocolor.features.colors.domain.model.StylistEdit
import com.zoewave.probase.kocolor.features.colors.domain.repository.ColorIntelligenceRepository
import com.zoewave.probase.kocolor.features.colors.util.ColorScienceUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ColorIntelligenceRepositoryImpl @Inject constructor(
    private val wardrobeRepository: WardrobeRepository,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val stylistEditEngine: StylistEditEngine
) : ColorIntelligenceRepository {

    override fun getAllInventoryColors(): Flow<List<ColorSignature>> {
        return combine(
            wardrobeRepository.getAllClothing(),
            cosmeticRepository.getAllCosmetics()
        ) { wardrobe, cosmetics ->
            val wardrobeColors = wardrobe.mapNotNull { item ->
                val hex = item.dominantHex ?: item.colorHex
                if (hex != null) {
                    ColorSignature(hex, item.internalId, SourceType.WARDROBE, item.name)
                } else null
            }
            val cosmeticColors = cosmetics.mapNotNull { item ->
                if (item.colorHex.isNotBlank()) {
                    ColorSignature(item.colorHex, item.internalId, SourceType.VANITY, item.name)
                } else null
            }
            wardrobeColors + cosmeticColors
        }
    }

    override fun getColorsByHarmony(baseHex: String, mode: HarmonyMode): Flow<List<ColorSignature>> {
        return getAllInventoryColors().map { allColors ->
            allColors.filter { sig ->
                isMatch(baseHex, sig.hex, mode)
            }
        }
    }

    private fun isMatch(target: String, item: String, mode: HarmonyMode): Boolean {
        return when (mode) {
            HarmonyMode.EXACT -> ColorScienceUtils.calculateDistance(target, item) < 50.0
            HarmonyMode.COMPLEMENTARY -> {
                val comp = ColorScienceUtils.getComplementary(target)
                ColorScienceUtils.calculateDistance(comp, item) < 80.0
            }
            HarmonyMode.ANALOGOUS -> {
                ColorScienceUtils.getAnalogous(target).any { 
                    ColorScienceUtils.calculateDistance(it, item) < 60.0 
                }
            }
            HarmonyMode.TRIADIC -> {
                ColorScienceUtils.getTriadic(target).any { 
                    ColorScienceUtils.calculateDistance(it, item) < 60.0 
                }
            }
            HarmonyMode.MONOCHROMATIC -> {
                ColorScienceUtils.getMonochromatic(target).any { 
                    ColorScienceUtils.calculateDistance(it, item) < 60.0 
                }
            }
            HarmonyMode.SPLIT_COMPLEMENTARY -> {
                 // Placeholder for further refinement
                 false
            }
            HarmonyMode.TETRADIC -> {
                 // Placeholder for further refinement
                 false
            }
        }
    }

    override fun getPaletteGaps(userSeason: SeasonalType): Flow<List<String>> {
        return getAllInventoryColors().map { inventory ->
             val inventoryHexes = inventory.map { it.hex }
             val seasonalPalette = getIdealPaletteForSeason(userSeason)
             
             seasonalPalette.filter { idealHex ->
                 inventoryHexes.none { invHex -> 
                     ColorScienceUtils.calculateDistance(idealHex, invHex) < 40.0 
                 }
             }
        }
    }

    override fun getStylistEdit(userSeason: SeasonalType): Flow<StylistEdit> {
        return getAllInventoryColors().map { inventory ->
            stylistEditEngine.generateEdit(userSeason, inventory)
        }
    }

    private fun getIdealPaletteForSeason(season: SeasonalType): List<String> {
        return when (season) {
            SeasonalType.WINTER -> listOf("#1D1D1D", "#FFFFFF", "#C0C0C0", "#000080", "#800080", "#FF0000")
            SeasonalType.SUMMER -> listOf("#F5F5F5", "#ADD8E6", "#E6E6FA", "#FFB6C1", "#98FB98", "#87CEEB")
            SeasonalType.AUTUMN -> listOf("#8B4513", "#D2691E", "#CD853F", "#556B2F", "#808000", "#A52A2A")
            SeasonalType.SPRING -> listOf("#FFFACD", "#FFA07A", "#FFD700", "#90EE90", "#00CED1", "#FF69B4")
            else -> emptyList()
        }
    }
}
