package com.zoewave.probase.ashbike.wear.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.lerp // ✅ Needed for the color math!

@Composable
fun TappableSpeedBox(
    currentSpeed: Float,
    maxSpeed: Float = 40f, // Need maxSpeed to calculate the fraction
    isTracking: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. The exact colors from your WearSpeedometer gauge
    val gaugeGreen = Color(0xFF81C784)
    val gaugeYellow = Color(0xFFFFF176)
    val gaugeRed = Color(0xFFFF5252)

    // 2. The Color Math (Interpolation)
    val fraction = (currentSpeed / maxSpeed).coerceIn(0f, 1f)

    val dynamicSpeedColor = when {
        // If in the bottom half of the speedometer, blend Green -> Yellow
        fraction <= 0.5f -> lerp(start = gaugeGreen, stop = gaugeYellow, fraction = fraction * 2f)
        // If in the top half, blend Yellow -> Red
        else -> lerp(start = gaugeYellow, stop = gaugeRed, fraction = (fraction - 0.5f) * 2f)
    }

    // 3. UI State Logic
    // Uses the dynamic color when tracking, drops back to pure White when paused
    val speedTextColor = if (isTracking) dynamicSpeedColor else Color.White
    val stopColor = Color(0xFFFF5252)
    val iconTintColor = if (isTracking) stopColor.copy(alpha = 0.4f) else gaugeGreen.copy(alpha = 0.5f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clickable { onToggle() }
            .padding(8.dp)
    ) {
        // Background Icon
        Icon(
            imageVector = if (isTracking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
            contentDescription = if (isTracking) "Stop Ride" else "Start Ride",
            tint = iconTintColor,
            modifier = Modifier.size(90.dp)
        )

        // Text Stack
        Box(contentAlignment = Alignment.Center) {
            // Outline
            Text(
                text = currentSpeed.toInt().toString(),
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                style = TextStyle(
                    drawStyle = Stroke(
                        miter = 10f,
                        width = 14f,
                        join = StrokeJoin.Round
                    )
                )
            )

            // Dynamic Color Fill
            Text(
                text = currentSpeed.toInt().toString(),
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = speedTextColor // ✅ Matches the exact point on the gauge!
            )
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(name = "Stopped (White)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun TappableSpeedBoxStoppedPreview() {
    MaterialTheme {
        TappableSpeedBox(
            currentSpeed = 24f,
            isTracking = false,
            onToggle = {}
        )
    }
}

@Preview(name = "Tracking - Med Speed (Yellowish)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun TappableSpeedBoxTrackingMedPreview() {
    MaterialTheme {
        TappableSpeedBox(
            currentSpeed = 22f, // Right around the middle, will interpolate to yellow-orange
            isTracking = true,
            onToggle = {}
        )
    }
}

@Preview(name = "Tracking - High Speed (Red)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun TappableSpeedBoxTrackingHighPreview() {
    MaterialTheme {
        TappableSpeedBox(
            currentSpeed = 38f, // Pushing max speed, will be deeply red
            isTracking = true,
            onToggle = {}
        )
    }
}