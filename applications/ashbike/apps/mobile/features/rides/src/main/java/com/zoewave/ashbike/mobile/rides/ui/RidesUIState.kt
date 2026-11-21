package com.zoewave.ashbike.mobile.rides.ui

import com.zoewave.ashbike.mobile.rides.ui.model.BikeRideUiModel

/**
 * Represents the different states for the Trips screen UI.
 */
sealed interface RidesUIState {
    data object Loading : RidesUIState
    data class Error(val message: String) : RidesUIState
    data class Success(
        val rides: List<BikeRideUiModel> = emptyList()
    ) : RidesUIState
}
