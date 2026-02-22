package com.zoewave.probase.ashbike.wear.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun WearHomeScreen() {
    val currentSpeed = 24f
    val distance = "1.66 km"
    val heartRate = "125"
    val calories = "150" // New metric for the right side

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
                .padding(start = 28.dp),
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

        // 3. Cals pinned to the right side (3 o'clock position)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Kcal",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
            Text(
                text = calories,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }

        // 4. The Main Center Dashboard
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = distance,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            val speedColor = if (isTracking) Color(0xFF81C784) else Color.White

            // The Tappable Box containing the Watermark and the Speed
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clickable { isTracking = !isTracking }
                    .padding(8.dp) // Large touch target
            ) {
                // The Watermark Icon (Behind the text)
                Icon(
                    imageVector = if (isTracking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (isTracking) "Stop Ride" else "Start Ride",
                    // Use a low alpha so it acts as a subtle background element
                    tint = if (isTracking) Color(0xFFE57373).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(90.dp) // Massive icon size
                )

                // The Speed Text (In front of the icon)
                Text(
                    text = currentSpeed.toInt().toString(),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = speedColor
                )
            }

            Text(
                text = "km/h",
                style = MaterialTheme.typography.labelMedium,
                color = Color.LightGray
            )
        }
    }
}

// (WearSpeedometer canvas code remains exactly the same below)
// (WearSpeedometer canvas code remains exactly the same below)

// (Keep your WearSpeedometer Composable exactly the same below this)