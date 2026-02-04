package com.zoewave.ashbike.mobile.rides.ui

import com.zoewave.ashbike.mobile.rides.ui.model.BikeRideUiModel

/**
 * Represents the different states for the Trips screen UI.
 */
sealed interface TripsUIState {
    object Loading : TripsUIState
    data class Error(val message: String) : TripsUIState
    data class Success(
        val rides: List<BikeRideUiModel> = emptyList()
    ) : TripsUIState
}
