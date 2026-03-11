package com.zoewave.probase.ashbike.wear.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.ashbike.model.R
import com.zoewave.ashbike.model.bike.RideState
import com.zoewave.ashbike.model.formatting.RideMetricsFormatter
import com.zoewave.ashbike.model.formatting.toMph
import com.zoewave.probase.ashbike.database.repository.UserProfileRepository
import com.zoewave.probase.ashbike.features.main.service.BikeServiceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// 1. Define the UI State exactly as the Compose screen needs it

@HiltViewModel
class WearBikeViewModel @Inject constructor(
    private val serviceManager: BikeServiceManager,
    private val userProfileRepository: UserProfileRepository,
    private val metricsFormatter: RideMetricsFormatter
) : ViewModel() {

    // Combine the live ride data with the user's unit preference
    val uiState: StateFlow<WearBikeUiState> = combine(
        serviceManager.rideInfo,
        userProfileRepository.isImperialFlow
    ) { rideInfo, isImperial ->



        // 1. Calculate the Canvas Speed properties
        val rawSpeedKmh = rideInfo.currentSpeed?.toFloat() ?: 0f
        val displaySpeed = if (isImperial) rawSpeedKmh.toMph() else rawSpeedKmh
        val dialMaxSpeed = if (isImperial) 40f else 60f // Automatically shrinks the dial for mph
        val speedUnitId = if (isImperial) R.string.applications_ashbike_model_unit_mph else R.string.applications_ashbike_model_unit_kmh


        // 2. Build the final UI state
        WearBikeUiState(
            currentSpeed = displaySpeed,
            maxSpeed = dialMaxSpeed,
            speedUnitResId = speedUnitId,

            // Format the text fields safely
            distance = metricsFormatter.formatDistance(
                distanceKm = (rideInfo.currentTripDistance / 1000f), // Ensure this is converted to km if it's in meters!
                isImperial = isImperial
            ),
            elevation = metricsFormatter.formatElevation(
                elevationMeters = rideInfo.elevation ?: 0.0,
                isImperial = isImperial
            ),
            elevationGain = metricsFormatter.formatElevation(
                elevationMeters = rideInfo.elevationGain ?: 0.0,
                isImperial = isImperial
            ),

            heartRate = rideInfo.heartbeat ?: 0,
            calories = rideInfo.caloriesBurned,
            isTracking = rideInfo.rideState == RideState.Riding
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WearBikeUiState()
    )

    fun toggleTracking(isCurrentlyTracking: Boolean) {
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