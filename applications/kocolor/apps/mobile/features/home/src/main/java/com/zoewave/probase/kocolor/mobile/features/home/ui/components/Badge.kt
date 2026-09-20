package com.zoewave.probase.kocolor.mobile.features.home.ui.components


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BadgeSample(
    onUvClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HALF: Weather Widget
        Box(
            modifier = Modifier
                .fillMaxWidth()
                //.clickable(onClick = onWeatherClick)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.Center
        ) {

            // UV Badge Button (Tactile 3D push-button feel)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-8).dp, y = (-12).dp)
                    .size(56.dp)
                    .clickable { onUvClick() },
                shape = CircleShape,
                color = Color(0xFFFAF6F0),
                border = BorderStroke(1.5.dp, Color.White),
                shadowElevation = 6.dp
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFC6B492).copy(alpha = 0.5f), CircleShape)
                    )

                    // Top-Right Micro Action Arrow ↗
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open UV Page",
                        tint = Color(0xFF4A2840),
                        modifier = Modifier
                            .size(9.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-10).dp, y = 10.dp)
                            .rotate(-45f)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "UV",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B5A52),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "8",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1A1A)
                        )
                        // Base Tap Indicator Bar
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(width = 12.dp, height = 2.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFBCAAA4))
                        )
                    }
                }
            }
        }

        val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
        var isExpanded by remember { mutableStateOf(false) }
        // Prismatic Chromatic Sparkle & Star Twinkle Animation for the master Collection Hub badge
        val infiniteTransition = rememberInfiniteTransition(label = "HubSparkleAnimation")

        val starRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "StarRotation"
        )

        val starScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "StarScale"
        )

        val auraScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.38f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "PrismaticAuraScale"
        )

        val auraAlpha by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.65f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "PrismaticAuraAlpha"
        )

        val chromaticAuraBrush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFFFFD700), // Gold
                Color(0xFFE91E63), // Rose
                Color(0xFF8E24AA), // Purple
                Color(0xFF00BCD4), // Cyan
                Color(0xFFFFD700)  // Gold wrap
            )
        )

        // TOP HALF: Weather Widget
        // Outer Prismatic Multi-Chromatic Aura Glow Ring
        Box(
            modifier = Modifier
                //.align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = (-12).dp)
                .size(52.dp)
                .graphicsLayer {
                    scaleX = auraScale
                    scaleY = auraScale
                    alpha = auraAlpha
                }
                .clip(CircleShape)
                .background(chromaticAuraBrush)
        ) {

// Floating Fashion Advisor / Hub Sparkle Badge (Intersecting top-right of pill, matching Weather/Routine badges)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-8).dp, y = (-12).dp)
                    .size(52.dp),
                shape = CircleShape,
                color = Color(0xFFEFE8E1).copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color.White),
                shadowElevation = 4.dp,
                onClick = { }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFC6B492).copy(alpha = 0.4f), CircleShape)
                    )
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Fashion Advisor",
                        tint = Color(0xFF5A2A54),
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                rotationZ = starRotation
                                scaleX = starScale
                                scaleY = starScale
                            }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun BadgeSamplePreview() {
    MaterialTheme {
        BadgeSample(
            onUvClick = {}
        )
    }
}