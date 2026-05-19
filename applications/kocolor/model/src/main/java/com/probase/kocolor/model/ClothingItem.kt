package com.probase.kocolor.model

data class ClothingItem(
    val id: String,
    val imageUri: String,
    val dominantHex: String?,
    val vibrantHex: String?,
    val mutedHex: String?,
    val paletteHexes: List<String>,
    val koColorGroup: String?,
    val contrastLevel: String?,
    val colorTemperature: String?,
    val seasonalPalette: String?
)
