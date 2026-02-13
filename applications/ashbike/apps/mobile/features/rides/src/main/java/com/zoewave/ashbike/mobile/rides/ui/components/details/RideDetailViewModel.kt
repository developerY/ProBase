package com.zoewave.ashbike.mobile.rides.ui.components.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.ashbike.mobile.rides.ui.RidesEvent
import com.zoewave.probase.ashbike.database.BikeRideRepo
import com.zoewave.probase.ashbike.database.RideWithLocations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// features/trips/src/main/java/com/ylabz/basepro/applications/bike/features/trips/RideDetailViewModel.kt

@HiltViewModel
class RideDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: BikeRideRepo
) : ViewModel() {

    /*private val rideId: String = checkNotNull(
        savedStateHandle.get<String>("rideId")
    ) { "rideId required in SavedStateHandle" }*/

    // ✅ ADD a state flow or variable to hold the ID
    private val _rideId = MutableStateFlow<String?>(null)

    /** Live DB-backed flow of this ride + its locations */
    // 2. The Stream: Reacts whenever _rideId changes
    @OptIn(ExperimentalCoroutinesApi::class)
    val rideWithLocations: StateFlow<RideWithLocations?> = _rideId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repo.getRideWithLocations(id)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // 3. Called from UI/Navigation to set the ID
    fun loadRide(id: String) {
        if (_rideId.value == id) return
        _rideId.value = id
    }

    fun onEvent(event: RidesEvent) {
        when (event) {
            is RidesEvent.UpdateRideNotes -> updateNotes(event.notes)
            else -> {}
            /*RidesEvent.DeleteAll -> TODO()
            is RidesEvent.DeleteItem -> TODO()
            RidesEvent.LoadData -> TODO()
            RidesEvent.OnRetry -> TODO()
            RidesEvent.StopSaveRide -> TODO()
            is RidesEvent.BuildBikeRec -> TODO()
            is RidesEvent.SyncRide -> TODO()*/
        }
    }

    /** Called by the UI when the notes text changes */
    private fun updateNotes(newNotes: String) {
        val currentId = _rideId.value ?: return
        viewModelScope.launch {
            repo.updateRideNotes(currentId, newNotes)
        }
    }
}
