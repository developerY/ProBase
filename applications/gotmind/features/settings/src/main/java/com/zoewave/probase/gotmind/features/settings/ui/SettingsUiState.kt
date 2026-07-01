package com.zoewave.probase.gotmind.features.settings.ui

import com.zoewave.probase.gotmind.features.memblox.MemBloxEvent
import com.zoewave.probase.gotmind.model.GameSettings
import com.zoewave.probase.gotmind.model.ThemeSettings

data class SettingsUiState(
    val gameSettings: GameSettings = GameSettings(),
    val themeSettings: ThemeSettings = ThemeSettings(),
    val firebaseId: String = ""
)

sealed interface SettingsScreenEvent {
    data class Settings(val event: SettingsEvent) : SettingsScreenEvent
    data class MemBlox(val event: MemBloxEvent) : SettingsScreenEvent
}
