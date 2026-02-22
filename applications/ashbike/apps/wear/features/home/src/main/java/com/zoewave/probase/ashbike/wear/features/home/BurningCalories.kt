package com.zoewave.probase.ashbike.wear.features.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Column
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
    val flameColor = when {
        calories == 0 -> Color.DarkGray
        calories < 200 -> Color(0xFFFFCA28)
        calories < 500 -> Color(0xFFFF9800)
        else -> Color(0xFFFF5722)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "fire_flicker")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = FastOutLinearInEasing),
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
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = "Calories Burned",
            tint = flameColor,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer {
                    val shouldFlicker = isTracking && calories > 0
                    scaleX = if (shouldFlicker) scale else 1f
                    scaleY = if (shouldFlicker) scale else 1f
                }
                .padding(bottom = 2.dp)
        )

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
@Preview(name = "Warmup (Amber)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PreviewCaloriesLow() { MaterialTheme { BurningCalories(calories = 120) } }

@Preview(name = "Mid-Ride (Orange)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PreviewCaloriesMed() { MaterialTheme { BurningCalories(calories = 350) } }

@Preview(name = "Crushing It (Red)", backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun PreviewCaloriesHigh() { MaterialTheme { BurningCalories(calories = 650) } }