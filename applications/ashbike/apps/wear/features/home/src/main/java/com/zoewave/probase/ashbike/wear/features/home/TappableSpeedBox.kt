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

@Composable
fun TappableSpeedBox(
    currentSpeed: Float,
    isTracking: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Colors dynamically change based on tracking state
    val activeColor = Color(0xFF81C784) // Soft Green
    val stopColor = Color(0xFFFF5252)   // Bright Red

    val speedTextColor = if (isTracking) Color.White else activeColor
    val iconTintColor = if (isTracking) stopColor.copy(alpha = 0.5f) else activeColor.copy(alpha = 0.5f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clickable { onToggle() }
            .padding(8.dp) // Generous touch target
    ) {
        // 1. The "Ghost" Background Icon
        Icon(
            imageVector = if (isTracking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
            contentDescription = if (isTracking) "Stop Ride" else "Start Ride",
            tint = iconTintColor,
            modifier = Modifier.size(90.dp)
        )

        // 2. The Text Stack (Restoring the dark outline for perfect contrast!)
        Box(contentAlignment = Alignment.Center) {
            // Background Text: The Thick Black Border
            Text(
                text = currentSpeed.toInt().toString(),
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                style = TextStyle(
                    drawStyle = Stroke(
                        miter = 10f,
                        width = 14f, // Nice thick stroke
                        join = StrokeJoin.Round
                    )
                )
            )

            // Foreground Text: The Solid Fill
            Text(
                text = currentSpeed.toInt().toString(),
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = speedTextColor
            )
        }
    }
}

// ==========================================
// Previews (So you can isolate and go crazy)
// ==========================================
@Preview(name = "Stopped State", backgroundColor = 0xFF000000, showBackground = true)
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

@Preview(name = "Tracking State", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun TappableSpeedBoxTrackingPreview() {
    MaterialTheme {
        TappableSpeedBox(
            currentSpeed = 32f,
            isTracking = true,
            onToggle = {}
        )
    }
}