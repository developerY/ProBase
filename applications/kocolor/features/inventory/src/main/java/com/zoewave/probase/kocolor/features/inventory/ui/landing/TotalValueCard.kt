package com.zoewave.probase.kocolor.features.inventory.ui.landing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Savings
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TotalValueCard(
    analytics: WardrobeAnalytics,
    totalValue: Double,
    onViewInventoryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val totalPieces = analytics.totalItems

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded = !isExpanded },
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
                                            imageVector = Icons.Default.MonetizationOn,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = "TOTAL VALUE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp,
                                            color = Color.Black,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "Inventory Vault",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Floating Inventory Badge (Intersecting top-right of pill)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = (-12).dp)
                            .size(52.dp),
                        shape = CircleShape,
                        color = Color(0xFFE8F5E9), // Light green
                        border = BorderStroke(1.dp, Color.White),
                        shadowElevation = 4.dp,
                        onClick = onViewInventoryClicked
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.4f), CircleShape)
                            )
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = "View Inventory",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // BOTTOM HALF: Total value with Chevron (overlapping pill bottom edge)
                val chevronRotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    label = "ChevronRotation"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-14).dp)
                        .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currencyFormatter.format(totalValue),
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
                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp)) {
                            Text(
                                text = "+12% vs Q3", 
                                color = Color(0xFF2E7D32), 
                                style = MaterialTheme.typography.labelSmall, 
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), 
                                fontSize = 9.sp
                            )
                        }
                        
                        Text(
                            text = "Across $totalPieces items · Archive", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = Color.Gray, 
                            fontSize = 10.sp
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Avg item value", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                                val avgItemValue = if(totalPieces > 0) totalValue / totalPieces else 0.0
                                Text(currencyFormatter.format(avgItemValue), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Cost / wear avg", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                                val totalWears = analytics.wearHistory.size
                                val cpw = if(totalWears > 0) totalValue / totalWears else totalValue
                                Text(currencyFormatter.format(cpw), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Surface(
                            color = Color.Black,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().clickable { onViewInventoryClicked() }
                        ) {
                            Text(
                                text = "VIEW INVENTORY ->",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TotalValueCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            val dummyAnalytics = WardrobeAnalytics(
                totalItems = 54,
                activeItems = 37,
                rarelyWornItems = 14,
                neverWornItems = 3,
                wearHistory = emptyList(),
                dna = com.zoewave.probase.kocolor.features.inventory.domain.WardrobeDna("Neutral-led", "Warm-biased", "Medium depth", "Balanced contrast", "Low-to-medium chroma"),
                colorDistribution = com.zoewave.probase.kocolor.features.inventory.domain.ColorDistribution(
                    42, 31, 27, listOf(
                        com.zoewave.probase.kocolor.features.inventory.domain.ColorStat("Sand Linen", "#D4C4B7", 19, 0.35f),
                        com.zoewave.probase.kocolor.features.inventory.domain.ColorStat("Noir Espresso", "#2C2A29", 14, 0.25f),
                        com.zoewave.probase.kocolor.features.inventory.domain.ColorStat("Olive Sage", "#7A8B76", 10, 0.18f),
                        com.zoewave.probase.kocolor.features.inventory.domain.ColorStat("Warm Ochre", "#C18C5D", 6, 0.12f),
                        com.zoewave.probase.kocolor.features.inventory.domain.ColorStat("Rose Accents", "#C28F90", 5, 0.10f)
                    )
                ),
                categoryDistribution = com.zoewave.probase.kocolor.features.inventory.domain.CategoryDistribution(16, 9, 8, 7, 6, 8),
                rotation = com.zoewave.probase.kocolor.features.inventory.domain.RotationAnalytics(18, 29, 7, 61, emptyList(), emptyList()),
                versatility = com.zoewave.probase.kocolor.features.inventory.domain.VersatilityAnalytics(312, com.zoewave.probase.kocolor.features.inventory.domain.VersatileGarment("w_41", "Universal Khaki Button-Down", 18, 8, 5, 3)),
                coverage = com.zoewave.probase.kocolor.features.inventory.domain.WardrobeCoverage(0.85f, 0.50f, 0.30f, 0.65f),
                insights = emptyList(),
                analyticsCoverage = com.zoewave.probase.kocolor.features.inventory.domain.AnalyticsCoverage(1.0f, 0.85f, 0.65f, 0.95f, 217, System.currentTimeMillis() - (86400000L * 90), System.currentTimeMillis())
            )
            TotalValueCard(
                analytics = dummyAnalytics,
                totalValue = 225.0,
                onViewInventoryClicked = {}
            )
        }
    }
}
