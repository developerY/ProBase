package com.zoewave.ashbike.mobile.settings.ui

import com.zoewave.ashbike.model.bike.LocationEnergyLevel
import com.zoewave.probase.ashbike.database.ProfileData


enum class ProfileField { NAME, HEIGHT, WEIGHT }


sealed class SettingsEvent {
    object LoadSettings : SettingsEvent()
    data class UpdateSetting(val key: String, val value: String) : SettingsEvent()
    data class SaveProfile(val profile: ProfileData) : SettingsEvent()
    data class UpdateEnergyLevel(val level: LocationEnergyLevel) : SettingsEvent()
    data class UpdateLongRideEnabled(val enabled: Boolean) : SettingsEvent() // New event for short ride
    // data class OnShowGpsCountdownChanged(val show: Boolean) : SettingsEvent() // New event
}
