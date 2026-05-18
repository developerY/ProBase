package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LuxuryBrandLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "LuxuryEffects")
    
    // Light Source / Sun Animation
    val lightX by infiniteTransition.animateFloat(
        initialValue = -150f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lightX"
    )

    // Shimmer Animation
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    // Derived Shadow Values
    val shadowOffsetX = -(lightX / 10).dp
    val shadowAlpha = (0.3f - (kotlin.math.abs(lightX) / 1000f)).coerceAtLeast(0.1f)

    Box(contentAlignment = Alignment.Center) {
        // Subtle moving light source (Sun)
        Box(
            modifier = Modifier
                .offset { IntOffset(lightX.toInt(), -20) }
                .size(40.dp)
                .blur(20.dp)
                .background(Color(0xFFFFF9C4).copy(alpha = 0.4f), CircleShape)
        )

        Text(
            text = "KoColor",
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = shadowAlpha),
                    offset = Offset(shadowOffsetX.value, 4f),
                    blurRadius = 8f
                )
            ),
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.drawWithContent {
                drawContent()
                // Premium Shimmer Overlay
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.4f),
                            Color.Transparent,
                        ),
                        start = Offset(shimmerTranslate - 200f, 0f),
                        end = Offset(shimmerTranslate, 0f)
                    ),
                    blendMode = BlendMode.SrcAtop
                )
            }
        )
    }
}
