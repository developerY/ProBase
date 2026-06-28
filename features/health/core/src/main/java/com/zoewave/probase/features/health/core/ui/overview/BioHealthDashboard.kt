package com.zoewave.probase.features.health.core.ui.overview

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import com.zoewave.probase.features.health.core.SkinInsight
import com.zoewave.probase.features.health.core.ui.HealthEvent
import com.zoewave.probase.features.health.core.ui.HealthUiState
import com.zoewave.probase.features.health.core.ui.components.BioElementTrackerCard
import com.zoewave.probase.features.health.core.ui.components.BioElementTrackerUiState
import com.zoewave.probase.features.health.hydration.ui.components.HydrationWaterDropCard
import com.zoewave.probase.features.health.hydration.ui.components.HydrationWaterDropUiState
import java.time.LocalDate

@Composable
fun BioHealthDashboard(
    uiState: HealthUiState.Success,
    onEvent: (HealthEvent) -> Unit,
    onNavigateToHydration: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    modifier: Modifier = Modifier,
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
        Text(
            text = "Biological Health",
            style = MaterialTheme.typography.displaySmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2420),
            textAlign = TextAlign.Center
        )

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

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryMetricItem(
                        icon = Icons.Rounded.Bedtime,
                        label = "Sleep",
                        value = lastSleep?.let { "${it.duration?.toHours()}h ${it.duration?.toMinutes()?.rem(60)}m" } ?: "--",
                        color = Color(0xFF9C27B0)
                    )
                    VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    SummaryMetricItem(
                        icon = Icons.Rounded.WaterDrop,
                        label = "Hydration",
                        value = "%.1fL".format(hydration),
                        color = Color(0xFF2196F3)
                    )
                    VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    SummaryMetricItem(
                        icon = Icons.Rounded.Favorite,
                        label = "Vitals",
                        value = if (uiState.alerts.isEmpty()) "Optimal" else "${uiState.alerts.size} Alerts",
                        color = if (uiState.alerts.isEmpty()) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }
        }

        if (uiState.alerts.isNotEmpty()) {
            SkinInsightsSection(uiState.alerts)
        }

        HydrationWaterDropCard(
            uiState = HydrationWaterDropUiState(hydration, hydrationGoal),
            onEvent = { onNavigateToHydration() },
            navTo = {}
        )

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
            ActivityMetricsSection(
                steps = uiState.todaySteps,
                calories = uiState.todayCalories,
                onNutritionClick = onNavigateToNutrition
            )
        }

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
                BioElementTrackerCard(
                    uiState = BioElementTrackerUiState(
                        connectionState = uiState.bleConnectionState,
                        metrics = uiState.trackerMetrics
                    ),
                    modifier = Modifier.clickable {
                        // TODO: Setup tracker
                        // onEvent(HealthEvent.SyncTracker)
                    }
                )
            }
        }
        
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun SkinInsightsSection(insights: List<SkinInsight>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Insights",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
        )

        insights.forEach { insight ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        color = if (insight.severity > 0.7f) Color(0xFFF44336).copy(alpha = 0.1f) else Color(0xFFFF9800).copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Favorite,
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = if (insight.severity > 0.7f) Color(0xFFF44336) else Color(0xFFFF9800)
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = insight.trigger,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C2420)
                        )
                        Text(
                            text = insight.manifestation,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Color(0xFFF9F7F2),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "✨ " + insight.recommendation,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp),
                                color = Color(0xFF4A4444)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(color = color.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(18.dp), tint = color) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}

@Composable
private fun ActivityMetricsSection(
    steps: Long,
    calories: Double,
    onNutritionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ActivityCard(
            label = "Steps",
            value = "$steps",
            unit = "today",
            modifier = Modifier.weight(1f)
        )
        ActivityCard(
            label = "Calories",
            value = "${calories.toInt()}",
            unit = "kcal",
            modifier = Modifier.weight(1f),
            onClick = onNutritionClick
        )
    }
}

@Composable
private fun ActivityCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val hasData = value != "0" && value != "0.0" && value != "--"
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .alpha(if (hasData) 1f else 0.7f)
            .let { if (onClick != null) it.clickable { onClick() } else it },
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    color = if (hasData) Color(0xFF2C2420) else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
                Text(
                    text = if (hasData) unit.lowercase() else "no data",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BioHealthDashboardPreview() {
    MaterialTheme {
        BioHealthDashboard(
            uiState = HealthUiState.Success(
                sessions = emptyList(),
                weeklyHydration = mapOf(LocalDate.now().toString() to 1.5),
                sleepSessions = emptyList(),
                alerts = listOf(
                    SkinInsight(
                        trigger = "Sleep Deprivation",
                        manifestation = "Puffiness \u0026 Dark Circles",
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
            onNavigateToHydration = {},
            onNavigateToNutrition = {}
        )
    }
}
