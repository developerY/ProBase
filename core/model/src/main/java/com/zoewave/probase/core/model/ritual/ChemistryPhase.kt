package com.zoewave.probase.core.model.ritual

import kotlinx.serialization.Serializable

/**
 * Represents the thermodynamic phase of a cosmetic product.
 * Pre-calculated by the Rust compiler during the build phase.
 */
@Serializable
enum class ChemistryPhase {
    HYDROPHILIC_AQUEOUS,   // Water-based
    HYDROPHOBIC_SILOXANE,  // Silicone-based
    LIPOPHILIC_LIPID,      // Oil/Wax-based
    ANHYDROUS_POWDER,      // Dry powders
    UNKNOWN;

    companion object {
        fun fromString(phase: String?): ChemistryPhase {
            if (phase == null) return UNKNOWN
            return entries.find { it.name.equals(phase, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
