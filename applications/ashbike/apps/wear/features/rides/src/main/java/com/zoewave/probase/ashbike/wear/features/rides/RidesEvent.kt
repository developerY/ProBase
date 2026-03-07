package com.zoewave.probase.ashbike.wear.features.rides

import com.zoewave.ashbike.model.bike.BikeRide

// 2. What the UI can DO (The Intents)
sealed interface RidesEvent {
    data class OnRideClick(val rideId: String) : RidesEvent
    data class OnDeleteClick(val rideId: String) : RidesEvent
    data class OnForceSyncClick(val ride: BikeRide) : RidesEvent
}