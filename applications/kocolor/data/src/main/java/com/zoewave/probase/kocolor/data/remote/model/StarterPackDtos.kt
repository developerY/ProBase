package com.zoewave.probase.kocolor.data.remote.model

import com.zoewave.probase.core.model.ritual.*
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
    @SerialName("macro_category") val macroCategory: String,
    @SerialName("micro_category") val microCategory: String,
    val formulation: String,
    val chemistry: String,
    val finish: String,
    val coverage: String,
    val temperature: String,
    @SerialName("color_hex") val colorHex: String,
    @SerialName("image_url") val imageUrl: String
)

@Serializable
data class ClothingItemDto(
    val id: String,
    val name: String,
    @SerialName("macro_category") val macroCategory: String,
    @SerialName("micro_category") val microCategory: String,
    @SerialName("color_hex") val colorHex: String,
    @SerialName("image_url") val imageUrl: String
)
