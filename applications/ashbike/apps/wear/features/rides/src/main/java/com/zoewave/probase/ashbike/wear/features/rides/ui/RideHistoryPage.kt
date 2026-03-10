package com.zoewave.probase.ashbike.wear.features.rides.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.zoewave.ashbike.model.bike.BikeRide
import com.zoewave.ashbike.wear.rides.R.string as RidesR
import com.zoewave.probase.ashbike.wear.features.rides.RidesEvent

@Composable
fun RideHistoryPage(
    rides: List<BikeRide>,
    onEvent: (RidesEvent) -> Unit,
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
        if (rides.isEmpty()) {
            item {
                Text(
                    text = stringResource(RidesR.applications_ashbike_apps_wear_features_rides_no_recent_rides),
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = Color.Gray
                )
            }
        } else {
            item {
                Text(
                    text = stringResource(RidesR.applications_ashbike_apps_wear_features_rides_recent_rides),
                    style = MaterialTheme.typography.title3,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(rides) { ride ->
                RideHistoryCard(
                    ride = ride,
                    onEvent = onEvent, // Pass the single dispatcher down
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}