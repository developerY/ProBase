package com.zoewave.probase.ashbike.wear.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun WearHomeScreen() {
    val currentSpeed = 24f

    // 1. A simple state to track if the ride is active.
    // (This will eventually live in your ViewModel)
    var isTracking by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // The Background Canvas Speedometer
        WearSpeedometer(
            currentSpeed = currentSpeed,
            modifier = Modifier.fillMaxSize()
        )

        // The Data Dashboard
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "HR",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
            Text(
                text = "--",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = currentSpeed.toInt().toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "km/h",
                style = MaterialTheme.typography.labelMedium,
                color = Color.LightGray
            )

            Text(
                text = "1.66 km",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // 3. The Single Toggle Button
        Button(
            onClick = { isTracking = !isTracking },
            colors = ButtonDefaults.buttonColors(
                // Toggle between soft Green for Go, and soft Red for Stop
                containerColor = if (isTracking) Color(0xFFE57373) else Color(0xFF81C784),
                contentColor = Color.Black
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp) // Pushed up slightly so it doesn't touch the dots
                .size(48.dp) // Makes it a perfect, compact circle
        ) {
            Icon(
                // Swap the icon based on the state
                imageVector = if (isTracking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                contentDescription = if (isTracking) "Stop Ride" else "Start Ride",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// (Keep your WearSpeedometer Composable exactly the same below this)