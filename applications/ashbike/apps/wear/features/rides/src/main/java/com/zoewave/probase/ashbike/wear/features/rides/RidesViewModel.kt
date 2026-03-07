package com.zoewave.probase.ashbike.wear.features.rides

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.ashbike.model.bike.BikeRide
import com.zoewave.probase.ashbike.database.BikeRideRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RidesViewModel @Inject constructor(
    private val repo: BikeRideRepo
) : ViewModel() {

    // The single source of truth for the UI
    val uiState: StateFlow<RidesUiState> = repo.getAllRides()
        .map { rides ->
            if (rides.isEmpty()) {
                RidesUiState.Success(emptyList()) as RidesUiState // Or create an Empty state
            } else {
                RidesUiState.Success(rides) as RidesUiState
            }
        }
        .catch { emit(RidesUiState.Error(it.message ?: "Unknown error occurred")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RidesUiState.Loading
        )

    // The ONLY public function exposed to the UI
    fun onEvent(event: RidesEvent) {
        when (event) {
            is RidesEvent.OnDeleteClick -> deleteRide(event.rideId)
            is RidesEvent.OnForceSyncClick -> syncRide(event.ride)
            is RidesEvent.OnRideClick -> {
                // Usually handled by Compose Navigation at the screen level,
                // but you can fire analytics or side-effects here if needed.
            }
        }
    }

    // --- Private Business Logic ---

    private fun deleteRide(rideId: String) {
        viewModelScope.launch {
            // Uses your exact delete method
            repo.deleteById(rideId)
        }
    }

    private fun syncRide(ride: BikeRide) {
        viewModelScope.launch {
            // TODO: Trigger your WearRideSyncEngine here!
        }
    }

    /** * If you are using this same VM for the Detail screen,
     * use the new reactive flow we built instead of a one-shot fetch!
     */
    fun getRideFlow(rideId: String) = repo.getRideFlow(rideId)
}