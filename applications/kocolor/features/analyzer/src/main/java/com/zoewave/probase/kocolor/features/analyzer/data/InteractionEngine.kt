package com.zoewave.probase.kocolor.features.analyzer.data

import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.CosmeticCategory
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class InteractionResult(
    val isCompatible: Boolean,
    val warning: String? = null,
    val suggestion: String? = null
)

/**
 * Engine for analyzing how different cosmetic/skincare products interact when used together.
 */
@Singleton
class InteractionEngine @Inject constructor() {

    /**
     * Checks if two products are compatible to be layered in a single routine.
     */
    fun checkCompatibility(item1: CosmeticItem, item2: CosmeticItem): InteractionResult {
        // High-level categorical interaction rules
        val cat1 = item1.category
        val cat2 = item2.category
        
        // Example: Vitamin C + Retinol (Simulated via categories for now)
        if ((cat1 == CosmeticCategory.FOUNDATION && cat2 == CosmeticCategory.PRIMER) ||
            (cat1 == CosmeticCategory.PRIMER && cat2 == CosmeticCategory.FOUNDATION)) {
            return InteractionResult(
                isCompatible = true,
                suggestion = "Perfect pair: Primer ensures smooth foundation application."
            )
        }
        
        // Example: Chemical Exfoliants + Retinoids
        // In a real implementation, we would check ingredient analysis tags.
        
        return InteractionResult(isCompatible = true)
    }

    /**
     * Recommends the optimal layering order for a list of products.
     */
    fun determineLayeringOrder(items: List<CosmeticItem>): List<CosmeticItem> {
        return items.sortedBy { getLayeringWeight(it.category) }
    }

    private fun getLayeringWeight(category: CosmeticCategory): Int {
        return when (category) {
            CosmeticCategory.PRIMER -> 10
            CosmeticCategory.FOUNDATION -> 20
            CosmeticCategory.CONCEALER -> 30
            CosmeticCategory.BB_CC_CREAM -> 20
            CosmeticCategory.SETTING_PRODUCT -> 100
            CosmeticCategory.BLUSH -> 40
            CosmeticCategory.BRONZER -> 45
            CosmeticCategory.CONTOUR -> 50
            CosmeticCategory.HIGHLIGHTER -> 60
            CosmeticCategory.EYESHADOW -> 70
            CosmeticCategory.EYELINER -> 80
            CosmeticCategory.MASCARA -> 90
            CosmeticCategory.EYEBROW_PRODUCT -> 75
            CosmeticCategory.LIPSTICK -> 110
            CosmeticCategory.LIP_GLOSS -> 120
            CosmeticCategory.LIP_LINER -> 105
            else -> 1000
        }
    }
}
