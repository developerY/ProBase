package com.zoewave.probase.core.util

import com.zoewave.probase.core.model.ritual.ChemistryPhase

/**
 * The Interfacial Chemistry Engine: Evaluates the physical compatibility of layering products.
 * Uses pre-calculated thermodynamic phases to prevent cosmetic pilling.
 */
sealed class CompatibilityResult {
    object Optimal : CompatibilityResult()
    data class PillingWarning(val reason: String, val resolution: String) : CompatibilityResult()
}

object ChemistryCompatibilityEngine {

    /**
     * Evaluates the physical compatibility of layering a new product over an existing base.
     * @param basePhase The phase of the product already on the skin (e.g., a primer).
     * @param layerPhase The phase of the product being applied on top (e.g., a foundation).
     */
    fun evaluateLayering(
        basePhase: ChemistryPhase, 
        layerPhase: ChemistryPhase
    ): CompatibilityResult {
        
        return when {
            // Optimal: Like over Like
            basePhase == layerPhase -> 
                CompatibilityResult.Optimal

            // Warning: Water over Silicone (The classic pilling disaster)
            basePhase == ChemistryPhase.HYDROPHOBIC_SILOXANE && layerPhase == ChemistryPhase.HYDROPHILIC_AQUEOUS -> 
                CompatibilityResult.PillingWarning(
                    reason = "Water-based formulas cannot penetrate a silicone barrier.",
                    resolution = "High Pilling Risk: Apply water-based products first, allow to dry, then seal with silicone."
                )

            // Warning: Water over Oil
            basePhase == ChemistryPhase.LIPOPHILIC_LIPID && layerPhase == ChemistryPhase.HYDROPHILIC_AQUEOUS -> 
                CompatibilityResult.PillingWarning(
                    reason = "Oil repels water.",
                    resolution = "Layering Risk: Apply water-based hydration before lipid-based oils."
                )

            // Powders generally sit fine on dried bases, but applying liquids over powders creates mud
            basePhase == ChemistryPhase.ANHYDROUS_POWDER && layerPhase != ChemistryPhase.ANHYDROUS_POWDER && layerPhase != ChemistryPhase.UNKNOWN -> 
                CompatibilityResult.PillingWarning(
                    reason = "Applying liquids over a set powder creates a muddy texture.",
                    resolution = "Texture Risk: Apply all liquid/cream phases before setting with powder."
                )

            else -> CompatibilityResult.Optimal
        }
    }
}
