package com.zoewave.probase.ashbike.wear.features.rides.ui.maps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import com.zoewave.probase.ashbike.wear.features.rides.RidesViewModel

@Composable
fun WearRideMapRoute(
    rideId: String,
    viewModel: RidesViewModel = hiltViewModel()
) {
    // REACTIVE FIX: Let the DB stream the state. No need for manual loading booleans!
    val ride by viewModel.getRideFlow(rideId).collectAsStateWithLifecycle(initialValue = null)

    // If ride is null, it means we are still fetching from the database
    if (ride == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator() // Premium Wear OS loading indicator
        }
    } else {
        // We have the data! Safely unwrap it.
        val safeRide = ride!!
        val locations = safeRide.locations
        val address = "Address/Map of Ride" // safeRide.startAddress ?: "Unknown Location"

        if (locations.isNotEmpty()) {
            // Render the Canvas map!
            WearRideMapScreen(
                locations = locations,
                addressLabel = address
            )
        } else {
            // Fallback if a ride was saved without GPS data
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No GPS data for this ride")
            }
        }
    }
}