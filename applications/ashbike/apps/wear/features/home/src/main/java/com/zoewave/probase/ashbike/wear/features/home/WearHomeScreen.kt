package com.zoewave.probase.ashbike.wear.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun WearHomeScreen() {
    val currentSpeed = 24f
    val distance = "1.66 km"
    val heartRate = "125"

    // Track the active state
    var isTracking by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. The Background Canvas Speedometer
        WearSpeedometer(
            currentSpeed = currentSpeed,
            modifier = Modifier.fillMaxSize()
        )

        // 2. HR pinned to the left side (9 o'clock position)
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 28.dp), // Pushed in so it doesn't get clipped by the curved screen
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "HR",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
            Text(
                text = heartRate,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }

        // 3. The Main Center Dashboard
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Distance replaces HR at the top
            Text(
                text = distance,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            // The Tappable Speed Number
            val speedColor = if (isTracking) Color(0xFF81C784) else Color.White // Green when active

            Text(
                text = currentSpeed.toInt().toString(),
                fontSize = 56.sp, // Bumped up slightly since there's more room
                fontWeight = FontWeight.Bold,
                color = speedColor,
                modifier = Modifier
                    .clickable { isTracking = !isTracking } // The magic interaction!
                    .padding(8.dp) // Adds a slightly larger invisible touch target area
            )

            Text(
                text = "km/h",
                style = MaterialTheme.typography.labelMedium,
                color = Color.LightGray
            )
        }
    }
}

// (WearSpeedometer canvas code remains exactly the same below)

// (Keep your WearSpeedometer Composable exactly the same below this)