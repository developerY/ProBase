package com.zoewave.probase.kocolor.features.inventory.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

/**
 * CuratedClosetDashboard
 *
 * A premium fashion-tech dashboard UI for wardrobe analytics.
 */
@Composable
fun CuratedClosetDashboard(
    totalPieces: Int,
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF9F9F9)) // Very faint off-white
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Row: Behavior Analysis
        SmallStatCard(
            icon = Icons.Default.AutoAwesome,
            value = glowScore?.let { "${(it * 100).toInt()}%" } ?: "—",
            label = "BEHAVIOR",
            onClick = onViewBehaviorClicked,
            modifier = Modifier.fillMaxWidth()
        )

        // Bottom Row: Total Pieces and Total Value
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LargeVerticalCard(
                icon = Icons.Default.Checkroom,
                value = totalPieces.toString(),
                label = "TOTAL PIECES",
                subContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "${diversityLabel.uppercase()} FOOTPRINT",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp
                            )
                        }
                    }
                },
                actionText = "VIEW INTELLIGENCE",
                actionBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFD1DC), // Pastel Pink
                        Color(0xFFE0FFFF), // Pastel Cyan
                        Color(0xFFFFFACD)  // Pastel Yellow
                    )
                ),
                onClick = onViewIntelligenceClicked,
                modifier = Modifier.weight(1f)
            )

            val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
            LargeVerticalCard(
                icon = Icons.Default.MonetizationOn,
                value = currencyFormatter.format(totalValue),
                label = "TOTAL VALUE",
                valueColor = Color(0xFF1B5E20), // Dark Green
                subContent = {
                    Text(
                        text = "Across $totalPieces items",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                },
                actionText = "VIEW INVENTORY →",
                actionBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0B2010), // Dark Forest Green
                        Color(0xFF1B3D2F)
                    )
                ),
                onClick = onViewInventoryClicked,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SmallStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.DarkGray,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.Black
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

@Composable
private fun LargeVerticalCard(
    icon: ImageVector,
    value: String,
    label: String?,
    actionText: String,
    actionBrush: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Black,
    subContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier.height(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Start)
                )
                
                Spacer(Modifier.weight(0.5f))
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = when {
                            value.length > 9 -> 26.sp
                            value.length > 6 -> 32.sp
                            else -> 42.sp
                        },
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = valueColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                
                label?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                subContent?.invoke()
                
                Spacer(Modifier.weight(0.8f))
            }

            // Action Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(actionBrush)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(50)),
                    color = Color.Black.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = actionText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CuratedClosetDashboardPreview() {
    MaterialTheme {
        CuratedClosetDashboard(
            totalPieces = 54,
            totalValue = 6210.0,
            glowScore = 0.84f,
            diversityLabel = "Strategic",
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
        CuratedClosetDashboard(
            totalPieces = 3,
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
