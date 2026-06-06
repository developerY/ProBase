package com.zoewave.probase.kocolor.mobile.core.ui.health

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.features.health.core.ui.HealthEvent
import com.zoewave.probase.features.health.core.ui.HealthUiState
import com.zoewave.probase.features.health.hydration.ui.components.HydrationWaterDropCard
import com.zoewave.probase.kocolor.mobile.core.ui.components.WellnessTrackerHeroCard
import com.zoewave.probase.kocolor.mobile.core.ui.theme.KoColorTheme
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.time.LocalDate

@Composable
fun StyleHealthDashboard(
    uiState: HealthUiState.Success,
    onEvent: (HealthEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now().toString()
    val hydration = uiState.weeklyHydration[today] ?: 0.0
    val hydrationGoal = uiState.hydrationGoal
    val lastSleep = uiState.sleepSessions.firstOrNull()
    var showTracker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(Color(0xFFF9F7F2))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 0. Centered Main Header
        Text(
            text = "Health & Wellness",
            style = MaterialTheme.typography.displaySmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2420),
            textAlign = TextAlign.Center
        )

        // 1. Bio-Markers Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Bio-Markers",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "BEAUTY FROM THE INSIDE OUT",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    icon = Icons.Rounded.Bedtime,
                    label = "Sleep",
                    value = lastSleep?.let { "${it.duration?.toHours()}h ${it.duration?.toMinutes()?.rem(60)}m" } ?: "--",
                    detail = "Restorative",
                    iconColor = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    icon = Icons.Rounded.WaterDrop,
                    label = "Hydration",
                    value = "%.1fL".format(hydration),
                    detail = "Target: %.1fL".format(hydrationGoal),
                    iconColor = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    icon = Icons.Rounded.Favorite,
                    label = "Vitals",
                    value = if (uiState.latestHeartRate != null) "Normal" else "Syncing...",
                    detail = uiState.latestHeartRate?.let { "$it bpm" } ?: "--- bpm",
                    iconColor = if (uiState.latestHeartRate != null) Color(0xFF4CAF50) else Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2. Active Alerts Section
        if (uiState.alerts.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Active Alerts",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
                AlertsSectionRefined(alerts = uiState.alerts)
            }
        }

        // 4. Hydration Section with Water Drop - Clicking navigates to detail page
        HydrationWaterDropCard(
            currentLiters = hydration,
            targetLiters = hydrationGoal,
            onClick = { navTo(KoColorRoute.Hydration) }
        )

        // 4. Activity Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Activity",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            ActivityMetricsSectionRefined(
                steps = uiState.todaySteps,
                calories = uiState.todayCalories
            )
        }

        // 5. Element Tracker Section
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTracker = !showTracker },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ELEMENT TRACKER",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
                Icon(
                    imageVector = if (showTracker) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = showTracker,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                WellnessTrackerHeroCard(
                    connectionState = uiState.bleConnectionState,
                    metrics = uiState.trackerMetrics,
                    modifier = Modifier.clickable {
                        onEvent(HealthEvent.SyncTracker)
                    }
                )
            }
        }
        
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    detail: String,
    iconColor: Color = Color.Black
) {
    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(detail, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.Gray), maxLines = 1)
        }
    }
}

@Composable
private fun AlertsSectionRefined(alerts: List<com.zoewave.probase.features.health.core.SkinInsight>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        alerts.forEach { insight ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    // Left Accent Border
                    Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(Color(0xFFD32F2F)))
                    
                    Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.Top) {
                        Surface(
                            color = Color(0xFFD32F2F).copy(alpha = 0.05f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(insight.trigger, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                            HorizontalDivider(modifier = Modifier.alpha(0.1f))
                            Text("BEAUTY IMPACT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                            Text(insight.manifestation, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                            Text(insight.recommendation, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, lineHeight = 22.sp)
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ActivityMetricsSectionRefined(steps: Long, calories: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ActivityCardRefined(
            icon = Icons.AutoMirrored.Rounded.DirectionsWalk,
            label = "Steps",
            value = "$steps",
            unit = "Today",
            modifier = Modifier.weight(1f)
        )
        ActivityCardRefined(
            icon = Icons.Rounded.LocalFireDepartment,
            label = "Calories",
            value = "${calories.toInt()}",
            unit = "kcal",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActivityCardRefined(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Spacer(Modifier.height(12.dp))
            
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(56.dp).alpha(0.05f), 
                    tint = Color.Black
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF2C2420),
                        maxLines = 1,
                        overflow = TextOverflow.Visible
                    )
                    Text(
                        text = unit.lowercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StyleHealthDashboardPreview() {
    KoColorTheme {
        StyleHealthDashboard(
            uiState = HealthUiState.Success(
                sessions = emptyList(),
                weeklyHydration = mapOf(LocalDate.now().toString() to 1.5),
                sleepSessions = emptyList(),
                alerts = listOf(
                    com.zoewave.probase.features.health.core.SkinInsight(
                        trigger = "Sleep Deprivation",
                        manifestation = "Puffiness & Dark Circles",
                        recommendation = "Use a caffeine-based eye serum and increase water intake to reduce visible signs of fatigue.",
                        severity = 0.8f
                    )
                ),
                latestHeartRate = 93,
                todaySteps = 0,
                todayCalories = 1730.0,
                hydrationGoal = 2.7
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
