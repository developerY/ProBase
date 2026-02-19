package com.zoewave.probase.features.health.ui

import androidx.health.connect.client.records.Record

sealed interface HealthEvent {
    data object RequestPermissions : HealthEvent
    data object LoadHealthData : HealthEvent
    data object DeleteAll : HealthEvent
    data object Retry : HealthEvent
    data object ReadAll : HealthEvent

    // New Events
    data object WriteTestRide : HealthEvent
    data object ManagePermissions : HealthEvent // <--- Added this

    data class DeleteSession(val uid: String) : HealthEvent // <--- NEW

    /** Insert a prepared list of Health Connect Record objects */
    data class Insert(
        val rideId: String,
        val records: List<Record>
    ) : HealthEvent
}