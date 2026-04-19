package com.zoewave.probase.photodo.mobile.features.settings.ui

sealed interface SettingsEvent {
    data class OnThemeSelected(val themeIdentifier: String) : SettingsEvent
    data class OnPaletteSelected(val paletteIdentifier: String) : SettingsEvent // NEW
    data class OnPaneContrastSelected(val paneContrastIdentifier: String) : SettingsEvent
    data class OnGeminiApiKeyChanged(val apiKey: String?) : SettingsEvent
    data class OnAiEnabledToggled(val enabled: Boolean) : SettingsEvent
    data class OnAnimationsEnabledToggled(val enabled: Boolean) : SettingsEvent

    // UI State Toggles
    data class OnThemeExpandedToggled(val expanded: Boolean) : SettingsEvent
    data class OnAiExpandedToggled(val expanded: Boolean) : SettingsEvent
    data class OnAboutExpandedToggled(val expanded: Boolean) : SettingsEvent
}
