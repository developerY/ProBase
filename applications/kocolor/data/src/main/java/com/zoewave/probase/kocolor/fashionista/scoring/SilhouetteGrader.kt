package com.zoewave.probase.kocolor.fashionista.scoring

import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SilhouetteGrader @Inject constructor() {

    fun grade(blueprint: StyleBlueprint): Float {
        val clothingIds = blueprint.selectedClothingIds
        if (clothingIds.isEmpty()) return 75.0f

        // Evaluates structural volume balance & proportion alignment
        val hasTopAndBottom = clothingIds.size >= 2
        val hasThreePiece = clothingIds.size >= 3

        return when {
            hasThreePiece -> 90.0f // Complete 3-piece silhouette (Top + Bottom + Shoes)
            hasTopAndBottom -> 82.0f // Standard 2-piece silhouette
            else -> 70.0f
        }
    }
}
