package com.zoewave.probase.photodo.mobile.features.settings.ui

data class SettingsUiState(
    val currentTheme: String = "SYSTEM", // Light/Dark
    val currentPalette: String = "DEFAULT", // Default/Coral
    val initialCardKeyToExpand: String? = null,
)