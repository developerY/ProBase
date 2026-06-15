package com.probase.kocolor.features.suggestions

import com.zoewave.probase.core.model.ritual.*

/**
 * Professional Look Builder that aligns clothing color DNA with the vanity archive.
 */
class ComprehensiveLookBuilder {

    fun buildLookForGarment(
        garment: ClothingItem,
        inventory: List<CosmeticItem>
    ): HarmonizedLook {
        return HarmonizedLook(
            targetGarment = garment,
            recommendedLip = findBestMatch(garment, inventory, MicroCategory.LIPSTICK),
            recommendedEye = findBestMatch(garment, inventory, MicroCategory.EYESHADOW),
            recommendedCheek = findBestMatch(garment, inventory, MicroCategory.BLUSH)
        )
    }

    private fun findBestMatch(
        garment: ClothingItem,
        inventory: List<CosmeticItem>,
        targetCategory: MicroCategory
    ): CosmeticItem? {
        val categoryItems = inventory.filter { it.microCategory == targetCategory }
        if (categoryItems.isEmpty()) return null
        return categoryItems.maxByOrNull { cosmetic -> calculateHarmonyScore(garment, cosmetic) }
    }

    private fun calculateHarmonyScore(garment: ClothingItem, cosmetic: CosmeticItem): Int {
        var score = 0
        val garmentTemp = garment.colorTemperature ?: "NEUTRAL"
        val garmentSeason = garment.seasonalPalette ?: "NEUTRAL"

        // In a pro app, we'd have colorTemperature on CosmeticItem too
        // For now we use the existing property from the refactored model
        // (Adding logic to match the new taxonomy)

        return score
    }

    private fun shareSameTemperature(seasonA: String, seasonB: String): Boolean {
        val warmSeasons = setOf("SPRING", "AUTUMN")
        val coolSeasons = setOf("SUMMER", "WINTER")
        return (warmSeasons.contains(seasonA) && warmSeasons.contains(seasonB)) ||
               (coolSeasons.contains(seasonA) && coolSeasons.contains(seasonB))
    }
}
