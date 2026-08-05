package com.zoewave.probase.kocolor.features.starterpack.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackItem(
    val id: String,
    val name: String,
    val shade: String,
    val brand: String,
    @SerialName("hex_color") val hexColor: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    @SerialName("image_url") val imageUrl: String,
    
    // Canonical KoColor Schema extensions
    val formulation: String? = null,
    @SerialName("chemistry_base") val chemistryBase: String? = null,
    val finish: String? = null,
    val coverage: String? = null,
    val temperature: String? = null,
    @SerialName("macro_category") val macroCategory: String? = null,
    @SerialName("micro_category") val microCategory: String? = null
)
