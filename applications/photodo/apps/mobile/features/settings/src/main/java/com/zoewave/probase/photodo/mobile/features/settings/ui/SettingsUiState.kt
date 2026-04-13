package com.zoewave.probase.photodo.mobile.features.settings.ui

data class SettingsUiState(
    val currentTheme: String = "SYSTEM", // Light/Dark
    val currentPalette: String = "DEFAULT", // Default/Coral
    val currentPaneContrast: String = "TINTED", // FLAT/TINTED
    val isApiKeySet: Boolean = false,
    val isAiEnabled: Boolean = false,
    val currentAiModel: String = "gemini-1.5-flash",
    val isTestingKey: Boolean = false,
    val keyTestResult: String? = null,
    val initialCardKeyToExpand: String? = null,
    val appVersion: String = "",
    val firebaseDeviceId: String = ""
)
