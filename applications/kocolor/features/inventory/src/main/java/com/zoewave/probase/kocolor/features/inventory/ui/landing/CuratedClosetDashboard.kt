package com.zoewave.probase.kocolor.features.inventory.ui.landing

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * CuratedClosetDashboard
 *
 * A premium fashion-tech dashboard UI for wardrobe analytics.
 */
@Composable
fun CuratedClosetDashboard(
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
    val totalPieces = analytics.totalItems
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF2F2F7)) // A bit more standard iOS/premium background
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Card: Palette & Chromatic DNA
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "PALETTE & CHROMATIC DNA",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Text(
                    text = "Wardrobe Color Intelligence",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
                
                Surface(
                    color = Color(0xFFF3E5F5), // Light purple
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "$diversityLabel Footprint · ${analytics.dna.primaryIdentity}",
                        color = Color(0xFF6A1B9A),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "$totalPieces PIECES TRACKED",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Curated Capsule Archive",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                            val displayScore = glowScore?.let { (it * 100).toInt().toString() + "%" } ?: "94%"
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
                
                Spacer(modifier = Modifier.height(24.dp))
                
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Grid of 5 color tone pills
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToneRow(color = Color(0xFFD4C4B7), name = "Sand Linen", percent = "35%", items = "${(totalPieces * 0.35).toInt()} items")
                    ToneRow(color = Color(0xFF2C2A29), name = "Noir Espresso", percent = "25%", items = "${(totalPieces * 0.25).toInt()} items")
                    ToneRow(color = Color(0xFF7A8B76), name = "Olive Sage", percent = "18%", items = "${(totalPieces * 0.18).toInt()} items")
                    ToneRow(color = Color(0xFFC18C5D), name = "Warm Ochre", percent = "12%", items = "${(totalPieces * 0.12).toInt()} items")
                    ToneRow(color = Color(0xFFC28F90), name = "Rose Accents", percent = "10%", items = "${(totalPieces * 0.10).toInt()} items")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
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
        
        // Bottom Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Behavior Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Text("BEHAVIOR", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                    Text("Active Rotation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
                    
                    Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(12.dp)) {
                        Text("+4.2% VELOCITY", color = Color(0xFFC62828), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 9.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("27%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                    Text("Active Vault Circulation", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 10.sp)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Weekly Cadence", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                    Row(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp).fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))) {
                        Box(modifier = Modifier.weight(0.7f).fillMaxSize().background(Color(0xFF4CAF50)))
                        Box(modifier = Modifier.weight(0.3f).fillMaxSize().background(Color(0xFFE0E0E0)))
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("14 Items", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("Worn this week", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("4.8x", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("Avg wears / pc", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "VIEW BEHAVIOR ->",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.clickable { onViewBehaviorClicked() }
                    )
                }
            }
            
            // Total Value Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Text("TOTAL VALUE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                    Text("Inventory Vault", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
                    
                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp)) {
                        Text("+12% vs Q3", color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 9.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(currencyFormatter.format(totalValue), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, fontSize = 24.sp, maxLines = 1)
                    Text("Across $totalPieces items · Archive", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 10.sp)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Avg item value", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                            val avgItemValue = if(totalPieces > 0) totalValue / totalPieces else 0.0
                            Text(currencyFormatter.format(avgItemValue), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cost / wear avg", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                            Text("$4.20", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
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
        
        // Footer Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                Text("Sync Updated Just Now", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            TextButton(onClick = { /* Export Audit */ }) {
                Text("EXPORT AUDIT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ToneRow(color: Color, name: String, percent: String, items: String) {
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

@Preview(showBackground = true)
@Composable
fun CuratedClosetDashboardPreview() {
    MaterialTheme {
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
        CuratedClosetDashboard(
            analytics = dummyAnalytics,
            totalValue = 6210.0,
            glowScore = 0.94f,
            diversityLabel = "Eclectic",
            onViewIntelligenceClicked = {},
            onViewInventoryClicked = {},
            onViewFootprintClicked = {},
            onViewBehaviorClicked = {}
        )
    }
}

@Preview(showBackground = true, name = "Cold Start")
@Composable
fun CuratedClosetDashboardColdStartPreview() {
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
        CuratedClosetDashboard(
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
