package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
enum class SeaweedThemeConfig {
    DEFAULT,
    CORAL
}

@Serializable
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Serializable
data class UserSettings(
    val monthlyIncome: Double = 5000.0,
    val currency: String = "USD",
    val themeConfig: SeaweedThemeConfig = SeaweedThemeConfig.DEFAULT,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)
