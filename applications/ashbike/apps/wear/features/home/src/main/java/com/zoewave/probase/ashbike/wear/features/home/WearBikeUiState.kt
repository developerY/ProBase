package com.zoewave.probase.ashbike.wear.features.home

// 1. Define the UI State exactly as the Compose screen needs it
// 1. Add the new hardware data to your UI State
data class WearBikeUiState(
    val currentSpeed: Float = 0f,
    val maxSpeed: Float = 40f,
    val distance: String = "0.00 km",
    val heartRate: Int = 0,
    val calories: Int = 0,
    val elevation: Double = 0.0,      // ✅ NEW: Current Altitude
    val elevationGain: Double = 0.0,  // ✅ NEW: Total Climbed
    val isTracking: Boolean = false
)