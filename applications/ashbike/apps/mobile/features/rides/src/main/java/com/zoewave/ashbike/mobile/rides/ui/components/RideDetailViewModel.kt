package com.zoewave.ashbike.mobile.rides.ui.components

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.ashbike.mobile.rides.ui.RidesEvent
import com.zoewave.probase.ashbike.database.BikeRideRepo
import com.zoewave.probase.ashbike.database.RideWithLocations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// features/trips/src/main/java/com/ylabz/basepro/applications/bike/features/trips/RideDetailViewModel.kt

@HiltViewModel
class RideDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: BikeRideRepo
) : ViewModel() {

    private val rideId: String = checkNotNull(
        savedStateHandle.get<String>("rideId")
    ) { "rideId required in SavedStateHandle" }

    /** Live DB-backed flow of this ride + its locations */
    val rideWithLocations: StateFlow<RideWithLocations?> =
        repo.getRideWithLocations(rideId)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun onEvent(event: RidesEvent) {
        when (event) {
            is RidesEvent.UpdateRideNotes -> updateNotes(event.notes)
            RidesEvent.DeleteAll -> TODO()
            is RidesEvent.DeleteItem -> TODO()
            RidesEvent.LoadData -> TODO()
            RidesEvent.OnRetry -> TODO()
            RidesEvent.StopSaveRide -> TODO()
            is RidesEvent.BuildBikeRec -> TODO()
            is RidesEvent.SyncRide -> TODO()
        }
    }

    /** Called by the UI when the notes text changes */
    fun updateNotes(newNotes: String) {
        viewModelScope.launch {
            repo.updateRideNotes(rideId, newNotes)
            // no need to re-load—Flow will emit the updated row automatically
        }
    }
}
