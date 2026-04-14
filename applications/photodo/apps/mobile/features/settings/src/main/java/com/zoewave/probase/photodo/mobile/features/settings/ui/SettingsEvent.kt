package com.zoewave.probase.photodo.mobile.features.settings.ui

sealed interface SettingsEvent {
    data class OnThemeSelected(val themeIdentifier: String) : SettingsEvent
    data class OnPaletteSelected(val paletteIdentifier: String) : SettingsEvent // NEW
    data class OnPaneContrastSelected(val paneContrastIdentifier: String) : SettingsEvent
    data class OnGeminiApiKeyChanged(val apiKey: String?) : SettingsEvent
    data class OnAiEnabledToggled(val enabled: Boolean) : SettingsEvent
    data class OnAiModelSelected(val model: String) : SettingsEvent
    data object OnTestApiKeyClicked : SettingsEvent
    data object OnTestModelClicked : SettingsEvent
}
