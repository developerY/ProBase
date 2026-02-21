package com.zoewave.probase.ashbike.wear.features.rides

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WearBikeViewModel @Inject constructor(
    // Inject your use cases or repositories here.
    // e.g., private val rideRepository: RideRepository,
    // e.g., private val healthServicesManager: HealthServicesManager
) : ViewModel() {

    // Internal mutable state
    private val _uiState = MutableStateFlow(BikeUiState())
    // Public immutable state exposed to AshBikeUiRoute
    val uiState: StateFlow<BikeUiState> = _uiState.asStateFlow()

    private var rideTrackingJob: Job? = null

    // The single entry point for all UI interactions
    fun onEvent(event: BikeUiEvent) {
        when (event) {
            is BikeUiEvent.StartRide -> startRide()
            is BikeUiEvent.PauseRide -> pauseRide()
            is BikeUiEvent.ResumeRide -> resumeRide()
            is BikeUiEvent.StopRide -> stopRide()
            is BikeUiEvent.AcknowledgeError -> clearError()
        }
    }

    private fun startRide() {
        _uiState.update { it.copy(isTracking = true, isPaused = false, isLoading = true) }

        // TODO: Call your foreground service or repository to start the actual tracking

        // Example: Start collecting data streams
        rideTrackingJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = false, activeRideId = "ride_${System.currentTimeMillis()}") }

            // This is where you would collect from your sensor flows:
            // healthServicesManager.heartRateFlow.collect { hr ->
            //     _uiState.update { it.copy(heartRateBpm = hr) }
            // }
        }
    }

    private fun pauseRide() {
        _uiState.update { it.copy(isPaused = true) }
        // TODO: Pause sensor collection / notify Foreground Service
    }

    private fun resumeRide() {
        _uiState.update { it.copy(isPaused = false) }
        // TODO: Resume sensor collection / notify Foreground Service
    }

    private fun stopRide() {
        rideTrackingJob?.cancel()

        // Reset state (or navigate to a summary screen logic)
        _uiState.update {
            it.copy(
                isTracking = false,
                isPaused = false,
                currentSpeedMph = 0.0f,
                heartRateBpm = 0
            )
        }
        // TODO: Save the ride to the local database / sync via DataClient
    }

    private fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        // Ensure background jobs tied specifically to this VM scope are cleaned up
        rideTrackingJob?.cancel()
    }
}