package com.zoewave.probase.ashbike.wear.presentation.screens.ride

// Represents everything the Wear UI needs to draw itself
data class BikeUiState(
    val isLoading: Boolean = false,
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val currentSpeedMph: Float = 0.0f,
    val distanceMiles: Float = 0.0f,
    val heartRateBpm: Int = 0,
    val activeRideId: String? = null,
    val errorMessage: String? = null
)