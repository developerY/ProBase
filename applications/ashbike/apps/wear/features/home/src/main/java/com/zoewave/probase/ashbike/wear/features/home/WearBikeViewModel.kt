package com.zoewave.probase.ashbike.wear.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.ashbike.model.bike.RideState
import com.zoewave.probase.ashbike.features.main.service.BikeServiceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// 1. Define the UI State exactly as the Compose screen needs it
@HiltViewModel
class WearBikeViewModel @Inject constructor(
    private val serviceManager: BikeServiceManager
) : ViewModel() {

    val uiState: StateFlow<WearBikeUiState> = serviceManager.rideInfo
        .map { rideInfo ->
            WearBikeUiState(
                currentSpeed = rideInfo.currentSpeed?.toFloat() ?: 0f,
                // Format distance safely
                distance = String.format("%.2f km", rideInfo.currentTripDistance ?: 0f),
                heartRate = rideInfo.heartbeat ?: 0,
                calories = rideInfo.caloriesBurned,
                elevation = rideInfo.elevation,           // ✅ Mapping from Service
                elevationGain = rideInfo.elevationGain,   // ✅ Mapping from Service
                isTracking = rideInfo.rideState == RideState.Riding // Matches your RideState enum!
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WearBikeUiState()
        )

    fun toggleTracking(isCurrentlyTracking: Boolean) {
        // Uses the exact action strings you defined in BikeForegroundService.Companion
        val action = if (isCurrentlyTracking)
            "com.zoewave.probase.ashbike.features.main.service.action.STOP_RIDE"
        else
            "com.zoewave.probase.ashbike.features.main.service.action.START_RIDE"

        serviceManager.sendCommand(action)
    }

    fun toggleDemoMode() {
        serviceManager.toggleDemoMode()
    }
}