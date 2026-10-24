package com.zoewave.probase.goswift.mobile.shots.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ShotsUiRoute(
    viewModel: ShotsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    ShotsUiScreen(
        uiState = uiState,
        onLogMicrodose = {
            viewModel.onEvent(ShotsUiEvent.LogShot(15))
            Toast.makeText(context, "Logged 15mg Fuel Microdose", Toast.LENGTH_SHORT).show()
        }
    )
}

@Composable
fun ShotsUiScreen(
    uiState: ShotsUiState = ShotsUiState.Loading,
    onLogMicrodose: () -> Unit = {}
) {
    if (uiState is ShotsUiState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101418)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFFF6F61))
        }
        return
    }

    val success = uiState as ShotsUiState.Success
    var searchQuery by remember { mutableStateOf("") }
    var selectedTimeFilter by remember { mutableStateOf("Day") }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF101418))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hummingbird Fuel",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFE1E2E9),
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = Color(0xFFFF6F61).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "SCHEDULE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF6F61),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFFFF6F61), CircleShape))
                            Text("On-Device AI Active • Health Connect Synced", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198), fontSize = 11.sp)
                        }
                        Text("⚡ 0m ago", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFEF08A), fontSize = 11.sp)
                    }
                }
                Box(contentAlignment = Alignment.TopEnd) {
                    Surface(
                        color = Color(0xFF242C34),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color(0xFFE1E2E9))
                        }
                    }
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF6F61), CircleShape).border(1.dp, Color(0xFF101418), CircleShape))
                }
            }

            // 2. Pharmacokinetics Engine Card
            Surface(
                color = Color(0xFF1C2228),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF242C34)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFFDBA74), modifier = Modifier.size(16.dp))
                            Text("PHARMACOKINETICS ENGINE", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFDBA74), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            color = Color(0xFF242C34),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Active Microdose #${success.activeMicrodoseNumber}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFB4AA), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TOTAL TELEMETRY", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198), fontFamily = FontFamily.Monospace)
                            Text("%,d doses".format(success.totalTelemetryDoses), style = MaterialTheme.typography.headlineLarge, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("✓ ${success.fidelityPercent}% zero-jitter fidelity", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("DAILY VELOCITY", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198), fontFamily = FontFamily.Monospace)
                            Text("${success.dailyVelocityMg} mg / day", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("${success.avgPulses} fuel pulses avg", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFDBA74))
                        }
                    }

                    // Pharmacokinetics Timeline Chart
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🌅 Morning Peak", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFB4AA))
                            Text("☀️ Mid-Day Maintenance", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFDBA74))
                            Text("🌙 17:00 Curfew", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                        }

                        // Bar Chart Canvas
                        Row(
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val barHeights = listOf(0.2f, 0.3f, 0.7f, 1.0f, 0.8f, 0.6f, 0.9f, 0.75f, 0.5f, 0.2f, 0.1f, 0.1f)
                            barHeights.forEachIndexed { idx, height ->
                                val barColor = when {
                                    idx in 2..4 -> Color(0xFFFF6F61)
                                    idx in 5..7 -> Color(0xFFFDBA74)
                                    else -> Color(0xFF242C34)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(height)
                                        .background(barColor, RoundedCornerShape(3.dp))
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("06:00", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("12:00", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("18:00", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("24:00", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }
                    }
                }
            }

            // 3. Search Bar
            Surface(
                color = Color(0xFF1C2228),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF242C34)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8B9198), modifier = Modifier.size(18.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFE1E2E9)),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search logs by dose, tag, or title...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF8B9198)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            // 4. Time Filter Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Day", "Week", "Month", "All Time").forEach { timeRange ->
                    val isSelected = timeRange == selectedTimeFilter
                    Surface(
                        color = if (isSelected) Color(0xFFFF6F61) else Color(0xFF1C2228),
                        shape = RoundedCornerShape(12.dp),
                        border = if (!isSelected) BorderStroke(1.dp, Color(0xFF242C34)) else null,
                        modifier = Modifier.weight(1f).clickable { selectedTimeFilter = timeRange }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = timeRange,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color(0xFF101418) else Color(0xFFE1E2E9),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (timeRange == "All Time") {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = Color(0xFF242C34),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("10.4k", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFEF08A), fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Category Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFF1C2228),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF6F61))
                ) {
                    Text("• All Doses", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF6F61), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
                Surface(
                    color = Color(0xFF1C2228),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFF242C34))
                ) {
                    Text("Fuel Drops (15-25mg)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }

            // 5. Pharmacokinetic Bloodstream Status Strip
            Surface(
                color = Color(0xFF161B20),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF242C34)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Science, contentDescription = null, tint = Color(0xFFFFB4AA), modifier = Modifier.size(20.dp))
                        Column {
                            Text("Pharmacokinetic Bloodstream", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFB4AA), fontWeight = FontWeight.Bold)
                            Text("Remaining in bloodstream: ${success.remainingInBloodstreamMg}mg", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC1C7CE))
                        }
                    }
                    Surface(
                        color = Color(0xFF242C34),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFFE1E2E9), modifier = Modifier.size(12.dp))
                            Text("Export", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE1E2E9))
                        }
                    }
                }
            }

            // 6. Today's Dose Group
            val filteredItems = remember(success.todayGroup.items, searchQuery, selectedTimeFilter) {
                success.todayGroup.items.filter { dose ->
                    searchQuery.isEmpty() ||
                        dose.title.contains(searchQuery, ignoreCase = true) ||
                        dose.description.contains(searchQuery, ignoreCase = true) ||
                        dose.doseType.contains(searchQuery, ignoreCase = true) ||
                        dose.doseCategory.contains(searchQuery, ignoreCase = true) ||
                        dose.tags.any { it.contains(searchQuery, ignoreCase = true) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Today Group Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF6F61), CircleShape))
                        Text(success.todayGroup.dateLabel, style = MaterialTheme.typography.titleMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                        Surface(
                            color = Color(0xFF693C00),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${filteredItems.size} Doses • ${filteredItems.sumOf { it.amountMg }}mg",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFDCBE),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text(success.todayGroup.rangeStatus, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                }

                // Dose Cards for Today
                if (filteredItems.isEmpty()) {
                    Text(
                        text = "No microdoses found matching filter",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8B9198),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    filteredItems.forEach { dose ->
                        DoseCard(dose = dose)
                    }
                }
            }

            // 7. Yesterday Summary Card
            Surface(
                color = Color(0xFF1C2228),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF242C34)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF8B9198), modifier = Modifier.size(20.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Yesterday, Oct 23", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                                Surface(
                                    color = Color(0xFF242C34),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("${success.yesterdayCount} Doses • ${success.yesterdayTotalMg}mg", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFEF08A), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                            }
                            Text(success.yesterdaySummaryText, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                        }
                    }
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF8B9198))
                }
            }

            // 8. Monthly Ingestion Summary Card
            Surface(
                color = Color(0xFF1C2228),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF242C34)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFFFDBA74), modifier = Modifier.size(18.dp))
                            Text("October 2024 (M-T-D)", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                        }
                        Text("${success.monthlyTotalDoses} Total Doses", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFDBA74), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Mass", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                            Text("${success.monthlyTotalMassGrams}g", style = MaterialTheme.typography.titleMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Curfew Score", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                            Text("${success.monthlyCurfewScorePercent}%", style = MaterialTheme.typography.titleMedium, color = Color(0xFFFEF08A), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Avg Interval", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                            Text("${success.monthlyAvgIntervalHours}h", style = MaterialTheme.typography.titleMedium, color = Color(0xFFFFB4AA), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242C34), contentColor = Color(0xFFE1E2E9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("View Complete ${success.monthlyTotalDoses} October Ingestions", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 9. Historical Multi-Year Archives Card
            Surface(
                color = Color(0xFF1C2228),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF242C34)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HISTORICAL MULTI-YEAR ARCHIVES", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFDBA74), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF8B9198), modifier = Modifier.size(16.dp))
                    }
                    Text("Scrub 10,340 indexed entries across 2023 - 2024", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B9198))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("SEP '24\n312 doses", "AUG '24\n298 doses", "JUL '24\n340 doses").forEach { monthLabel ->
                            Surface(
                                color = Color(0xFF242C34),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(monthLabel, style = MaterialTheme.typography.labelSmall, color = Color(0xFFE1E2E9), fontSize = 10.sp, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp))
                            }
                        }
                        Surface(
                            color = Color(0xFF242C34),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Jump Date", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFEF08A), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // 10. Dispenser Telemetry Synced Card
            Surface(
                color = Color(0xFF161B20),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF242C34)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Science, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(22.dp))
                        Column {
                            Text("Dispenser Telemetry Synced", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                            Text("Smart Flask #HF-9402 • Bluetooth LE 5.3", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(14.dp))
                        Text("100% Validated", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        // Floating Action Button (+ Log Microdose)
        Surface(
            color = Color(0xFFFF6F61),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
                .clickable { onLogMicrodose() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF101418), modifier = Modifier.size(20.dp))
                Text("Log Microdose", style = MaterialTheme.typography.labelLarge, color = Color(0xFF101418), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DoseCard(dose: DoseCardItem) {
    val accentColor = Color(dose.accentColorHex)
    val iconVector = when (dose.iconType) {
        "coffee" -> Icons.Default.LocalCafe
        "sun" -> Icons.Default.WbSunny
        else -> Icons.Default.Bolt
    }

    Surface(
        color = Color(0xFF1C2228),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF242C34)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
            // Left vertical accent stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            )

            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = Color(0xFF242C34),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(iconVector, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Microdose #${dose.doseNumber}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFE1E2E9),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "• ${dose.time}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF8B9198),
                                    maxLines = 1
                                )
                                Surface(
                                    color = Color(0xFF242C34),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = dose.doseType,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accentColor,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${dose.amountMg}mg",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFFFFB4AA),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = dose.doseCategory,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8B9198),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Text(
                    text = dose.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE1E2E9),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dose.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8B9198)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dose.tags.forEach { tag ->
                        Surface(
                            color = Color(0xFF161B20),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF242C34))
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE1E2E9),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
