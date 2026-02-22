package com.zoewave.probase.ashbike.wear.features.rides

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable

@Composable
fun WearRidesScreen() {
    androidx.compose.foundation.layout.Box(contentAlignment = androidx.compose.ui.Alignment.Center, modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        androidx.wear.compose.material3.Text("Rides List (Swipe Right to go back)")
    }
}