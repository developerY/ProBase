package com.zoewave.probase.photodo.mobile.features.settings.ui

sealed interface SettingsEvent {
    data class OnThemeSelected(val themeIdentifier: String) : SettingsEvent
    data class OnPaletteSelected(val paletteIdentifier: String) : SettingsEvent // NEW
    data class OnPaneContrastSelected(val paneContrastIdentifier: String) : SettingsEvent
}
