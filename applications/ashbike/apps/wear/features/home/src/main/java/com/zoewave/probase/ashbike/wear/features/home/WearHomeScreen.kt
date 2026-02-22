package com.zoewave.probase.ashbike.wear.features.home

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices

@Composable
fun WearHomeScreen() {
    // Placeholder data (Will eventually come from ViewModel)
    val currentSpeed = 24f
    val distance = "1.66 km"
    val heartRate = "125"
    val calories = "150"

    // Local state for the UI toggle
    var isTracking by remember { mutableStateOf(false) }

    // Define colors based on tracking state
    val activeColor = Color(0xFF81C784) // Soft Green
    val stopColor = Color(0xFFFF5252)   // Bright Red
    val speedTextColor = if (isTracking) Color.White else activeColor
    val iconTintColor = if (isTracking) stopColor.copy(alpha = 0.5f) else activeColor.copy(alpha = 0.5f)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Background Canvas Speedometer
        WearSpeedometer(
            currentSpeed = currentSpeed,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Left Side Data (HR) - Aligned to Start (Left)
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 26.dp),
            horizontalAlignment = Alignment.Start // <= Hugs left edge
        ) {
            Text(text = "HR", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
            Text(text = heartRate, style = MaterialTheme.typography.titleMedium, color = Color.White)
        }

        // 3. Right Side Data (Kcal) - Aligned to End (Right)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 26.dp),
            horizontalAlignment = Alignment.End // <= Hugs right edge
        ) {
            Text(text = "Kcal", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
            Text(text = calories, style = MaterialTheme.typography.titleMedium, color = Color.White)
        }

        // 4. Center Dashboard (Distance & Speed)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp) // <= Moves the whole central stack down a little
        ) {
            // Distance Text
            Text(
                text = distance,
                style = MaterialTheme.typography.titleSmall, // Slightly smaller hierarchy
                color = Color.White,
                modifier = Modifier.padding(bottom = 2.dp) // Space between distance and speed box
            )

            // The Tappable Speed Box
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clickable { isTracking = !isTracking }
                    .padding(4.dp)
            ) {
                // Background Icon
                Icon(
                    imageVector = if (isTracking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (isTracking) "Stop Ride" else "Start Ride",
                    tint = iconTintColor,
                    modifier = Modifier.size(85.dp)
                )

                // Text Stack (Border + Fill)
                Box(contentAlignment = Alignment.Center) {
                    // Dark Border outline
                    Text(
                        text = currentSpeed.toInt().toString(),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        style = TextStyle(
                            drawStyle = Stroke(
                                miter = 10f,
                                width = 10f,
                                join = StrokeJoin.Round
                            )
                        )
                    )
                    // Solid foreground text
                    Text(
                        text = currentSpeed.toInt().toString(),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = speedTextColor
                    )
                }
            }

            // Unit Label
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