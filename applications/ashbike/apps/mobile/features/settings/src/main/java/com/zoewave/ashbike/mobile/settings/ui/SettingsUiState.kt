package com.zoewave.ashbike.mobile.settings.ui

import com.zoewave.ashbike.model.bike.LocationEnergyLevel
import com.zoewave.probase.ashbike.database.ProfileData

// 3a) Extend your UiState to carry both the *options* and the *current selection*
/**
 * Combine app-wide options with current selections AND rider profile
 */
sealed interface SettingsUiState {
    object Loading : SettingsUiState
    data class Success(
        val options: Map<String, List<String>>,
        val selections: Map<String, String>,
        val profile: ProfileData? = null, // Make nullable and provide default
        val isProfileIncomplete: Boolean = true, // Add this field
        val currentEnergyLevel: LocationEnergyLevel = LocationEnergyLevel.BALANCED, // Added field
        val isLongRideEnabled: Boolean = false, // Added for short ride feature
        val expandedSectionId: String? = null
    ) : SettingsUiState

    data class Error(val message: String) : SettingsUiState
}

/*
"Theme" to listOf("Light", "Dark", "System Default"),
"Language" to listOf("English", "Spanish", "French"),
"Notifications" to listOf("Enabled", "Disabled"),
"Units" to listOf("Imperial (English)", "Metric (SI)")
 */
