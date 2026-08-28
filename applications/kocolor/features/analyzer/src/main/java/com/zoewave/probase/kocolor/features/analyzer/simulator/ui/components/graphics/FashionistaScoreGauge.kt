package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CollapsibleFashionistaScoreCard(
    score: Int,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.06f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "KOCOLOR FASHIONISTA SCORE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black
                        )
                        Text(
                            text = if (isExpanded) "Tap to collapse" else "Tap for detailed score gauge",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(16.dp))
                    FashionistaScoreGauge(score = score)
                }
            }
        }
    }
}

@Composable
fun FashionistaScoreGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    // Smooth entrance animation for progress and score integer
    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(score) {
        animationTriggered = true
    }

    val targetProgress = (score.coerceIn(0, 100) / 100f)
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) targetProgress else 0f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "scoreProgress"
    )

    val animatedScoreInt by animateIntAsState(
        targetValue = if (animationTriggered) score.coerceIn(0, 100) else 0,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "scoreInt"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val outerRadius = size.minDimension / 2

                // 1. Iridescent Pearl / Metallic Outer Background Disk
                val iridescentBrush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFF3E7D3), // Champagne
                        Color(0xFFE8C8D5), // Rose Gold
                        Color(0xFFDCD5EF), // Soft Lavender
                        Color(0xFFC6DAC9), // Soft Sage
                        Color(0xFFE2CBB7), // Warm Taupe
                        Color(0xFFF3E7D3)  // Loop
                    ),
                    center = center
                )

                // Outer Iridescent Disk Base
                drawCircle(
                    brush = iridescentBrush,
                    radius = outerRadius,
                    center = center
                )

                // 2. Beveled Metallic Border Ring
                val borderStrokeWidth = 4.dp.toPx()
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = outerRadius - borderStrokeWidth / 2,
                    center = center,
                    style = Stroke(width = borderStrokeWidth)
                )

                // 3. Inner Dial Track
                val trackRadius = outerRadius - 18.dp.toPx()
                val trackStrokeWidth = 6.dp.toPx()

                // Inactive Subtle Track
                drawCircle(
                    color = Color.Black.copy(alpha = 0.08f),
                    radius = trackRadius,
                    center = center,
                    style = Stroke(width = trackStrokeWidth)
                )

                // Dominant Sterling Silver Brush with a subtle touch of Ice Blue
                val silverBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE2E8F0), // Platinum White
                        Color(0xFFCBD5E1), // Sterling Silver
                        Color(0xFF94A3B8), // Brushed Steel
                        Color(0xFF7DD3FC), // Touch of Ice Blue
                        Color(0xFFE2E8F0)  // Platinum loop
                    ),
                    start = Offset(center.x - trackRadius, center.y - trackRadius),
                    end = Offset(center.x + trackRadius, center.y + trackRadius)
                )

                val sweepAngle = animatedProgress * 360f

                // Subtle Ice-Blue Glow Aura
                drawArc(
                    color = Color(0xFF38BDF8).copy(alpha = 0.25f),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = trackStrokeWidth + 4.dp.toPx(), cap = StrokeCap.Round),
                    topLeft = Offset(center.x - trackRadius, center.y - trackRadius),
                    size = Size(trackRadius * 2, trackRadius * 2)
                )

                // Core Platinum Silver Arc
                drawArc(
                    brush = silverBrush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = trackStrokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(center.x - trackRadius, center.y - trackRadius),
                    size = Size(trackRadius * 2, trackRadius * 2)
                )

                // 4. Inner White Core Disc
                val innerCoreRadius = trackRadius - 12.dp.toPx()
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f),
                    radius = innerCoreRadius,
                    center = center
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.04f),
                    radius = innerCoreRadius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Score Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$animatedScoreInt",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 72.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.Black
                )
                Text(
                    text = "/100",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "KOCOLOR FASHIONISTA SCORE",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 2.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FashionistaScoreGaugePreview() {
    CollapsibleFashionistaScoreCard(score = 87)
}
