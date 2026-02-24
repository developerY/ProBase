package com.zoewave.probase.ashbike.wear.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.zoewave.probase.ashbike.wear.features.home.ui.BurningCalories
import com.zoewave.probase.ashbike.wear.features.home.ui.PulsingHeartRate
import com.zoewave.probase.ashbike.wear.features.home.ui.TappableSpeedBox
import com.zoewave.probase.ashbike.wear.features.home.ui.WearSpeedometer


@Composable
fun WearHomeScreen(
    viewModel: WearBikeViewModel = hiltViewModel()
) {
    // 1. Collect the state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // 2. Pass the dynamic state into your UI components
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,

    ) {
        WearSpeedometer(
            currentSpeed = uiState.currentSpeed,
            maxSpeed = uiState.maxSpeed,
            modifier = Modifier.fillMaxSize()
        )

        // Left Cheek - Heart Rate
        PulsingHeartRate(
            heartRate = uiState.heartRate,
            isTracking = uiState.isTracking,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 42.dp, bottom = 48.dp)
        )

        // Right Cheek - Calories
        BurningCalories(
            calories = uiState.calories,
            isTracking = uiState.isTracking,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 42.dp, bottom = 48.dp)
        )

        // Center Dashboard
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
        ) {
            Text(
                text = uiState.distance,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            // Pass the toggle action to your box
            TappableSpeedBox(
                currentSpeed = uiState.currentSpeed,
                isTracking = uiState.isTracking,
                onToggle = { viewModel.toggleTracking(uiState.isTracking) }
            )

            Text(
                text = "km/h",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
        }
    }
}

// KEEP YOUR WearSpeedometer COMPOSABLE HERE

// Previews
@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "Small Watch"
)
@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "Large Watch"
)
@Composable
fun WearHomeScreenPreview() {
    MaterialTheme {
        WearHomeScreen()
    }
}