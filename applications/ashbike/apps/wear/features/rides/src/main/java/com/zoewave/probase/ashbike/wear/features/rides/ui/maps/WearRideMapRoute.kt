package com.zoewave.probase.ashbike.wear.features.rides.ui.maps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.wear.compose.material.Text
import com.zoewave.ashbike.model.bike.BikeRide
import com.zoewave.probase.ashbike.wear.features.rides.RidesViewModel

@Composable
fun WearRideMapRoute(
    rideId: String,
    viewModel: RidesViewModel = hiltViewModel()
) {
    // Hold the fetched ride and a loading state
    var ride by remember { mutableStateOf<BikeRide?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch the data exactly once when the screen opens
    LaunchedEffect(key1 = rideId) {
        isLoading = true
        ride = viewModel.getRide(rideId) // Your suspend function!
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Mapping route...")
        }
    } else {
        // Use standard Kotlin safe-calls to handle the data
        val locations = ride?.locations ?: emptyList()
        val address = "Address/Map of Ride" //ride?.startAddress ?: "Unknown Location"

        if (locations.isNotEmpty()) {
            // Render the Canvas map!
            WearRideMapScreen(
                locations = locations,
                addressLabel = address
            )
        } else {
            // Fallback if a ride was saved without GPS data
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No GPS data for this ride for ride ${ride?.locations}")
            }
        }
    }
}