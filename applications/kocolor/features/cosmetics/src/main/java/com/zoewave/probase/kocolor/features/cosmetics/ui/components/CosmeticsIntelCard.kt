package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CosmeticsIntelCard(
    totalItems: Int,
    onViewIntelligenceClicked: () -> Unit,
    onViewFootprintClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "CosmeticsIntelAnimation")

    val badgeScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BadgeScaleTransition"
    )

    val chromaticBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFA0C4FF), Color(0xFFBDB2FF), Color(0xFFFFADAD),
            Color(0xFFFFD6A5), Color(0xFFFDFFB6), Color(0xFFCAFFBF)
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFFF3F3F7)
    ) {
        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TOP HALF: Main Glassmorphic Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewIntelligenceClicked() }
                        .padding(top = 20.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Translucent frosted glass pill
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(86.dp),
                        shape = RoundedCornerShape(44.dp),
                        color = Color.White.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = "SPECTRAL & CHROMATIC DNA",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp,
                                            color = Color.Black,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "Color Intelligence Hub",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Floating Footprint / Tone Badge (Intersecting top-right of pill)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = (-12).dp)
                            .size(52.dp),
                        shape = CircleShape,
                        color = Color.Transparent,
                        shadowElevation = 4.dp,
                        onClick = onViewFootprintClicked
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(chromaticBrush)
                                    .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            )
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Explore Chromatic Blueprint",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        scaleX = badgeScale
                                        scaleY = badgeScale
                                    }
                            )
                        }
                    }
                }

                // BOTTOM HALF: Items Tracked Row with Chevron (overlapping pill bottom edge)
                val chevronRotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    label = "ChevronRotation"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .offset(y = (-14).dp)
                        .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$totalItems ITEMS TRACKED",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937),
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF8C7F72),
                        modifier = Modifier.size(20.dp).rotate(chevronRotation)
                    )
                }

                // Collapsible Content
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Curated Capsule Archive",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Score Banner
                        Surface(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ALGORITHMIC COHESION",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val displayScore = if (totalItems > 0) "89%" else "0%"
                                    Text(
                                        text = "$displayScore HARMONY SCORE",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107)
                                )
                            }
                        }

                        // Chromatic Spectrum Distribution
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CHROMATIC SPECTRUM DISTRIBUTION",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "5 TONES TRACKED",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }

                        // Continuous color bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            Box(modifier = Modifier.weight(0.35f).fillMaxSize().background(Color(0xFFD4C4B7)))
                            Box(modifier = Modifier.weight(0.25f).fillMaxSize().background(Color(0xFF2C2A29)))
                            Box(modifier = Modifier.weight(0.18f).fillMaxSize().background(Color(0xFF7A8B76)))
                            Box(modifier = Modifier.weight(0.12f).fillMaxSize().background(Color(0xFFC18C5D)))
                            Box(modifier = Modifier.weight(0.10f).fillMaxSize().background(Color(0xFFC28F90)))
                        }

                        // Grid of top color tone pills
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ToneRowStatic(color = Color(0xFFD4C4B7), name = "Sand Linen", percent = "35%", items = "${(totalItems * 0.35).toInt()} items")
                            ToneRowStatic(color = Color(0xFF2C2A29), name = "Noir Espresso", percent = "25%", items = "${(totalItems * 0.25).toInt()} items")
                            ToneRowStatic(color = Color(0xFF7A8B76), name = "Olive Sage", percent = "18%", items = "${(totalItems * 0.18).toInt()} items")
                            ToneRowStatic(color = Color(0xFFC18C5D), name = "Warm Ochre", percent = "12%", items = "${(totalItems * 0.12).toInt()} items")
                            ToneRowStatic(color = Color(0xFFC28F90), name = "Rose Accents", percent = "10%", items = "${(totalItems * 0.10).toInt()} items")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Season Match: 86% in active palette",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToneRowStatic(color: Color, name: String, percent: String, items: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
            Text(name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(percent, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(items, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Cosmetics Intel Card Preview")
@Composable
private fun CosmeticsIntelCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CosmeticsIntelCard(
                totalItems = 175,
                onViewIntelligenceClicked = {},
                onViewFootprintClicked = {}
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Tone Row Static Preview")
@Composable
private fun ToneRowStaticPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ToneRowStatic(
                color = Color(0xFFD4C4B7),
                name = "Sand Linen",
                percent = "35%",
                items = "42 items"
            )
        }
    }
}
