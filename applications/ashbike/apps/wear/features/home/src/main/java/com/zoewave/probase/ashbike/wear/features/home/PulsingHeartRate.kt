package com.zoewave.probase.ashbike.wear.features.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

/**
 * Zone 1 (Resting/Warmup - Under 110): Gray/Muted Blue
 *
 * Zone 2 (Fat Burn - 110 to 135): Soft Green
 *
 * Zone 3 (Cardio/Aerobic - 135 to 155): Orange
 *
 * Zone 4/5 (Peak/Anaerobic - 155+): Deep, glowing Red
 */

@Composable
fun PulsingHeartRate(
    heartRate: Int, // Changed to Int for zone math
    modifier: Modifier = Modifier,
    isTracking: Boolean = true
) {
    // 1. Determine the Heart Rate Zone Color
    val zoneColor = when {
        heartRate == 0 -> Color.DarkGray // No data
        heartRate < 110 -> Color(0xFF90CAF9) // Light Blue (Resting/Warmup)
        heartRate < 135 -> Color(0xFF81C784) // Green (Fat Burn)
        heartRate < 155 -> Color(0xFFFFB74D) // Orange (Aerobic)
        else -> Color(0xFFE53935)            // Deep Red (Peak)
    }

    // 2. The Smooth "Breathing" Animation (1 steady beat per second)
    val infiniteTransition = rememberInfiniteTransition(label = "heart_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = .7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // 3. The Colored Background Icon
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Heart Rate",
            tint = zoneColor.copy(alpha = 0.85f), // Uses our dynamic zone color!
            modifier = Modifier
                .size(52.dp)
                .graphicsLayer {
                    // Only pulse if actively tracking AND we have a valid reading
                    val shouldPulse = isTracking && heartRate > 0
                    scaleX = if (shouldPulse) scale else .5f
                    scaleY = if (shouldPulse) scale else .5f
                }
        )

        // 4. The Number Overlay
        Text(
            text = if (heartRate > 0) heartRate.toString() else "--",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 2.dp)
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