package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.features.health.core.SkinInsight
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.model.KoColorRoute

@Preview(showBackground = true)
@Composable
private fun WellnessInsightsSectionPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            WellnessInsightsSection(
                uiState = WellnessInsightsUiState(
                    insights = emptyList(),
                    sleepDuration = "7h 12m",
                    hydrationLiters = 1.2,
                    hydrationGoalLiters = 2.7,
                    isPermissionGranted = true
                ),
                onEvent = {},
                navTo = {}
            )
        }
    }
}

data class WellnessInsightsUiState(
    val insights: List<SkinInsight>,
    val sleepDuration: String?,
    val hydrationLiters: Double,
    val hydrationGoalLiters: Double,
    val isPermissionGranted: Boolean
)

@Composable
fun WellnessInsightsSection(
    uiState: WellnessInsightsUiState,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(uiState = SectionTitleUiState(stringResource(R.string.applications_kocolor_apps_mobile_features_home_bio_markers), stringResource(R.string.applications_kocolor_apps_mobile_features_home_style_inside_out)), onEvent = {}, navTo = {})

        ElevatedCard(
            modifier = Modifier.fillMaxWidth().clickable { navTo(KoColorRoute.Health) },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFF5EFFF), // Soft Lavender for Sleep
                                Color(0xFFE0F2FF), // Soft Sky Blue for Hydration
                                Color(0xFFE6F7ED)  // Soft Mint for Vitals
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                if (!uiState.isPermissionGranted) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = stringResource(R.string.applications_kocolor_apps_mobile_features_home_sync_health), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = stringResource(R.string.applications_kocolor_apps_mobile_features_home_connect_vitals), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        BioMarkerItem(uiState = BioMarkerUiState(Icons.Default.Bedtime, stringResource(R.string.applications_kocolor_apps_mobile_features_home_sleep), uiState.sleepDuration ?: "--", Color(0xFF9C27B0)), modifier = Modifier.weight(1f), onEvent = {}, navTo = {})
                        VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        BioMarkerItem(uiState = BioMarkerUiState(Icons.Default.WaterDrop, stringResource(R.string.applications_kocolor_apps_mobile_features_home_hydration), stringResource(R.string.applications_kocolor_apps_mobile_features_home_hydration_format, uiState.hydrationLiters), Color(0xFF2196F3)), modifier = Modifier.weight(1f), onEvent = {}, navTo = {})
                        VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        BioMarkerItem(uiState = BioMarkerUiState(Icons.Default.Favorite, stringResource(R.string.applications_kocolor_apps_mobile_features_home_vitals), if (uiState.insights.isEmpty()) stringResource(R.string.applications_kocolor_apps_mobile_features_home_optimal) else stringResource(R.string.applications_kocolor_apps_mobile_features_home_alerts_format, uiState.insights.size), if (uiState.insights.isEmpty()) Color(0xFF4CAF50) else Color(0xFFF44336)), modifier = Modifier.weight(1f), onEvent = {}, navTo = {})
                    }
                }
            }
        }
    }
}

data class BioMarkerUiState(val icon: ImageVector, val label: String, val value: String, val color: Color)

@Composable
fun BioMarkerItem(
    uiState: BioMarkerUiState,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .drawBehind {
                    drawCircle(
                        color = uiState.color.copy(alpha = 0.12f),
                        radius = size.maxDimension / 2 + 12f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(48.dp),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = uiState.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = uiState.color
                    )
                }
            }

            // Status Dot
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 2.dp)
                    .size(10.dp)
                    .background(uiState.color, CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = uiState.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        Text(
            text = uiState.value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}