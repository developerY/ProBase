package com.zoewave.probase.mobile.features.settings.ui

sealed interface SettingsEvent {
    data class OnThemeSelected(val themeIdentifier: String) : SettingsEvent
    // Note: We don't need an OnBackClicked event because we use the navTo() pipe directly!
}