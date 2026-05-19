package com.probase.kocolor.features.suggestions

import com.probase.kocolor.model.ClothingItem
import com.probase.kocolor.model.CosmeticCategory
import com.probase.kocolor.model.CosmeticItem
import com.probase.kocolor.model.HarmonizedLook

class ComprehensiveLookBuilder {

    fun buildLookForGarment(
        garment: ClothingItem,
        inventory: List<CosmeticItem>
    ): HarmonizedLook {
        return HarmonizedLook(
            targetGarment = garment,
            recommendedLip = findBestMatch(garment, inventory, CosmeticCategory.LIP),
            recommendedEye = findBestMatch(garment, inventory, CosmeticCategory.EYE),
            recommendedCheek = findBestMatch(garment, inventory, CosmeticCategory.CHEEK)
        )
    }

    private fun findBestMatch(
        garment: ClothingItem,
        inventory: List<CosmeticItem>,
        targetCategory: CosmeticCategory
    ): CosmeticItem? {
        val categoryItems = inventory.filter { it.category == targetCategory }
        if (categoryItems.isEmpty()) return null
        return categoryItems.maxByOrNull { cosmetic -> calculateHarmonyScore(garment, cosmetic) }
    }

    private fun calculateHarmonyScore(garment: ClothingItem, cosmetic: CosmeticItem): Int {
        var score = 0
        val garmentTemp = garment.colorTemperature ?: "NEUTRAL"
        val garmentSeason = garment.seasonalPalette ?: "NEUTRAL"

        if (cosmetic.colorTemperature == garmentTemp) score += 10
        else if (garmentTemp == "NEUTRAL" || cosmetic.colorTemperature == "NEUTRAL") score += 5
        else score -= 5

        if (cosmetic.seasonalPalette == garmentSeason) score += 8
        else if (shareSameTemperature(garmentSeason, cosmetic.seasonalPalette)) score += 3

        return score
    }

    private fun shareSameTemperature(seasonA: String, seasonB: String): Boolean {
        val warmSeasons = setOf("SPRING", "AUTUMN")
        val coolSeasons = setOf("SUMMER", "WINTER")
        return (warmSeasons.contains(seasonA) && warmSeasons.contains(seasonB)) ||
               (coolSeasons.contains(seasonA) && coolSeasons.contains(seasonB))
    }
}
