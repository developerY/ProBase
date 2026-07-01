package com.zoewave.probase.kocolor.features.colors.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ColorIdResponse(
    val name: ColorName
)

@Serializable
data class ColorName(
    val value: String
)

@Serializable
data class ColorSchemeResponse(
    val colors: List<ColorItem>
)

@Serializable
data class ColorItem(
    val hex: ColorHex
)

@Serializable
data class ColorHex(
    val value: String,
    val clean: String
)
