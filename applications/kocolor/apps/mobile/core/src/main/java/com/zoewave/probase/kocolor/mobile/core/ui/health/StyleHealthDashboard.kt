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
import androidx.compose.ui.res.stringResource
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
import com.zoewave.probase.features.health.hydration.ui.components.HydrationWaterDropUiState
import com.zoewave.probase.kocolor.mobile.core.R
import com.zoewave.probase.kocolor.mobile.core.ui.components.WellnessTrackerHeroCard
import com.zoewave.probase.kocolor.mobile.core.ui.components.WellnessTrackerHeroUiState
import com.zoewave.probase.kocolor.mobile.core.ui.theme.KoColorTheme
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.time.LocalDate

@Composable
fun StyleHealthDashboard(
    uiState: HealthUiState.Success,
    onEvent: (HealthEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val today = LocalDate.now().toString()
    val hydration = uiState.weeklyHydration[today] ?: 0.0
    val hydrationGoal = uiState.hydrationGoal
    val lastSleep = uiState.sleepSessions.firstOrNull()
    var showTracker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(Color(0xFFF9F7F2))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.applications_kocolor_apps_mobile_core_health_title),
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
                text = stringResource(R.string.applications_kocolor_apps_mobile_core_health_bio_markers),
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.applications_kocolor_apps_mobile_core_health_style_inside_out),
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
                        uiState = SummaryMetricUiState(
                            icon = Icons.Rounded.Bedtime,
                            label = stringResource(R.string.applications_kocolor_apps_mobile_core_health_sleep),
                            value = lastSleep?.let { stringResource(R.string.applications_kocolor_apps_mobile_core_health_sleep_duration_format, it.duration?.toHours() ?: 0, it.duration?.toMinutes()?.rem(60) ?: 0) } ?: "--",
                            color = Color(0xFF9C27B0)
                        ),
                        onEvent = {},
                        navTo = {}
                    )
                    VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    SummaryMetricItem(
                        uiState = SummaryMetricUiState(
                            icon = Icons.Rounded.WaterDrop,
                            label = stringResource(R.string.applications_kocolor_apps_mobile_core_health_hydration),
                            value = "%.1fL".format(hydration),
                            color = Color(0xFF2196F3)
                        ),
                        onEvent = {},
                        navTo = {}
                    )
                    VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    SummaryMetricItem(
                        uiState = SummaryMetricUiState(
                            icon = Icons.Rounded.Favorite,
                            label = stringResource(R.string.applications_kocolor_apps_mobile_core_health_vitals),
                            value = if (uiState.latestHeartRate != null) stringResource(R.string.applications_kocolor_apps_mobile_core_health_vitals_normal) else stringResource(R.string.applications_kocolor_apps_mobile_core_health_vitals_syncing),
                            color = if (uiState.latestHeartRate != null) Color(0xFF4CAF50) else Color.Gray
                        ),
                        onEvent = {},
                        navTo = {}
                    )
                }
            }
        }

        HydrationWaterDropCard(
            uiState = HydrationWaterDropUiState(hydration, hydrationGoal),
            onEvent = { navTo(KoColorRoute.Hydration) },
            navTo = {}
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.applications_kocolor_apps_mobile_core_health_activity),
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            ActivityMetricsSection(
                uiState = ActivityMetricsUiState(uiState.todaySteps, uiState.todayCalories),
                onEvent = {},
                navTo = navTo
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
                    text = stringResource(R.string.applications_kocolor_apps_mobile_core_health_element_tracker),
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
                    uiState = WellnessTrackerHeroUiState(
                        connectionState = uiState.bleConnectionState,
                        metrics = uiState.trackerMetrics,
                        modifier = Modifier.clickable {
                            onEvent(HealthEvent.SyncTracker)
                        }
                    ),
                    onEvent = {},
                    navTo = {}
                )
            }
        }
        
        Spacer(Modifier.height(48.dp))
    }
}

data class SummaryMetricUiState(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val color: Color
)

@Composable
private fun SummaryMetricItem(
    uiState: SummaryMetricUiState,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(color = uiState.color.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(uiState.icon, null, modifier = Modifier.size(18.dp), tint = uiState.color) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = uiState.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = uiState.value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}

data class ActivityMetricsUiState(
    val steps: Long,
    val calories: Double
)

@Composable
private fun ActivityMetricsSection(
    uiState: ActivityMetricsUiState,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ActivityCard(
            uiState = ActivityCardUiState(
                label = stringResource(R.string.applications_kocolor_apps_mobile_core_health_steps),
                value = "${uiState.steps}",
                unit = stringResource(R.string.applications_kocolor_apps_mobile_core_health_today),
                modifier = Modifier.weight(1f)
            ),
            onEvent = {},
            navTo = {}
        )
        ActivityCard(
            uiState = ActivityCardUiState(
                label = stringResource(R.string.applications_kocolor_apps_mobile_core_health_calories),
                value = "${uiState.calories.toInt()}",
                unit = stringResource(R.string.applications_kocolor_apps_mobile_core_health_kcal),
                modifier = Modifier.weight(1f)
            ),
            onEvent = { navTo(KoColorRoute.Nutrition()) },
            navTo = navTo
        )
    }
}

data class ActivityCardUiState(
    val label: String,
    val value: String,
    val unit: String,
    val modifier: Modifier = Modifier
)

@Composable
private fun ActivityCard(
    uiState: ActivityCardUiState,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val hasData = uiState.value != "0" && uiState.value != "0.0" && uiState.value != "--"
    Card(
        modifier = uiState.modifier
            .aspectRatio(1f)
            .alpha(if (hasData) 1f else 0.7f)
            .clickable { onEvent() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(uiState.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    color = if (hasData) Color(0xFF2C2420) else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
                Text(
                    text = if (hasData) uiState.unit.lowercase() else stringResource(R.string.applications_kocolor_apps_mobile_core_health_no_data),
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
