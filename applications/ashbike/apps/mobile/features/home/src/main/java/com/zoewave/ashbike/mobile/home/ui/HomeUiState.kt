package com.zoewave.ashbike.mobile.home.ui

import com.zoewave.ashbike.model.bike.BikeRideInfo
import com.zoewave.ashbike.model.bike.LocationEnergyLevel
import com.zoewave.ashbike.model.glass.GlassButtonState

sealed class HomeUiState {
    /** we haven't yet gotten our first fix */
    object WaitingForGps : HomeUiState()
    object Loading : HomeUiState()
    object Idle : HomeUiState()

    data class Success(
        val bikeData: BikeRideInfo,
        val showSetDistanceDialog: Boolean = false,
        val showGpsCountdown: Boolean = true, // Add the new property
        val locationEnergyLevel: LocationEnergyLevel = LocationEnergyLevel.BALANCED, // <<< ADD THIS LINE

        // --- GLASS STATE ---
        val glassGear: Int = 1,
        val isGlassActive: Boolean = false, // (Optional: keep for legacy or other UI logic)
        val glassButtonState: GlassButtonState = GlassButtonState.NO_GLASSES // <--- NEW SOURCE OF TRUTH

    ) : HomeUiState() {

        // =====================================================================
        // COMPUTED PROPERTIES (The "Brain" for your UI formatting)
        // =====================================================================

        val formattedBattery: String
            get() = if (bikeData.isBikeConnected) {
                bikeData.batteryLevel?.let { "$it%" } ?: "--%"
            } else {
                "--%"
            }

        val formattedMotor: String
            get() = if (bikeData.isBikeConnected) {
                // Show power only if > 0
                bikeData.motorPower?.takeIf { it > 0 }?.let { "${it.toInt()} W" } ?: "-- W"
            } else {
                "-- W"
            }

        val formattedGear: String
            get() = if (bikeData.isBikeConnected) {
                "$glassGear"
            } else {
                "--"
            }
}

    data class Error(val message: String) : HomeUiState()
}


sealed interface BikeSideEffect {
    data object LaunchGlassProjection : BikeSideEffect
    data class ShowToast(val message: String) : BikeSideEffect
}

/*
"Theme" to listOf("Light", "Dark", "System Default"),
        "Language" to listOf("English", "Spanish", "French"),
        "Notifications" to listOf("Enabled", "Disabled")
 */
/*
val location: Location? = null,
        val currentSpeed: Double = 0.0,     // current speed (km/h)
        val currentDistance: Double = 0.0, // current trip distance (km)
        val totalDistance: Double = 50.0,   // total trip distance (km)
        val locationString :String = "Santa Barbara, US",
        val averageSpeed : Double = 25.0,
        val elevation : Double = 150.0,
        val heading : Float = 0f,
        val bikeID : String? = null,
        val batteryLevel : Int? = null,
        val motorPower : Float? = null,
        val isBikeConnected : Boolean = false,

        // Just a place holder
        val speedKmh : Float = 0.0f,
        // Just a place holder
        val remainingDistance : Float = 0.0f,

        val rideDuration : String = "0h 0m"
 */