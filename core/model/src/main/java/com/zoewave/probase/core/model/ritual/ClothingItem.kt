package com.zoewave.probase.core.model.ritual

import kotlinx.serialization.Serializable

@Serializable
enum class ClothingCategory {
    TOPS, BOTTOMS, SHOES, ACCESSORIES, OTHER;

    val displayName: String
        get() = when (this) {
            TOPS -> "Tops"
            BOTTOMS -> "Bottoms"
            SHOES -> "Shoes"
            ACCESSORIES -> "Accessories"
            OTHER -> "Other"
        }

    val description: String
        get() = when (this) {
            TOPS -> "Strategic foundational pieces for your upper silhouette."
            BOTTOMS -> "Structural elements that define your architectural base."
            SHOES -> "Performance-driven anchors for your daily ensemble."
            ACCESSORIES -> "Curated enhancements to amplify your style DNA."
            OTHER -> "Miscellaneous archive entries."
        }
}

@Serializable
enum class Formality(val rank: Int) {
    LOUNGE(1), 
    CASUAL(2), 
    SMART_CASUAL(3), 
    PROFESSIONAL(4), 
    FORMAL(5), 
    GALA(6)
}

@Serializable
data class ClothingItem(
    val id: Long = 0,
    val name: String,
    val brand: String? = null,
    val category: ClothingCategory,
    val formality: Formality = Formality.CASUAL,
    val colorHex: String,
    val colorFamily: ColorFamily = ColorFamily.UNKNOWN,
    val size: String? = null,
    val material: String? = null,
    val price: Double? = null,
    val imageUrl: String? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    
    // --- Wardrobe Color Engine Metadata ---
    val dominantHex: String? = null,
    val vibrantHex: String? = null,
    val mutedHex: String? = null,
    val paletteHexes: List<String> = emptyList(),
    val colorTemperature: String? = null, // WARM, COOL, NEUTRAL
    val seasonalPalette: String? = null,  // SPRING, SUMMER, AUTUMN, WINTER
    val contrastLevel: String? = null,    // LOW, MEDIUM, HIGH
    val koColorGroup: String? = null,     // Semantic group
    val sourcePackId: String? = null,

    // --- Usage & Performance ---
    val usageCount: Int = 0
) {
    /** Cost per single wear. */
    val costPerUse: Double?
        get() = if (price != null && usageCount > 0) price / usageCount else null
}
