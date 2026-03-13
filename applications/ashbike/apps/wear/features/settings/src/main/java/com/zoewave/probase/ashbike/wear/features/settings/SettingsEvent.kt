package com.zoewave.probase.ashbike.wear.features.settings

// The only way the UI can talk to the ViewModel
sealed interface SettingsEvent {
    data class ToggleAutoPause(val isEnabled: Boolean) : SettingsEvent
    data class ToggleHealthConnect(val isEnabled: Boolean) : SettingsEvent
    data class ToggleMetricUnits(val isMetric: Boolean) : SettingsEvent
    object OnAboutClicked : SettingsEvent // In case you need to track this!
}