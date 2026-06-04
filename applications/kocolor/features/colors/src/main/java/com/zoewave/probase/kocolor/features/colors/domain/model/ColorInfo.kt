package com.zoewave.probase.kocolor.features.colors.domain.model

data class ColorInfo(
    val hex: String,
    val name: String,
    val pantoneMatch: String,
    val complementaryPalette: List<String>
)
