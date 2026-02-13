package com.zoewave.ashbike.mobile.rides.ui.components.details

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.ashbike.database.RideWithLocations

@Preview(
    name = "Ride Detail - Light Mode",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun RideDetailScreenPreview() {
    // 1. Mock the BikeRideEntity
    // Note: Adjust the constructor parameters if your actual Entity differs slightly
    val mockRide = com.zoewave.probase.ashbike.database.BikeRideEntity(
        rideId = "preview_ride_001",
        startTime = System.currentTimeMillis() - 3600000, // 1 hour ago
        endTime = System.currentTimeMillis(),
        totalDistance = 12500f, // 12.5 km
        averageSpeed = 22.5f,
        maxSpeed = 45.0f,
        elevationGain = 320f,
        elevationLoss = 150f,
        caloriesBurned = 650,
        startLat = 37.7749,
        startLng = -122.4194,
        endLat = 37.8049,
        endLng = -122.4294,
        notes = "Great morning ride to the cafe!",
        weatherCondition = "Sunny",
        isHealthDataSynced = false
    )

    // 2. Mock a list of locations (simulating a path)
    val mockLocations = listOf(
        com.zoewave.probase.ashbike.database.RideLocationEntity(
            rideId = "preview_ride_001",
            timestamp = mockRide.startTime,
            lat = 37.7749,
            lng = -122.4194,
            elevation = 10f
        ),
        com.zoewave.probase.ashbike.database.RideLocationEntity(
            rideId = "preview_ride_001",
            timestamp = mockRide.startTime + 1800000,
            lat = 37.7900,
            lng = -122.4200,
            elevation = 150f
        ),
        com.zoewave.probase.ashbike.database.RideLocationEntity(
            rideId = "preview_ride_001",
            timestamp = mockRide.endTime,
            lat = 37.8049,
            lng = -122.4294,
            elevation = 320f
        )
    )

    // 3. Wrap in the parent object
    val mockRideWithLocs = RideWithLocations(
        bikeRideEnt = mockRide,
        locations = mockLocations
    )

    MaterialTheme {
        RideDetailScreen(
            rideWithLocs = mockRideWithLocs,
            onEvent = {},
            onFindCafes = {}
        )
    }
}

@Preview(name = "Stat Card")
@Composable
fun StatCardPreview() {
    MaterialTheme {
        StatCard(
            label = "Max Speed",
            value = "45.0 km/h",
            modifier = Modifier.padding(8.dp)
        )
    }
}