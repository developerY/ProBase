package com.zoewave.probase.features.health.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.features.health.core.R
import com.zoewave.probase.features.health.core.SkinInsight

data class BioMarkersUiState(
    val insights: List<SkinInsight> = emptyList(),
    val sleepDuration: String? = null,
    val hydrationLiters: Double = 0.0,
    val hydrationGoalLiters: Double = 2.0,
    val isPermissionGranted: Boolean = true
)

@Composable
fun BioMarkersCard(
    uiState: BioMarkersUiState,
    onClick: () -> Unit,
    onGrantPermissionsClick: () -> Unit,
    onHydrationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Shared Section Header
        HealthSectionTitle(
            uiState = HealthSectionTitleUiState(
                stringResource(R.string.features_health_core_bio_markers_title),
                stringResource(R.string.features_health_core_bio_markers_subtitle)
            )
        )

        // Glassmorphic Card Container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clickable {
                    if (uiState.isPermissionGranted) onClick() else onGrantPermissionsClick()
                },
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFFEBF7F2) // Soft pastel mint/sky
        ) {
            Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                // Background Gradient Wash
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFF5EFFF), // Soft Lavender
                                    Color(0xFFE0F2FF), // Soft Sky Blue
                                    Color(0xFFE6F7ED)  // Soft Mint
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TOP HALF: Glassmorphic Central Pill + Hydration Icon Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Translucent Frosted Glass Pill
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
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.features_health_core_bio_markers_title),
                                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Floating Hydration Icon Badge (Intersecting top-right of pill, matching Weather/Routine badges)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = (-12).dp)
                                .size(52.dp),
                            shape = CircleShape,
                            color = Color(0xFFEFE8E1).copy(alpha = 0.95f),
                            border = BorderStroke(1.dp, Color.White),
                            shadowElevation = 4.dp,
                            onClick = onHydrationClick
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color(0xFFC6B492).copy(alpha = 0.4f), CircleShape)
                                )
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = "Hydration Tracking",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // BOTTOM HALF: Subtitle Row with Chevron (overlapping pill bottom edge)
                    val chevronRotation by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        label = "ChevronRotation"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                            .offset(y = (-14).dp)
                            .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.features_health_core_bio_markers_subtitle),
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

                    // Collapsible Content (3 Bio-Marker Pillars or Permission Lock)
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                        ) {
                            if (!uiState.isPermissionGranted) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = stringResource(R.string.features_health_core_sync_health_data),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.features_health_core_connect_vitals_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BioMarkerItem(
                                        uiState = BioMarkerUiState(
                                            Icons.Default.Bedtime,
                                            "Sleep",
                                            uiState.sleepDuration ?: "8h 0m",
                                            Color(0xFF9C27B0)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    VerticalDivider(
                                        modifier = Modifier.height(48.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                    BioMarkerItem(
                                        uiState = BioMarkerUiState(
                                            Icons.Default.WaterDrop,
                                            "Hydration",
                                            "%.1fL".format(uiState.hydrationLiters),
                                            Color(0xFF2196F3)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    VerticalDivider(
                                        modifier = Modifier.height(48.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                    BioMarkerItem(
                                        uiState = BioMarkerUiState(
                                            Icons.Default.Favorite,
                                            "Vitals",
                                            if (uiState.insights.isEmpty()) "Optimal" else "${uiState.insights.size} Alerts",
                                            if (uiState.insights.isEmpty()) Color(0xFF4CAF50) else Color(0xFFF44336)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class BioMarkerUiState(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val color: Color
)

@Composable
private fun BioMarkerItem(
    uiState: BioMarkerUiState,
    modifier: Modifier = Modifier
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
            color = Color.Black.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        Text(
            text = uiState.value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BioMarkersCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            BioMarkersCard(
                uiState = BioMarkersUiState(
                    insights = emptyList(),
                    sleepDuration = "7h 12m",
                    hydrationLiters = 1.2,
                    hydrationGoalLiters = 2.7,
                    isPermissionGranted = true
                ),
                onClick = {},
                onGrantPermissionsClick = {}
            )
        }
    }
}
