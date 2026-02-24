package com.zoewave.probase.ashbike.wear.features.home.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun PulsingHeartRate(
    heartRate: Int,
    modifier: Modifier = Modifier,
    isTracking: Boolean = true
) {
    val zoneColor = when {
        heartRate == 0 -> Color.DarkGray
        heartRate < 110 -> Color(0xFF90CAF9)
        heartRate < 135 -> Color(0xFF81C784)
        heartRate < 155 -> Color(0xFFFFB74D)
        else -> Color(0xFFE53935)
    }

    val animationDurationMs = remember(heartRate) {
        if (heartRate <= 0) return@remember 500
        val duration = ((60f / heartRate.toFloat()) * 1000f) / 2f
        duration.toInt().coerceIn(160, 600)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "heart_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f, // Slightly larger pulse since the icon is smaller
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDurationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    // The Minimalist Vertical Stack
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Heart Rate",
            tint = zoneColor, // Solid, sharp color
            modifier = Modifier
                .size(22.dp) // Tiny, precision size
                .graphicsLayer {
                    val shouldPulse = isTracking && heartRate > 0
                    scaleX = if (shouldPulse) scale else 1f
                    scaleY = if (shouldPulse) scale else 1f
                }
                .padding(bottom = 2.dp) // Tiny gap between icon and text
        )

        Text(
            text = if (heartRate > 0) heartRate.toString() else "--",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// ==========================================
// Previews (Test all the zones!)
// ==========================================
@Preview(name = "Zone 1 (Blue)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PreviewZone1() { MaterialTheme { PulsingHeartRate(heartRate = 95) } }

@Preview(name = "Zone 2 (Green)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PreviewZone2() { MaterialTheme { PulsingHeartRate(heartRate = 125) } }

@Preview(name = "Zone 3 (Orange)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PreviewZone3() { MaterialTheme { PulsingHeartRate(heartRate = 145) } }

@Preview(name = "Zone 4 (Red)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PreviewZone4() { MaterialTheme { PulsingHeartRate(heartRate = 165) } }