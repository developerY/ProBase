package com.zoewave.probase.ashbike.wear.features.home

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
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
fun BurningCalories(
    calories: Int,
    modifier: Modifier = Modifier,
    isTracking: Boolean = true
) {
    // 1. The Flame Color (Gets "hotter" as you burn more)
    val flameColor = when {
        calories == 0 -> Color.DarkGray
        calories < 200 -> Color(0xFFFFCA28) // Amber/Yellow
        calories < 500 -> Color(0xFFFF9800) // Bright Orange
        else -> Color(0xFFFF5722)           // Deep Red-Orange
    }

    // 2. The Flicker Animation
    // We use a faster duration (300ms) and Linear easing to simulate a dancing flame
    val infiniteTransition = rememberInfiniteTransition(label = "fire_flicker")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // 3. The Flame Icon
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = "Calories Burned",
            tint = flameColor.copy(alpha = 0.85f),
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    val shouldFlicker = isTracking && calories > 0
                    scaleX = if (shouldFlicker) scale else 1f
                    scaleY = if (shouldFlicker) scale else 1f
                }
        )

        // 4. The Number Overlay
        Text(
            text = if (calories > 0) calories.toString() else "--",
            fontSize = 15.sp, // Slightly smaller to fit the tapered shape of a flame
            fontWeight = FontWeight.Bold,
            color = Color.White,
            // Optical Centering: A flame is wide at the bottom and pointy at the top.
            // We push the text down so it sits securely in the "belly" of the fire!
            modifier = Modifier.padding(top = 10.dp)
        )
    }
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