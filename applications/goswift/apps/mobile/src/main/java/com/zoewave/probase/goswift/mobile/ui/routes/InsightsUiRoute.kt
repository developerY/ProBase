package com.zoewave.probase.goswift.mobile.ui.routes

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InsightsUiRoute() {
    val context = LocalContext.current
    InsightsUiScreen(
        onExportReport = {
            Toast.makeText(context, "Exporting Physician & Coach Report (PDF)...", Toast.LENGTH_SHORT).show()
        }
    )
}

@Composable
fun InsightsUiScreen(
    onExportReport: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101418))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GOSWIFT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFB4AA),
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFE1E2E9),
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                color = Color(0xFF1C2228),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF41474D))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFFF6F61), CircleShape)
                    )
                    Text(
                        text = "CYP1A2 Fast",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFE1E2E9)
                    )
                }
            }
        }

        // Time Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeFilterChip("24h Kinetic", true)
            TimeFilterChip("7D Analysis", false)
            TimeFilterChip("30D Adaptive", false)
            TimeFilterChip("Quarterly", false)
        }

        // 2x2 Grid of Stat Cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            InsightCard(
                icon = Icons.Default.Bolt,
                iconTint = Color(0xFFFF6F61),
                title = "Zero-Crash Streak",
                value = "18d",
                badgeText = "+100% vs coffee",
                modifier = Modifier.weight(1f)
            )
            InsightCard(
                icon = Icons.Default.Bedtime,
                iconTint = Color(0xFFFEF08A),
                title = "Sleep Arch. Score",
                value = "91%",
                badgeText = "+14% REM density",
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            InsightCard(
                icon = Icons.Default.Science,
                iconTint = Color(0xFFFDBA74),
                title = "Avg Daily Dose",
                value = "72 mg",
                badgeText = "-71% vs 250mg cans",
                modifier = Modifier.weight(1f)
            )
            InsightCard(
                icon = Icons.Default.SelfImprovement,
                iconTint = Color(0xFFFFB4AA),
                title = "Optimal Flow Zone",
                value = "4.8 hrs",
                badgeText = "Peak Performance",
                modifier = Modifier.weight(1f)
            )
        }

        // Microdosing vs Commercial Drinks Comparison Section
        Surface(
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "MICRODOSING VS COMMERCIAL DRINKS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8B9198),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Smooth microdose curve (coral) avoids 200mg spike and rapid 2:00 PM crash (dashed yellow).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE1E2E9)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Flow Target: 35-50mg", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFDBA74))
                    Text("Commercial Spike: 200mg", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFEF08A))
                }
            }
        }

        // Dose-Sleep Correlation Card
        Surface(
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ShowChart, contentDescription = null, tint = Color(0xFFFF6F61), modifier = Modifier.size(20.dp))
                    Text("Outlier Correlation", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFB4AA), fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Late dose at 5:15 PM shifted REM onset by 22 min. Recommended curfew remains 7:00 PM.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE1E2E9)
                )
            }
        }

        // Weekly Intake & Hydration Bar Chart
        Surface(
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(20.dp))
                    Text("WEEKLY INTAKE & HYDRATION", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198), fontFamily = FontFamily.Monospace)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    WeeklyBar("M", 0.7f)
                    WeeklyBar("T", 0.85f)
                    WeeklyBar("W", 0.6f)
                    WeeklyBar("T", 0.9f)
                    WeeklyBar("F", 0.75f)
                    WeeklyBar("S", 0.4f)
                    WeeklyBar("S", 0.5f)
                }
            }
        }

        // Physician & Coach Export Card
        Surface(
            color = Color(0xFF161B20),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(20.dp))
                    Text("Hummingbird ML Personalization", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFEF08A), fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "You maintained 100% flow in 18d without a crash. Fast CYP1A2 gene expression verified.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE1E2E9)
                )
                Button(
                    onClick = onExportReport,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F61), contentColor = Color(0xFF101418)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Export Report for Physician / Coach", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TimeFilterChip(label: String, selected: Boolean) {
    Surface(
        color = if (selected) Color(0xFFFF6F61) else Color(0xFF1C2228),
        shape = RoundedCornerShape(12.dp),
        border = if (!selected) BorderStroke(1.dp, Color(0xFF242C34)) else null
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Color(0xFF101418) else Color(0xFFE1E2E9),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun InsightCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    badgeText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF1C2228),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF242C34)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
            Text(value, style = MaterialTheme.typography.headlineLarge, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(badgeText, style = MaterialTheme.typography.labelSmall, color = Color(0xFFFDBA74))
        }
    }
}

@Composable
fun WeeklyBar(day: String, heightFraction: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(60.dp)
                .background(Color(0xFF242C34), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(heightFraction.coerceIn(0f, 1f))
                    .background(Color(0xFFFF6F61), RoundedCornerShape(4.dp))
            )
        }
        Text(day, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
    }
}
