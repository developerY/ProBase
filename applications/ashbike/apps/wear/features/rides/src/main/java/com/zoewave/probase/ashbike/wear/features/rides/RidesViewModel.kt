package com.zoewave.probase.ashbike.wear.features.rides

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.ashbike.model.bike.BikeRide // ✅ Using your Domain Model
import com.zoewave.probase.ashbike.database.BikeRideRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RidesViewModel @Inject constructor(
    private val repo: BikeRideRepo
) : ViewModel() {

    // ✅ Uses your exact repo method and maps to the Domain Model
    val ridesState: StateFlow<List<BikeRide>> = repo.getAllRides()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteRide(rideId: String) {
        viewModelScope.launch {
            // ✅ Uses your exact delete method
            repo.deleteById(rideId)
        }
    }
}