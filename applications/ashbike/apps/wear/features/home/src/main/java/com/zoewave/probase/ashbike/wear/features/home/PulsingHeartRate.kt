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

@Composable
fun PulsingHeartRate(
    heartRate: String,
    modifier: Modifier = Modifier,
    isTracking: Boolean = true // We can use this to pause the pulse when stopped!
) {
    // 1. The Heartbeat Animation setup
    val infiniteTransition = rememberInfiniteTransition(label = "heart_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f, // Expands by 15%
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // 2. The Pulsing Background Icon
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Heart Rate",
            tint = Color(0xFFE53935).copy(alpha = 0.85f), // Nice deep translucent red
            modifier = Modifier
                .size(48.dp) // Large enough to hold the text
                .graphicsLayer {
                    // Only pulse if the ride is active
                    scaleX = if (isTracking) scale else 1f
                    scaleY = if (isTracking) scale else 1f
                }
        )

        // 3. The Number Overlay
        Text(
            text = heartRate,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            // The default Favorite icon visually holds weight at the top.
            // A tiny bottom padding optical-centers the text perfectly inside the heart!
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(name = "Tracking (Pulsing)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PulsingHeartRateTrackingPreview() {
    MaterialTheme {
        // Look at the preview window, you will actually see it beating!
        PulsingHeartRate(heartRate = "125", isTracking = true)
    }
}

@Preview(name = "Stopped (Static)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PulsingHeartRateStoppedPreview() {
    MaterialTheme {
        PulsingHeartRate(heartRate = "125", isTracking = false)
    }
}