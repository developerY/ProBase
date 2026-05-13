package com.zoewave.probase.rxlogic.features.settings

data class SettingsUiState(
    val description: String = "Application settings and profile management."
)

sealed interface SettingsEvent {
    data object OnResetData : SettingsEvent
    data object OnGenerateSampleData : SettingsEvent
}
