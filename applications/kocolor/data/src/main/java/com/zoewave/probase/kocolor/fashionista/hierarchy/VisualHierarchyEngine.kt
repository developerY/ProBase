package com.zoewave.probase.kocolor.fashionista.hierarchy

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisualHierarchyEngine @Inject constructor() {

    fun evaluate(clothingItems: List<ClothingItem>): FeatureValue {
        if (clothingItems.isEmpty()) {
            return FeatureValue(value = 0.0, availability = 0.0)
        }

        // Focal point isolation check (Primary statement piece vs secondary anchors)
        val statementPieces = clothingItems.count { it.colorTemperature != null || it.brand != null }
        val score = when {
            statementPieces == 1 -> 0.95 // Clear primary focal point
            statementPieces in 2..3 -> 0.80 // Structured secondary balance
            else -> 0.65 // Potential focal conflict
        }

        val availability = if (clothingItems.size >= 2) 1.0 else 0.70
        return FeatureValue(value = score, availability = availability)
    }
}
