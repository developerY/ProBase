package com.zoewave.probase.ashbike.wear.features.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import kotlinx.coroutines.delay

@Composable
fun BurningCalories(
    calories: Int,
    modifier: Modifier = Modifier,
    isTracking: Boolean = true
) {
    // 1. Dynamic Color (Kept from before, gets hotter with more calories)
    val flameColor = when {
        calories == 0 -> Color.DarkGray
        calories < 200 -> Color(0xFFFFCA28) // Amber
        calories < 500 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFFF5722)           // Red-Orange
    }

    // 2. State to toggle between Fire and Metabolism icons
    var showFireIcon by remember { mutableStateOf(true) }

    // 3. The Timer Loop
    // This launches a coroutine that swaps the icon state every 3 seconds
    // ONLY while tracking is active and calories > 0.
    LaunchedEffect(isTracking, calories > 0) {
        if (isTracking && calories > 0) {
            while (true) {
                delay(2000) // Wait 3 seconds
                showFireIcon = !showFireIcon // Toggle state
            }
        } else {
            // Reset to fire icon when stopped
            showFireIcon = true
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // 4. The Animated Icon Transition
        AnimatedContent(
            targetState = showFireIcon,
            transitionSpec = {
                // A smooth, slow crossfade with a subtle scale effect for energy
                (fadeIn(animationSpec = tween(1000)) + scaleIn(initialScale = 0.8f, animationSpec = tween(1000)))
                    .togetherWith(fadeOut(animationSpec = tween(1000)))
            },
            label = "icon_transition"
        ) { showingFire ->
            Icon(
                // Swap the icon based on the current state
                imageVector = if (showingFire) Icons.Filled.LocalFireDepartment else Icons.Filled.Bolt,
                contentDescription = if (showingFire) "Fire" else "Metabolism",
                tint = flameColor, // Both icons use the same dynamic color
                modifier = Modifier
                    .size(22.dp) // Precision size
                    .padding(bottom = 2.dp)
            )
        }

        Text(
            text = if (calories > 0) calories.toString() else "--",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(name = "Stopped (Static Fire)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PreviewCaloriesStopped() {
    MaterialTheme { BurningCalories(calories = 0, isTracking = false) }
}

@Preview(name = "Tracking (Animating)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PreviewCaloriesTracking() {
    // In the preview, you will see it slowly fade between the Fire and the Bolt icon.
    MaterialTheme { BurningCalories(calories = 350, isTracking = true) }
}

// ==========================================
// Previews
// ==========================================
@Preview(name = "Warmup (Amber)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PreviewCaloriesLow() { MaterialTheme { BurningCalories(calories = 120) } }

@Preview(name = "Mid-Ride (Orange)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PreviewCaloriesMed() { MaterialTheme { BurningCalories(calories = 350) } }

@Preview(name = "Crushing It (Red)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PreviewCaloriesHigh() { MaterialTheme { BurningCalories(calories = 650) } }