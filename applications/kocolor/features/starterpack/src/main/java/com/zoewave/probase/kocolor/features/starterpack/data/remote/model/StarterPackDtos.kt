package com.zoewave.probase.kocolor.features.starterpack.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StarterPackResponse(
    val version: Int,
    val cosmetics: List<CosmeticItemDto>,
    val clothing: List<ClothingItemDto>
)

@Serializable
data class CosmeticItemDto(
    val id: String,
    val name: String,
    val brand: String,
    @SerialName("macro_category") val macroCategory: String,
    @SerialName("micro_category") val microCategory: String,
    
    // Level 3 Professional Facets
    val formulation: String,
    @SerialName("chemistry_base") val chemistryBase: String,
    val finish: String,
    val coverage: String,
    val temperature: String,
    
    @SerialName("color_hex") val colorHex: String,
    @SerialName("shade_name") val shadeName: String? = null,
    @SerialName("image_url") val imageUrl: String,
    val notes: String? = null,
    val instructions: String? = null,

    // Professional Inventory & Logistics
    @SerialName("batch_code") val batchCode: String? = null,
    @SerialName("pao_months") val paoMonths: Int? = null,
    val price: Double? = null,
    val volume: String? = null,

    // Algorithmic & AI Insights
    @SerialName("hero_ingredient") val heroIngredient: String? = null,
    @SerialName("skin_compatibility") val skinCompatibility: String? = null,
    @SerialName("contains_fragrance") val containsFragrance: Boolean? = null,
    val ingredients: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    
    // Sustainability & Eco-Impact
    @SerialName("eco_score") val ecoScore: String? = null,
    @SerialName("is_vegan") val isVegan: Boolean? = null,
    @SerialName("is_cruelty_free") val isCrueltyFree: Boolean? = null,
    @SerialName("recycling_instructions") val recyclingInstructions: String? = null,
    
    // Ritual Context
    @SerialName("ritual_placement") val ritualPlacement: String? = null,

    // Professional Inventory & Logistics Extra
    @SerialName("expiry_date") val expiryDate: Long? = null,

    // FDA & Clinical Safety
    @SerialName("fda_recall_status") val fdaRecallStatus: String? = null,
    @SerialName("fda_adverse_event_count") val fdaAdverseEventCount: Int = 0,
    @SerialName("fda_clinical_warnings") val fdaClinicalWarnings: List<String> = emptyList(),
    @SerialName("fda_top_reactions") val fdaTopReactions: List<String> = emptyList(),
    @SerialName("fda_active_ingredients") val fdaActiveIngredients: List<String> = emptyList(),
    @SerialName("fda_data_verified") val fdaDataVerified: Boolean = false
)

@Serializable
data class ClothingItemDto(
    val id: String,
    val name: String,
    val brand: String? = null,
    @SerialName("macro_category") val macroCategory: String,
    @SerialName("micro_category") val microCategory: String,
    val formality: String = "CASUAL",
    @SerialName("color_hex") val colorHex: String,
    val size: String? = null,
    val material: String? = null,
    val price: Double? = null,
    @SerialName("image_url") val imageUrl: String,
    val notes: String? = null,
    
    // Wardrobe Color Engine Metadata
    @SerialName("dominant_hex") val dominantHex: String? = null,
    @SerialName("vibrant_hex") val vibrantHex: String? = null,
    @SerialName("muted_hex") val mutedHex: String? = null,
    @SerialName("palette_hexes") val paletteHexes: List<String> = emptyList(),
    @SerialName("color_temperature") val colorTemperature: String? = null,
    @SerialName("seasonal_palette") val seasonalPalette: String? = null,
    @SerialName("contrast_level") val contrastLevel: String? = null,
    @SerialName("ko_color_group") val koColorGroup: String? = null
)
