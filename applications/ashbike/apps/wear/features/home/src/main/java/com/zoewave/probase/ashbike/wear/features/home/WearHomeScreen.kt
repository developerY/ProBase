package com.zoewave.probase.ashbike.wear.features.home

import androidx.compose.runtime.Composable

@Composable
fun WearHomeScreen(
    onNavigateToRides: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // Using a Column just for temporary navigation buttons
    androidx.wear.compose.foundation.lazy.ScalingLazyColumn {
        item { androidx.wear.compose.material3.Text("Home (Speedometer)") }
        item { androidx.wear.compose.material3.Button(onClick = onNavigateToRides) { androidx.wear.compose.material3.Text("Go to Rides") } }
        item { androidx.wear.compose.material3.Button(onClick = onNavigateToSettings) { androidx.wear.compose.material3.Text("Go to Settings") } }
    }
}