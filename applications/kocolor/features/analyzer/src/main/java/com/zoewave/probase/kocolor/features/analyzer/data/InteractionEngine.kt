package com.zoewave.probase.kocolor.features.analyzer.data

import com.zoewave.probase.kocolor.model.*
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
 * Focuses on professional chemistry-based compatibility and layering.
 */
@Singleton
class InteractionEngine @Inject constructor() {

    /**
     * Checks if two products are compatible to be layered in a single routine.
     * Professionals look for "Chemical Conflict" (e.g. Water over Silicone).
     */
    fun checkCompatibility(item1: CosmeticItem, item2: CosmeticItem): InteractionResult {
        // 1. Chemistry Compatibility Pass
        if (item1.chemistryBase == ChemistryBase.SILICONE && item2.chemistryBase == ChemistryBase.WATER) {
            return InteractionResult(
                isCompatible = false,
                warning = "Potential Pilling: Water-based ${item2.name} over Silicone-based ${item1.name} may separate.",
                suggestion = "Try a Silicone-based foundation or a Water-based primer instead."
            )
        }

        // 2. High-level categorical interaction rules
        val cat1 = item1.microCategory
        val cat2 = item2.microCategory
        
        if ((cat1 == MicroCategory.FOUNDATION && cat2 == MicroCategory.PRIMER) ||
            (cat1 == MicroCategory.PRIMER && cat2 == MicroCategory.FOUNDATION)) {
            return InteractionResult(
                isCompatible = true,
                suggestion = "Professional pairing confirmed."
            )
        }
        
        return InteractionResult(isCompatible = true)
    }

    /**
     * Recommends the optimal layering order for a list of products.
     */
    fun determineLayeringOrder(items: List<CosmeticItem>): List<CosmeticItem> {
        return items.sortedBy { getLayeringWeight(it.microCategory) }
    }

    private fun getLayeringWeight(category: MicroCategory): Int {
        return when (category) {
            MicroCategory.CLEANSER -> 0
            MicroCategory.TONER -> 5
            MicroCategory.SERUM -> 10
            MicroCategory.MOISTURIZER -> 15
            MicroCategory.SPF -> 20
            MicroCategory.PRIMER -> 25
            MicroCategory.FOUNDATION -> 30
            MicroCategory.BB_CC_CREAM -> 30
            MicroCategory.CONCEALER -> 35
            MicroCategory.BLUSH -> 40
            MicroCategory.BRONZER -> 45
            MicroCategory.CONTOUR -> 50
            MicroCategory.HIGHLIGHTER -> 60
            MicroCategory.EYESHADOW -> 70
            MicroCategory.EYELINER -> 80
            MicroCategory.MASCARA -> 90
            MicroCategory.BROW_PENCIL -> 75
            MicroCategory.BROW_GEL -> 76
            MicroCategory.LIPSTICK -> 110
            MicroCategory.LIP_GLOSS -> 120
            MicroCategory.LIP_LINER -> 105
            MicroCategory.SETTING_POWDER -> 150
            MicroCategory.SETTING_SPRAY -> 160
            else -> 1000
        }
    }
}
