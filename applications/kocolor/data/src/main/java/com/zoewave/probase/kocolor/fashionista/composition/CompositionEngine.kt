package com.zoewave.probase.kocolor.fashionista.composition

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompositionEngine @Inject constructor() {

    fun evaluate(clothingItems: List<ClothingItem>): FeatureValue {
        if (clothingItems.isEmpty()) {
            return FeatureValue(value = 0.0, availability = 0.0)
        }

        val categories = clothingItems.map { it.category }.toSet()
        var score = 0.5

        // Category completeness check
        val hasTop = categories.contains(ClothingCategory.TOPS) || categories.contains(ClothingCategory.DRESSES)
        val hasBottom = categories.contains(ClothingCategory.BOTTOMS) || categories.contains(ClothingCategory.DRESSES)
        val hasShoes = categories.contains(ClothingCategory.SHOES)

        if (hasTop && hasBottom) score += 0.25
        if (hasShoes) score += 0.15
        if (categories.contains(ClothingCategory.OUTERWEAR)) score += 0.10

        val availability = (clothingItems.size.toDouble() / 3.0).coerceIn(0.33, 1.0)
        return FeatureValue(value = score.coerceIn(0.0, 1.0), availability = availability)
    }
}
