package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleGapAnalyzer @Inject constructor() {

    /**
     * Determines missing role requirements based on current anchors, occasion, and weather temperature.
     */
    fun determineRoleRequirements(
        anchors: List<ClothingItem>,
        occasion: String,
        weatherTempC: Float
    ): List<RoleRequirement> {
        val presentCategories = anchors.map { it.category }.toSet()
        val requirements = mutableListOf<RoleRequirement>()

        // 1. Tops (if no dress or top is anchored)
        val hasDress = presentCategories.contains(ClothingCategory.DRESSES)
        val hasTop = presentCategories.contains(ClothingCategory.TOPS)
        if (!hasDress && !hasTop) {
            requirements.add(RoleRequirement(role = ClothingCategory.TOPS.name, minCount = 1, maxCount = 2))
        }

        // 2. Bottoms (if no dress or bottom is anchored)
        val hasBottom = presentCategories.contains(ClothingCategory.BOTTOMS)
        if (!hasDress && !hasBottom) {
            requirements.add(RoleRequirement(role = ClothingCategory.BOTTOMS.name, minCount = 1, maxCount = 1))
        }

        // 3. Footwear / Shoes
        val hasShoes = presentCategories.contains(ClothingCategory.SHOES)
        if (!hasShoes) {
            requirements.add(RoleRequirement(role = ClothingCategory.SHOES.name, minCount = 1, maxCount = 1))
        }

        // 4. Outerwear (Formal occasion or cold weather)
        val isFormal = occasion.contains("Formal", ignoreCase = true) || occasion.contains("Business", ignoreCase = true)
        val isCold = weatherTempC < 15f
        val hasOuterwear = presentCategories.contains(ClothingCategory.OUTERWEAR)
        if ((isFormal || isCold) && !hasOuterwear) {
            requirements.add(RoleRequirement(role = ClothingCategory.OUTERWEAR.name, minCount = 1, maxCount = 1))
        }

        // 5. Accessories
        val hasAccessories = presentCategories.contains(ClothingCategory.ACCESSORIES)
        if (!hasAccessories) {
            requirements.add(RoleRequirement(role = ClothingCategory.ACCESSORIES.name, minCount = 0, maxCount = 2))
        }

        return requirements
    }
}
