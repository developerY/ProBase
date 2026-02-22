package com.zoewave.probase.ashbike.wear.features.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DataCycler(
    heartRate: Int,
    calories: Int,
    isTracking: Boolean,
    modifier: Modifier = Modifier
) {
    // State to track which metric is currently showing (0 = HR, 1 = Calories)
    var currentIndex by remember { mutableStateOf(0) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            // The magic interaction: Tapping cycles the index!
            .clickable { currentIndex = (currentIndex + 1) % 2 }
            .padding(8.dp) // Generous touch target for sweaty fingers
    ) {
        // The Crossfade Animation
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                // Smooth 300ms fade in and out when tapped
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "data_cycler_animation"
        ) { targetIndex ->
            when (targetIndex) {
                // Reusing the beautiful components you already built!
                0 -> PulsingHeartRate(heartRate = heartRate, isTracking = isTracking)
                1 -> BurningCalories(calories = calories, isTracking = isTracking)
            }
        }
    }
}