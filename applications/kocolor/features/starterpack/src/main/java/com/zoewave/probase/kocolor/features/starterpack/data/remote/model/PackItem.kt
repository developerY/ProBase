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
    @SerialName("image_url") val imageUrl: String
)
