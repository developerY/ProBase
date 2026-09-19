package com.zoewave.probase.kocolor.features.inventory.ui.landing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.inventory.domain.WardrobeAnalytics

@Composable
fun WardrobInelCard(
    analytics: WardrobeAnalytics,
    totalValue: Double,
    glowScore: Float?,
    diversityLabel: String,
    onViewIntelligenceClicked: () -> Unit,
    onViewInventoryClicked: () -> Unit,
    onViewFootprintClicked: () -> Unit,
    onViewBehaviorClicked: () -> Unit,
    onViewAnalyticsClicked: () -> Unit = onViewIntelligenceClicked,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember { java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US) }
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onViewAnalyticsClicked() },
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
                                            text = "PALETTE & CHROMATIC DNA",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp,
                                            color = Color.Black,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "Wardrobe Color Intelligence",
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
                        color = Color(0xFFEADBf4), // Light purple
                        border = BorderStroke(1.dp, Color.White),
                        shadowElevation = 4.dp,
                        onClick = onViewFootprintClicked
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0xFF6A1B9A).copy(alpha = 0.4f), CircleShape)
                            )
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Explore Chromatic Blueprint",
                                tint = Color(0xFF6A1B9A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // BOTTOM HALF: Greeting/Description Row with Chevron (overlapping pill bottom edge)
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
                        text = "${analytics.totalItems} PIECES TRACKED",
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
                                    val displayScore = analytics.harmonyScore?.let { "${(it * 100).toInt()}%" }
                                        ?: if (analytics.totalItems > 0) "94%" else "0%"
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
                                text = "${analytics.colorDistribution.topColors.size} TONES TRACKED",
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
                            analytics.colorDistribution.topColors.forEach { stat ->
                                val color = try { Color(android.graphics.Color.parseColor(if (stat.hex.startsWith("#")) stat.hex else "#${stat.hex}")) } catch (e: Exception) { Color.Gray }
                                Box(modifier = Modifier.weight(stat.percentage.coerceAtLeast(0.01f)).fillMaxSize().background(color))
                            }
                        }

                        // Grid of top color tone pills
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            analytics.colorDistribution.topColors.forEach { stat ->
                                val color = try { Color(android.graphics.Color.parseColor(if (stat.hex.startsWith("#")) stat.hex else "#${stat.hex}")) } catch (e: Exception) { Color.Gray }
                                ToneRow(
                                    color = color,
                                    name = stat.name,
                                    percent = "${(stat.percentage * 100).toInt()}%",
                                    items = "${(analytics.totalItems * stat.percentage).toInt()} items"
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Season Match: 88% in active palette",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = "EXPLORE CHROMATIC BLUEPRINT ->",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.clickable { onViewFootprintClicked() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Cold Start")
@Composable
fun WardrobeIntelCardPreview() {
    MaterialTheme {
        val dummyAnalytics = WardrobeAnalytics(
            totalItems = 3,
            activeItems = 1,
            rarelyWornItems = 2,
            neverWornItems = 0,
            wearHistory = emptyList(),
            dna = com.zoewave.probase.kocolor.features.inventory.domain.WardrobeDna("Initializing", "-", "-", "-", "-"),
            colorDistribution = com.zoewave.probase.kocolor.features.inventory.domain.ColorDistribution(
                0, 0, 0, emptyList()
            ),
            categoryDistribution = com.zoewave.probase.kocolor.features.inventory.domain.CategoryDistribution(0, 0, 0, 0, 0, 0),
            rotation = com.zoewave.probase.kocolor.features.inventory.domain.RotationAnalytics(0, 0, 0, 0, emptyList(), emptyList()),
            versatility = com.zoewave.probase.kocolor.features.inventory.domain.VersatilityAnalytics(0, com.zoewave.probase.kocolor.features.inventory.domain.VersatileGarment("w_1", "Initial Item", 0, 0, 0, 0)),
            coverage = com.zoewave.probase.kocolor.features.inventory.domain.WardrobeCoverage(0f, 0f, 0f, 0f),
            insights = emptyList(),
            analyticsCoverage = com.zoewave.probase.kocolor.features.inventory.domain.AnalyticsCoverage(1.0f, 0f, 0f, 0f, 0, System.currentTimeMillis(), System.currentTimeMillis())
        )
        WardrobInelCard(
            analytics = dummyAnalytics,
            totalValue = 450.0,
            glowScore = null,
            diversityLabel = "Initializing",
            onViewIntelligenceClicked = {},
            onViewInventoryClicked = {},
            onViewFootprintClicked = {},
            onViewBehaviorClicked = {}
        )
    }
}