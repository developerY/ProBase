package com.zoewave.probase.ashbike.features.main.ui

import com.zoewave.probase.core.model.bike.BikeRideInfo


// Minimal Events
sealed class BikeEvent {
    object StartRide : BikeEvent()
    object StopRide : BikeEvent()
    data class NavigateToSettingsRequested(val cardKey: String?) : BikeEvent()
}

// Minimal State
sealed class BikeUiState {
    object Idle : BikeUiState()
    object Loading : BikeUiState()
    data class Success(val bikeData: BikeRideInfo? = null) : BikeUiState()
    data class Error(val message: String) : BikeUiState()
}