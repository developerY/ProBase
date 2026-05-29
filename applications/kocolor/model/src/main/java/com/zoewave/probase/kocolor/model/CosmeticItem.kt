package com.zoewave.probase.kocolor.model

import kotlinx.serialization.Serializable

@Serializable
enum class MacroCategory(val displayName: String, val description: String) {
    PREP("Skincare & Prep", "Everything applied before pigment."),
    COMPLEXION("Complexion", "Products that unify the skin tone."),
    DIMENSION("Color & Dimension", "Products that bring life, shadow, and light."),
    EYES("Eyes & Brows", "All definition for the upper face."),
    LIPS("Lips", "All color and care for the mouth."),
    TOOLS("Tools & Hygiene", "Brushes, sponges, and maintenance.")
}

@Serializable
enum class MicroCategory(val macro: MacroCategory) {
    // Prep
    CLEANSER(MacroCategory.PREP),
    TONER(MacroCategory.PREP),
    SERUM(MacroCategory.PREP),
    MOISTURIZER(MacroCategory.PREP),
    SPF(MacroCategory.PREP),
    PRIMER(MacroCategory.PREP),
    
    // Complexion
    FOUNDATION(MacroCategory.COMPLEXION),
    BB_CC_CREAM(MacroCategory.COMPLEXION),
    CONCEALER(MacroCategory.COMPLEXION),
    COLOR_CORRECTOR(MacroCategory.COMPLEXION),
    SETTING_POWDER(MacroCategory.COMPLEXION),
    SETTING_SPRAY(MacroCategory.COMPLEXION),
    
    // Dimension
    BLUSH(MacroCategory.DIMENSION),
    BRONZER(MacroCategory.DIMENSION),
    CONTOUR(MacroCategory.DIMENSION),
    HIGHLIGHTER(MacroCategory.DIMENSION),
    FRECKLE_TINT(MacroCategory.DIMENSION),
    
    // Eyes
    EYESHADOW(MacroCategory.EYES),
    EYELINER(MacroCategory.EYES),
    MASCARA(MacroCategory.EYES),
    LASH_PRIMER(MacroCategory.EYES),
    BROW_PENCIL(MacroCategory.EYES),
    BROW_GEL(MacroCategory.EYES),
    FALSE_LASHES(MacroCategory.EYES),
    
    // Lips
    LIPSTICK(MacroCategory.LIPS),
    LIP_GLOSS(MacroCategory.LIPS),
    LIP_LINER(MacroCategory.LIPS),
    LIP_TINT_STAIN(MacroCategory.LIPS),
    LIP_BALM(MacroCategory.LIPS),
    LIP_PLUMPER(MacroCategory.LIPS),
    
    // Tools
    BRUSHES(MacroCategory.TOOLS),
    SPONGES(MacroCategory.TOOLS),
    EYELASH_CURLER(MacroCategory.TOOLS),
    ORGANIZERS(MacroCategory.TOOLS),
    OTHER(MacroCategory.TOOLS),
    
    // AI Pending
    AI_PENDING(MacroCategory.TOOLS);

    val displayName: String
        get() = when (this) {
            BB_CC_CREAM -> "BB/CC Cream"
            COLOR_CORRECTOR -> "Color Corrector"
            SETTING_POWDER -> "Setting Powder"
            SETTING_SPRAY -> "Setting Spray"
            FRECKLE_TINT -> "Freckle Tint"
            LASH_PRIMER -> "Lash Primer"
            BROW_PENCIL -> "Brow Pencil"
            BROW_GEL -> "Brow Gel"
            FALSE_LASHES -> "False Lashes"
            LIP_TINT_STAIN -> "Lip Tint/Stain"
            LIP_BALM -> "Lip Balm"
            LIP_PLUMPER -> "Lip Plumper"
            AI_PENDING -> "New Capture"
            else -> name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
        }

    /** Typical amount used per application (in ml or g). */
    val typicalAmountPerUse: Double
        get() = when (this) {
            CLEANSER -> 1.0
            TONER -> 0.5
            SERUM -> 0.2
            MOISTURIZER -> 0.5
            SPF -> 1.2
            PRIMER -> 0.3
            FOUNDATION -> 0.35
            BB_CC_CREAM -> 0.4
            CONCEALER -> 0.1
            COLOR_CORRECTOR -> 0.05
            SETTING_POWDER -> 0.1
            SETTING_SPRAY -> 0.2
            BLUSH -> 0.1
            BRONZER -> 0.1
            CONTOUR -> 0.1
            HIGHLIGHTER -> 0.05
            FRECKLE_TINT -> 0.02
            EYESHADOW -> 0.05
            EYELINER -> 0.02
            MASCARA -> 0.1
            LASH_PRIMER -> 0.05
            BROW_PENCIL -> 0.02
            BROW_GEL -> 0.03
            FALSE_LASHES -> 1.0
            LIPSTICK -> 0.05
            LIP_GLOSS -> 0.1
            LIP_LINER -> 0.02
            LIP_TINT_STAIN -> 0.05
            LIP_BALM -> 0.1
            LIP_PLUMPER -> 0.1
            BRUSHES, SPONGES, EYELASH_CURLER, ORGANIZERS, AI_PENDING -> 0.0
            OTHER -> 0.1
        }
}

@Serializable
enum class Formulation { 
    LIQUID, CREAM, POWDER, GEL, BALM, PENCIL, SPRAY, STICK, OTHER, UNKNOWN 
}

@Serializable
enum class ChemistryBase { 
    WATER, SILICONE, OIL, ALCOHOL, WAX, UNKNOWN 
}

@Serializable
enum class Finish { 
    MATTE, SATIN, NATURAL, DEWY, RADIANT, METALLIC, GLITTER, SHEEN, GLOSSY, UNKNOWN 
}

@Serializable
enum class Coverage { 
    SHEER, LIGHT, MEDIUM, FULL, BUILDABLE, NOT_APPLICABLE 
}

@Serializable
data class CosmeticItem(
    val id: Long = 0,
    val name: String,
    val brand: String,
    val macroCategory: MacroCategory,
    val microCategory: MicroCategory,
    
    // Professional Metadata
    val formulation: Formulation = Formulation.UNKNOWN,
    val chemistryBase: ChemistryBase = ChemistryBase.UNKNOWN,
    val finish: Finish = Finish.UNKNOWN,
    val coverage: Coverage = Coverage.NOT_APPLICABLE,
    
    val colorHex: String? = null,
    val shadeName: String? = null,
    val imageUrl: String? = null,
    val notes: String? = null,
    /** Official manufacturer instructions for use. */
    val instructions: String? = null,
    val timestamp: Long = System.currentTimeMillis(),

    // --- Professional Inventory & Logistics ---
    /** Batch or lot code for recall readiness and production tracking. */
    val batchCode: String? = null,
    /** When the product was first opened. Used for PAO calculation. */
    val openedDate: Long? = null,
    /** Period After Opening (in months) as specified by the manufacturer. */
    val paoMonths: Int? = null,
    /** Hard expiration date from the manufacturer. */
    val expiryDate: Long? = null,
    /** Purchase price for Cost-Per-Use (CPU) calculation. */
    val price: Double? = null,
    /** Product volume/weight (e.g., 30ml, 15g). */
    val volume: String? = null,

    // --- Usage & Consumption Engine ---
    val isOpened: Boolean = false,
    val isFinished: Boolean = false,
    val isArchived: Boolean = false,
    /** Total number of times this product has been used. */
    val usageCount: Int = 0,
    /** Total amount remaining (same unit as volume). */
    val amountRemaining: Double? = null,
    /** Amount consumed per single usage event. */
    val amountPerUse: Double? = null
) {
    /** 
     * Calculated estimated expiration based on PAO and opened date. 
     * Prefers hard expiryDate if PAO is not set or further in future.
     */
    val estimatedExpiry: Long?
        get() {
            val paoExpiry = if (openedDate != null && paoMonths != null) {
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = openedDate
                calendar.add(java.util.Calendar.MONTH, paoMonths)
                calendar.timeInMillis
            } else null

            return when {
                paoExpiry != null && expiryDate != null -> kotlin.math.min(paoExpiry, expiryDate)
                paoExpiry != null -> paoExpiry
                else -> expiryDate
            }
        }

    /** Cost per single usage event. */
    val costPerUse: Double?
        get() = if (price != null && usageCount > 0) price / usageCount else null
    
    /** Percentage of product remaining (0.0 to 1.0). */
    val fillLevel: Double?
        get() {
            val total = volume?.filter { it.isDigit() || it == '.' }?.toDoubleOrNull()
            return if (total != null && amountRemaining != null && total > 0) {
                (amountRemaining / total).coerceIn(0.0, 1.0)
            } else null
        }
}
