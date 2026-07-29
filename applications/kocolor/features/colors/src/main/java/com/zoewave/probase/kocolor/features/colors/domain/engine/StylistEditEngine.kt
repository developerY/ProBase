package com.zoewave.probase.kocolor.features.colors.domain.engine

import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.kocolor.features.colors.domain.model.ColorSignature
import com.zoewave.probase.kocolor.features.colors.domain.model.StylistEdit
import com.zoewave.probase.kocolor.features.colors.util.ColorScienceUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StylistEditEngine @Inject constructor() {

    fun generateEdit(userSeason: SeasonalType, inventory: List<ColorSignature>): StylistEdit {
        val vibe = analyzeVibe(inventory)
        val gaps = analyzeGaps(userSeason, inventory)
        
        val primaryInsight = when (vibe) {
            InventoryVibe.MINIMALIST -> "Your current collection leans heavily into Cool Neutrals. While sophisticated, it lacks the depth required for high-contrast seasonal styling."
            InventoryVibe.VIBRANT -> "Your collection is a vibrant explosion of color! To elevate your look, we recommend anchoring these tones with more structured deep neutrals."
            InventoryVibe.WARM_EARTH -> "You have a beautiful foundation of earthy tones. For your $userSeason profile, adding a few cool-toned highlights will create a more balanced radiance."
            InventoryVibe.COOL_SOPHISTICATE -> "Your collection is perfectly curated for a cool profile. To add a modern edge, consider integrating a few unexpected pops of high-saturation color."
        }

        val recColor1 = gaps.getOrNull(0) ?: "#000000"
        val recColor2 = gaps.getOrNull(1) ?: "#FFFFFF"
        val name1 = ColorScienceUtils.findNearestPantone(recColor1).name
        val name2 = ColorScienceUtils.findNearestPantone(recColor2).name

        val recommendation = "Integrating Deep Jewel Tones like $name1 and $name2 will anchor your silhouette and provide a radiant glow against your Roseate Sand undertones."

        return StylistEdit(
            primaryInsight = primaryInsight,
            recommendation = recommendation,
            anchorColors = listOf(recColor1, recColor2)
        )
    }

    private fun analyzeVibe(inventory: List<ColorSignature>): InventoryVibe {
        if (inventory.isEmpty()) return InventoryVibe.MINIMALIST
        
        var neutralCount = 0
        var warmCount = 0
        var coolCount = 0

        inventory.forEach { sig ->
            val hsv = ColorScienceUtils.hexToHsv(sig.hex)
            if (hsv != null) {
                if (hsv.s < 0.15f) {
                    neutralCount++
                } else {
                    if (hsv.h in 0f..80f || hsv.h in 320f..360f) warmCount++ else coolCount++
                }
            }
        }

        val total = inventory.size.toFloat()
        return when {
            neutralCount / total > 0.6f -> InventoryVibe.MINIMALIST
            warmCount / total > 0.5f -> InventoryVibe.WARM_EARTH
            coolCount / total > 0.5f -> InventoryVibe.COOL_SOPHISTICATE
            else -> InventoryVibe.VIBRANT
        }
    }

    private fun analyzeGaps(season: SeasonalType, inventory: List<ColorSignature>): List<String> {
        val idealPalette = getIdealPaletteForSeason(season)
        val inventoryHexes = inventory.map { it.hex }
        
        return idealPalette.filter { idealHex ->
            inventoryHexes.none { invHex -> 
                ColorScienceUtils.calculateDistance(idealHex, invHex) < 40.0 
            }
        }.take(2)
    }

    private fun getIdealPaletteForSeason(season: SeasonalType): List<String> {
        return when (season) {
            SeasonalType.WINTER -> listOf("#000080", "#800080", "#FF0000", "#006400", "#191970")
            SeasonalType.SUMMER -> listOf("#ADD8E6", "#E6E6FA", "#FFB6C1", "#98FB98", "#F5F5F5")
            SeasonalType.AUTUMN -> listOf("#8B4513", "#D2691E", "#CD853F", "#556B2F", "#A52A2A")
            SeasonalType.SPRING -> listOf("#FFFACD", "#FFA07A", "#FFD700", "#90EE90", "#00CED1")
            else -> listOf("#000000", "#FFFFFF")
        }
    }

    enum class InventoryVibe {
        MINIMALIST, VIBRANT, WARM_EARTH, COOL_SOPHISTICATE
    }
}
