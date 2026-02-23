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
import androidx.compose.runtime.rememberUpdatedState
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
    // 1. Dynamic Color (Gets hotter with more calories)
    val flameColor = when {
        calories == 0 -> Color.DarkGray
        calories < 200 -> Color(0xFFFFCA28) // Amber
        calories < 500 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFFF5722)           // Red-Orange
    }

    // 2. The Proportional Speed Math
    // Calculate how intense the workout is (0.0 to 1.0 fraction, assuming 1000 is a massive burn)
    val intensityFraction = (calories.toFloat() / 1000f).coerceIn(0f, 1f)

    // How long to wait before swapping icons (4000ms at start, drops to 800ms at peak intensity)
    val currentDelayMs = (4000f - (intensityFraction * 3200f)).toLong()

    // How fast the crossfade animation itself should play (1500ms down to 400ms)
    val crossfadeDuration = (1500f - (intensityFraction * 1100f)).toInt()

    // 3. Keep the loop updated WITHOUT restarting it every time calories change
    val activeDelay by rememberUpdatedState(currentDelayMs)
    var showFireIcon by remember { mutableStateOf(true) }

    // 4. The Timer Loop
    LaunchedEffect(isTracking) {
        if (isTracking) {
            while (true) {
                delay(activeDelay) // Reads the dynamically changing delay!
                showFireIcon = !showFireIcon
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
                // Pass our dynamic crossfade duration into the tweens!
                (fadeIn(animationSpec = tween(crossfadeDuration)) +
                        scaleIn(initialScale = 0.8f, animationSpec = tween(crossfadeDuration)))
                    .togetherWith(fadeOut(animationSpec = tween(crossfadeDuration)))
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
@Preview(name = "Warmup (Slow, 120 cal)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PreviewCaloriesSlow() {
    // Fades very slowly (approx 3.5 seconds)
    MaterialTheme { BurningCalories(calories = 120, isTracking = true) }
}

@Preview(name = "Crushing It (Fast, 850 cal)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PreviewCaloriesFast() {
    // Fades rapidly, showing high metabolic burn!
    MaterialTheme { BurningCalories(calories = 850, isTracking = true) }
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