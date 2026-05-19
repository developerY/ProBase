package com.probase.kocolor.model

enum class CosmeticCategory {
    LIP, EYE, CHEEK, BASE
}

data class CosmeticItem(
    val id: String,
    val name: String,
    val brand: String,
    val category: CosmeticCategory,
    val colorTemperature: String,
    val seasonalPalette: String
)
