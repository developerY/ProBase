package com.zoewave.probase.ashbike.wear.features.rides.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Text
import com.zoewave.ashbike.model.bike.BikeRide

@Composable
fun RideHistoryPage(
    rides: List<BikeRide>, // The list of data from your Room DB
    onRideClick: (BikeRide) -> Unit, // What happens when a user taps a card
    onDeleteClick: (BikeRide) -> Unit, // What happens when a user taps the trash can
    onMapClick: (String) -> Unit, // <-- New Map Callback
    modifier: Modifier = Modifier
) {
    // ScalingLazyColumn is the standard scrolling list for Wear OS
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        // Adds padding so the top/bottom items don't get cut off by the round screen
        contentPadding = PaddingValues(
            top = 32.dp,
            bottom = 32.dp,
            start = 8.dp,
            end = 8.dp
        )
    ) {
        // Optional: A title at the top of the list
        item {
            Text(
                text = "Recent Rides",
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Loop through your list of rides and create a card for each one
        items(rides) { ride ->
            RideHistoryCard(
                ride = ride,
                // Pass the clicks up to the parent screen/ViewModel
                onRideClick = { onRideClick(ride) },
                onDeleteClick = { onDeleteClick(ride) },
                modifier = Modifier.padding(bottom = 4.dp) // Spacing between cards
            )
        }
    }
}