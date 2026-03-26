package com.zoewave.probase.mobile.features.settings

data class SettingsUiState(
    val currentTheme: String = "SYSTEM",
    val initialCardKeyToExpand: String? = null
)