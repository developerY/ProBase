package com.zoewave.probase.ashbike.wear.features.rides

import com.zoewave.ashbike.model.bike.BikeRide

// 1. What the UI can SHOW
sealed interface RidesUiState {
    object Loading : RidesUiState
    data class Success(val rides: List<BikeRide>) : RidesUiState
    data class Error(val message: String) : RidesUiState
}