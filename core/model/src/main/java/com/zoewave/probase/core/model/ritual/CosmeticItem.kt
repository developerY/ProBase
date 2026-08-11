package com.zoewave.probase.core.model.ritual

import kotlinx.serialization.Serializable

@Serializable
enum class MacroCategory(val displayName: String, val description: String) {
    PREP("Skincare & Prep", "Everything applied before pigment."),
    COMPLEXION("Complexion", "Products that unify the skin tone."),
    DIMENSION("Color & Dimension", "Products that bring life, shadow, and light."),
    EYES("Eyes & Brows", "All definition for the upper face."),
    LIPS("Lips", "All color and care for the mouth."),
    NAILS("Nails", "Polish, care, and enhancements."),
    HAIR("Haircare", "Cleanse, treat, and style."),
    HYGIENE("Hygiene & Bath", "Daily body care and sanitization."),
    ORAL("Oral Care", "Smile preservation and hygiene."),
    FRAGRANCE("Fragrances", "Olfactory signatures."),
    GROOMING("Grooming & Shaving", "Precision care and hair removal."),
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
    FACE_MASK(MacroCategory.PREP),
    EXFOLIANT(MacroCategory.PREP),
    EYE_CARE(MacroCategory.PREP),
    LIP_CARE(MacroCategory.PREP),
    
    // Complexion
    FOUNDATION(MacroCategory.COMPLEXION),
    BB_CC_CREAM(MacroCategory.COMPLEXION),
    CONCEALER(MacroCategory.COMPLEXION),
    COLOR_CORRECTOR(MacroCategory.COMPLEXION),
    SETTING_POWDER(MacroCategory.COMPLEXION),
    FACE_POWDER(MacroCategory.COMPLEXION),
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
    
    // Hair
    SHAMPOO(MacroCategory.HAIR),
    CONDITIONER(MacroCategory.HAIR),
    HAIR_MASK(MacroCategory.HAIR),
    HAIR_COLOR(MacroCategory.HAIR),
    HAIR_STYLING(MacroCategory.HAIR),
    HAIR_SPRAY(MacroCategory.HAIR),
    SCALP_TREATMENT(MacroCategory.HAIR),
    
    // Hygiene
    SOAP(MacroCategory.HYGIENE),
    SHOWER_GEL(MacroCategory.HYGIENE),
    BATH_PRODUCT(MacroCategory.HYGIENE),
    DEODORANT(MacroCategory.HYGIENE),
    ANTIPERSPIRANT(MacroCategory.HYGIENE),
    INTIMATE_HYGIENE(MacroCategory.HYGIENE),
    COTTON_PRODUCT(MacroCategory.HYGIENE),
    
    // Oral
    TOOTHPASTE(MacroCategory.ORAL),
    MOUTHWASH(MacroCategory.ORAL),
    TOOTHBRUSH(MacroCategory.ORAL),
    DENTAL_FLOSS(MacroCategory.ORAL),
    
    // Fragrance
    PERFUME(MacroCategory.FRAGRANCE),
    EAU_DE_PARFUM(MacroCategory.FRAGRANCE),
    EAU_DE_TOILETTE(MacroCategory.FRAGRANCE),
    COLOGNE(MacroCategory.FRAGRANCE),
    BODY_MIST(MacroCategory.FRAGRANCE),
    
    // Grooming
    SHAVING_CREAM(MacroCategory.GROOMING),
    AFTERSHAVE(MacroCategory.GROOMING),
    BEARD_CARE(MacroCategory.GROOMING),
    RAZOR(MacroCategory.GROOMING),
    
    // Hands & Nails
    HAND_CREAM(MacroCategory.HYGIENE),
    NAIL_POLISH(MacroCategory.NAILS),
    
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
            BROW_GEL -> "Brow Gel"
            FALSE_LASHES -> "False Lashes"
            LIP_TINT_STAIN -> "Lip Tint/Stain"
            LIP_BALM -> "Lip Balm"
            LIP_PLUMPER -> "Lip Plumper"
            BB_CC_CREAM -> "BB/CC Cream"
            COLOR_CORRECTOR -> "Color Corrector"
            SETTING_POWDER -> "Setting Powder"
            FACE_POWDER -> "Face Powder"
            SETTING_SPRAY -> "Setting Spray"
            FRECKLE_TINT -> "Freckle Tint"
            LASH_PRIMER -> "Lash Primer"
            BROW_PENCIL -> "Brow Pencil"
            FACE_MASK -> "Face Mask"
            EYE_CARE -> "Eye Care"
            LIP_CARE -> "Lip Care"
            HAIR_MASK -> "Hair Mask"
            HAIR_COLOR -> "Hair Coloring"
            HAIR_STYLING -> "Hair Styling"
            HAIR_SPRAY -> "Hair Spray"
            SCALP_TREATMENT -> "Scalp Treatment"
            SHOWER_GEL -> "Shower Gel"
            BATH_PRODUCT -> "Bath Product"
            INTIMATE_HYGIENE -> "Intimate Hygiene"
            COTTON_PRODUCT -> "Cotton Pad/Swab"
            DENTAL_FLOSS -> "Dental Floss"
            EAU_DE_PARFUM -> "Eau de Parfum"
            EAU_DE_TOILETTE -> "Eau de Toilette"
            BODY_MIST -> "Body Mist"
            SHAVING_CREAM -> "Shaving Cream/Foam"
            BEARD_CARE -> "Beard Care"
            HAND_CREAM -> "Hand Cream"
            NAIL_POLISH -> "Nail Polish"
            AI_PENDING -> "New Capture"
            else -> name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
        }

    /** Typical amount used per application (in ml or g). */
    val typicalAmountPerUse: Double
        get() = when (this) {
            CLEANSER, SHAMPOO, CONDITIONER, SHOWER_GEL -> 5.0
            TONER, MOUTHWASH -> 2.0
            SERUM, EYE_CARE, SCALP_TREATMENT -> 0.3
            MOISTURIZER, FACE_MASK, HAIR_MASK, HAIR_COLOR, HAIR_STYLING, HAND_CREAM -> 1.5
            SPF -> 1.2
            PRIMER, FOUNDATION -> 0.4
            BB_CC_CREAM -> 0.5
            CONCEALER, COLOR_CORRECTOR -> 0.1
            SETTING_POWDER, FACE_POWDER -> 0.2
            SETTING_SPRAY, HAIR_SPRAY, BODY_MIST -> 0.5
            BLUSH, BRONZER, CONTOUR -> 0.15
            HIGHLIGHTER, FRECKLE_TINT -> 0.05
            EYESHADOW, EYELINER, BROW_PENCIL, BROW_GEL -> 0.02
            MASCARA, LASH_PRIMER -> 0.05
            LIPSTICK, LIP_LINER, LIP_TINT_STAIN -> 0.05
            LIP_GLOSS, LIP_BALM, LIP_PLUMPER, LIP_CARE -> 0.1
            FALSE_LASHES -> 1.0
            BRUSHES, SPONGES, EYELASH_CURLER, ORGANIZERS, AI_PENDING, TOOTHBRUSH, RAZOR, NAIL_POLISH -> 0.0
            SOAP, BATH_PRODUCT, DEODORANT, ANTIPERSPIRANT, INTIMATE_HYGIENE, COTTON_PRODUCT -> 1.0
            TOOTHPASTE -> 0.5
            DENTAL_FLOSS -> 0.2
            PERFUME, EAU_DE_PARFUM, EAU_DE_TOILETTE, COLOGNE -> 0.1
            SHAVING_CREAM, AFTERSHAVE, BEARD_CARE -> 1.0
            EXFOLIANT -> 2.0
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
enum class Temperature { 
    WARM, COOL, NEUTRAL, OLIVE, UNKNOWN 
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
    val temperature: Temperature = Temperature.UNKNOWN,
    
    val colorHex: String,
    val colorFamily: ColorFamily = ColorFamily.UNKNOWN,
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
    val amountPerUse: Double? = null,
    
    // --- Algorithmic & AI Insights ---
    val heroIngredient: String? = null,
    val skinCompatibility: String? = null,
    val containsFragrance: Boolean? = null,
    val ingredients: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    
    // --- Sustainability & Eco-Impact ---
    val ecoScore: String? = null, // A, B, C, D, E
    val isVegan: Boolean? = null,
    val isCrueltyFree: Boolean? = null,
    val recyclingInstructions: String? = null,
    
    // --- Ritual Context ---
    val ritualPlacement: String? = null, // e.g. "Morning Routine (Step 2)"
    val sourceType: InventorySource = InventorySource.USER_SCAN,
    val sourceName: String? = null,
    val sourcePackId: String? = null,
    val provenance: Provenance? = null,
    val parentItemId: String? = null,
    val isHidden: Boolean = false,

    // Engine Enrichment (Calculated at Compile Time) ---
    val calculatedChemistryPhase: String? = null,
    val chemistryPhase: ChemistryPhase = ChemistryPhase.fromString(calculatedChemistryPhase),
    val calculatedCielabL: Double? = null,
    val calculatedCielabA: Double? = null,
    val calculatedCielabB: Double? = null,
    val calculatedHueAngle: Double? = null,
    val blurhash: String? = null,
    val isSiliconeFree: Boolean? = null,
    val isParabenFree: Boolean? = null,
    val isSulfateFree: Boolean? = null,
    val heroActives: List<String> = emptyList(),
    val calculatedUnitPrice: Double? = null,
    val searchTokens: List<String> = emptyList(),

    // --- FDA & Clinical Safety ---
    val fdaRecallStatus: String? = null,
    val fdaAdverseEventCount: Int = 0,
    val fdaClinicalWarnings: List<String> = emptyList(),
    val fdaTopReactions: List<String> = emptyList(),
    val fdaActiveIngredients: List<String> = emptyList(),
    val fdaDataVerified: Boolean = false
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
