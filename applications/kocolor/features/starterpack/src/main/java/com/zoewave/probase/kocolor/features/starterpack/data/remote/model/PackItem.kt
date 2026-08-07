package com.zoewave.probase.kocolor.features.starterpack.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackItem(
    val id: String,
    val name: String,
    @SerialName("shade_name") val shade: String? = null,
    val brand: String,
    @SerialName("color_hex") val hexColor: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    @SerialName("image_url") val imageUrl: String,
    
    // Professional Facets (Cosmetic)
    val formulation: String? = null,
    @SerialName("chemistry_base") val chemistryBase: String? = null,
    val finish: String? = null,
    val coverage: String? = null,
    val temperature: String? = null,
    
    // Wardrobe Metadata (Clothing)
    val formality: String? = null,
    val material: String? = null,

    // Classification
    @SerialName("macro_category") val macroCategory: String? = null,
    @SerialName("micro_category") val microCategory: String? = null,
    
    // Additional Metadata
    val notes: String? = null,
    val instructions: String? = null,
    @SerialName("pao_months") val paoMonths: Int? = null,
    @SerialName("expiry_date") val expiryDate: Long? = null,
    val price: Double? = null,
    val volume: String? = null,
    val ingredients: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    @SerialName("fda_data_verified") val fdaDataVerified: Boolean = false,
    @SerialName("is_vegan") val isVegan: Boolean? = null,
    @SerialName("is_cruelty_free") val isCrueltyFree: Boolean? = null
)

@Serializable
data class RemoteStarterPackResponse(
    @SerialName("schema_version") val schemaVersion: Int,
    val cosmetics: List<PackItem>,
    val clothing: List<PackItem> = emptyList()
)
