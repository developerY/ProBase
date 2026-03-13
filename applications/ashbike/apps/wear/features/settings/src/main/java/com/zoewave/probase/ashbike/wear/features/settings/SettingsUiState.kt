package com.zoewave.probase.ashbike.wear.features.settings

// The single source of truth for the UI
data class SettingsUiState(
    val isAutoPauseEnabled: Boolean = false,
    val isHealthConnectEnabled: Boolean = false,
    val isMetricUnits: Boolean = true
)