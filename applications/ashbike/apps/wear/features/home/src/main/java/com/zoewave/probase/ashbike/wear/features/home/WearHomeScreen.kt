package com.zoewave.probase.ashbike.wear.features.home


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun WearHomeScreen(
    // We will eventually pass your ViewModel here to get live data
) {
    // Hardcoded for now to see the UI in action
    val currentSpeed = 24f

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. The Background Layer: Your custom Canvas Speedometer
        WearSpeedometer(
            currentSpeed = currentSpeed,
            maxSpeed = 40f,
            modifier = Modifier.fillMaxSize()
        )

        // 2. The Middle Layer: The Data Dashboard
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
                fontSize = 48.sp, // Massive font for quick glanceability
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

        // 3. The Foreground Layer: Action Buttons
        // Placed at the bottom gap of your 240-degree sweep angle
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { /* TODO: Start Ride */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE6E6FA), // Light Lavender from your mockup
                    contentColor = Color.Black
                )
            ) {
                Text("Go")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { /* TODO: Stop Ride */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray,
                    contentColor = Color.White
                )
            ) {
                Text("Stop")
            }
        }
    }
}