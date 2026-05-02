package com.zoewave.probase.gotmind.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

@Serializable
enum class ColorPalette {
    DEFAULT, CORAL, FOREST, OCEAN, MATERIAL_EXPRESSIVE
}

data class ThemeSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val palette: ColorPalette = ColorPalette.DEFAULT
)
