package com.zoewave.probase.goswift.mobile.shots.ui

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ShotsUiRoute(
    viewModel: ShotsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    ShotsUiScreen(uiState = uiState)
}

@Composable
fun ShotsUiScreen(
    uiState: ShotsUiState = ShotsUiState.Loading
) {
    val successState = uiState as? ShotsUiState.Success
    val intendedMg = successState?.intendedActiveDosingMg ?: 73
    val loggedMg = successState?.loggedMg ?: 35
    val safeCeilingMg = successState?.safeCeilingMg ?: 120
    val timelineItems = successState?.timelineItems ?: emptyList()

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
                    text = "Schedule",
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
                        text = "AI Active",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFE1E2E9)
                    )
                }
            }
        }

        // Date Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DateChip("Mon 23", false)
            DateChip("Tue 24", false)
            DateChip("Today 25", true)
            DateChip("Thu 26", false)
            DateChip("Fri 27", false)
        }

        // Intended Active Dosing Card
        Surface(
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INTENDED ACTIVE DOSING",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8B9198),
                        fontFamily = FontFamily.Monospace
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Bedtime, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(16.dp))
                        Text("Curfew 7:00 PM", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFEF08A))
                    }
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$intendedMg",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFFFFB4AA),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "mg / ${safeCeilingMg}mg Safe Ceiling",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8B9198),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                LinearProgressIndicator(
                    progress = (loggedMg.toFloat() / safeCeilingMg.toFloat()).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFFFF6F61),
                    trackColor = Color(0xFF242C34)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val percent = if (intendedMg > 0) (loggedMg * 100 / intendedMg) else 0
                    Text("Logged: ${loggedMg}mg ($percent%)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B9198))
                    Text("REM Buffer: Optimal", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFDBA74))
                }
            }
        }

        // AI Tuning Profile Card
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
                    Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFFFDBA74), modifier = Modifier.size(20.dp))
                    Text("AI Tuning Profile", style = MaterialTheme.typography.labelMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Ultra-Steady Flow", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFDBA74))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileChip("Steady (10-15mg)", true)
                    ProfileChip("Athletic (25-35mg)", false)
                    ProfileChip("Gentle Taper", false)
                }
            }
        }

        // Hummingbird On-Device ML Card
        Surface(
            color = Color(0xFF161B20),
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
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFFF6F61), modifier = Modifier.size(20.dp))
                    Text("Hummingbird On-Device ML", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFB4AA), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Just now", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                }
                Text("Biometric Dip Detected:", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                Text(
                    "HRV trending -11% during your 2:00 PM sprint. Pre-loading 18mg Hummingbird fuel + 250ml water maintains steady state.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFC1C7CE)
                )
            }
        }

        // Target Timeline Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TARGET TIMELINE",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B9198),
                fontFamily = FontFamily.Monospace
            )
            Surface(
                color = Color(0xFF242C34),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(12.dp))
                    Text("Sync to Google Calendar", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFEF08A))
                }
            }
        }

        // Timeline Items
        timelineItems.forEach { item ->
            TimelineItem(
                time = item.time,
                title = item.title,
                subtitle = item.subtitle,
                amount = "${item.amountMg}mg",
                status = item.status,
                isCompleted = item.isCompleted,
                isNextWindow = item.isNextWindow,
                isCurfew = item.isCurfew
            )
        }

        // CYP1A2 Gene Profile Footer Card
        Surface(
            color = Color(0xFF161B20),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Science, contentDescription = null, tint = Color(0xFFFFB4AA), modifier = Modifier.size(24.dp))
                Column {
                    Text("CYP1A2 GENE PROFILE ACTIVE", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFB4AA), fontWeight = FontWeight.Bold)
                    Text("Fast metabolizer detected: 4.8h serum half-life.", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC1C7CE))
                }
            }
        }
    }
}

@Composable
fun DateChip(label: String, selected: Boolean) {
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ProfileChip(label: String, active: Boolean) {
    Surface(
        color = if (active) Color(0xFF242C34) else Color(0xFF161B20),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (active) Color(0xFFFDBA74) else Color(0xFF242C34))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) Color(0xFFFDBA74) else Color(0xFF8B9198),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun TimelineItem(
    time: String,
    title: String,
    subtitle: String,
    amount: String,
    status: String,
    isCompleted: Boolean,
    isNextWindow: Boolean = false,
    isCurfew: Boolean = false
) {
    Surface(
        color = if (isNextWindow) Color(0xFF1C2228) else Color(0xFF161B20),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isNextWindow) Color(0xFFFF6F61) else Color(0xFF242C34)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(time, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198), fontFamily = FontFamily.Monospace)
                    Surface(
                        color = when {
                            isCompleted -> Color(0xFF242C34)
                            isNextWindow -> Color(0xFFFF6F61)
                            isCurfew -> Color(0xFFFEF08A)
                            else -> Color(0xFF242C34)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                isCompleted -> Color(0xFF8B9198)
                                isNextWindow -> Color(0xFF101418)
                                isCurfew -> Color(0xFF101418)
                                else -> Color(0xFFFDBA74)
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(title, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B9198))
            }
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineMedium,
                color = if (isCurfew) Color(0xFFFEF08A) else Color(0xFFFFB4AA),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
