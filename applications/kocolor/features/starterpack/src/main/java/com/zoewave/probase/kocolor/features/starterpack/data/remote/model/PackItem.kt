package com.zoewave.probase.kocolor.features.starterpack.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KcpsPayload(
    @SerialName("schema_version") val schemaVersion: Int, // MUST equal 1
    val cosmetics: List<CosmeticItemDto>,
    val clothing: List<ClothingItemDto>
)

@Serializable
sealed interface PackItemDto {
    val id: String
    val name: String
    val brand: String?
    val macroCategory: String
    val microCategory: String
    val colorHex: String
    val shadeName: String?
    val imageUrl: String
    val thumbnailUrl: String
    val price: Double?
    val notes: String?
}

@Serializable
data class CosmeticItemDto(
    override val id: String,
    override val name: String,
    override val brand: String,
    @SerialName("macro_category") override val macroCategory: String,
    @SerialName("micro_category") override val microCategory: String,
    @SerialName("color_hex") override val colorHex: String,
    @SerialName("shade_name") override val shadeName: String?, 
    @SerialName("image_url") override val imageUrl: String,
    @SerialName("thumbnail_url") override val thumbnailUrl: String,
    override val price: Double?,
    override val notes: String?,
    val formulation: String?,
    @SerialName("chemistry_base") val chemistryBase: String?,
    val finish: String?,
    val coverage: String?,
    val temperature: String?,
    val volume: String?,
    @SerialName("pao_months") val paoMonths: Int?,
    @SerialName("expiry_date") val expiryDate: Long?,
    val instructions: String?,
    val ingredients: List<String>,
    val allergens: List<String>,
    @SerialName("is_vegan") val isVegan: Boolean?,
    @SerialName("is_cruelty_free") val isCrueltyFree: Boolean?,
    @SerialName("fda_data_verified") val fdaDataVerified: Boolean
) : PackItemDto

@Serializable
data class ClothingItemDto(
    override val id: String,
    override val name: String,
    override val brand: String,
    @SerialName("macro_category") override val macroCategory: String,
    @SerialName("micro_category") override val microCategory: String,
    @SerialName("color_hex") override val colorHex: String,
    @SerialName("shade_name") override val shadeName: String?,
    @SerialName("image_url") override val imageUrl: String,
    @SerialName("thumbnail_url") override val thumbnailUrl: String,
    override val price: Double?,
    override val notes: String?,
    val formality: String?,
    val material: String?,
    @SerialName("dominant_hex") val dominantHex: String? = null,
    @SerialName("vibrant_hex") val vibrantHex: String? = null,
    @SerialName("muted_hex") val mutedHex: String? = null,
    @SerialName("palette_hexes") val paletteHexes: List<String> = emptyList(),
    @SerialName("color_temperature") val colorTemperature: String? = null,
    @SerialName("seasonal_palette") val seasonalPalette: String? = null,
    @SerialName("contrast_level") val contrastLevel: String? = null,
    @SerialName("ko_color_group") val koColorGroup: String? = null
) : PackItemDto
