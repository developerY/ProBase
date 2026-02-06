package com.zoewave.ashbike.mobile.rides.ui

import com.zoewave.ashbike.model.bike.BikeRide


sealed class RidesEvent {
    object LoadData : RidesEvent()

    // object AddBikeRide : TripsEvent()
    data class DeleteItem(val itemId: String) : RidesEvent()
    data class UpdateRideNotes(val itemId: String, val notes: String) : RidesEvent()
    object DeleteAll : RidesEvent()
    object OnRetry : RidesEvent()

    //data class OnItemClicked(val itemId: Int) : TripsEvent()
    object StopSaveRide : RidesEvent()
    //data class SyncHeathConnect(val ride : BikeRide) : TripsEvent()

    data class SyncRide(val rideId: String) : RidesEvent()

    data class BuildBikeRec(val ride: BikeRide) : RidesEvent()

}
