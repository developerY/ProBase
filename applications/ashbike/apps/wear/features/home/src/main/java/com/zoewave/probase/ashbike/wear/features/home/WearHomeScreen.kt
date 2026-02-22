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
    val currentSpeed = 24f
    val distance = "1.66 km"
    val heartRate = "125"
    val calories = "150"

    var isTracking by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Background Speedometer
        WearSpeedometer(
            currentSpeed = currentSpeed,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Left Side Data (HR)
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "HR", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
            Text(text = heartRate, style = MaterialTheme.typography.titleMedium, color = Color.White)
        }

        // 3. Right Side Data (Kcal)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Kcal", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
            Text(text = calories, style = MaterialTheme.typography.titleMedium, color = Color.White)
        }

        // 4. Center Dashboard
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

            // The speed text color now also changes to match the state
            val speedColor = if (isTracking) Color.White else Color(0xFF81C784)

            // ... (Inside your main Column)

            // The Tappable Box containing the Background Icon and the Speed
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clickable { isTracking = !isTracking }
                    .padding(8.dp) // Large touch target
            ) {
                // 1. The Background Icon (Play/Stop)
                Icon(
                    imageVector = if (isTracking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (isTracking) "Stop Ride" else "Start Ride",
                    tint = if (isTracking) Color(0xFFFF5252).copy(alpha = 0.6f) else Color(0xFF81C784).copy(alpha = 0.6f),
                    modifier = Modifier.size(180.dp)
                )

                // 2. The Text Stack (Border + Fill)
                Box(contentAlignment = Alignment.Center) {
                    // Background Text: The Dark Border
                    Text(
                        text = currentSpeed.toInt().toString(),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black, // The border color
                        style = TextStyle(
                            drawStyle = Stroke(
                                miter = 10f,
                                width = 12f, // Adjust this to make the border thicker/thinner
                                join = StrokeJoin.Round // Rounds the corners of the outline
                            )
                        )
                    )

                    // Foreground Text: The Solid Fill
                    Text(
                        text = currentSpeed.toInt().toString(),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        // color = speedColor // Your existing dynamic color (White/Green)
                    )
                }
            }
        }
    }
}
// (WearSpeedometer canvas code remains below)

// Stack multiple previews to see how it scales on small vs. large watches
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
    // Wrap it in your theme if you have one, otherwise MaterialTheme is fine
    MaterialTheme {
        WearHomeScreen()
    }
}