package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.health.core.SkinInsight
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.*
import com.zoewave.probase.kocolor.model.*

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
private fun HomeUiRoutePreview() {
    MaterialTheme {
        HomeUiRoute(
            uiState = HomeUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
fun HomeUiRoute(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    HomeScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (uiState.isDaytime) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 1500),
        label = "ChronobiologicalBackground"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { LuxuryBrandLogo(uiState = Unit, onEvent = {}, navTo = {}) },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = backgroundColor,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                HomeHeader(
                    uiState = HomeHeaderUiState(uiState.fashionProfile, uiState.isDaytime, uiState.beautyTip),
                    onEvent = {},
                    navTo = {}
                )
            }

            item {
                WellnessInsightsSection(
                    uiState = WellnessInsightsUiState(
                        uiState.wellnessInsights,
                        uiState.lastNightSleepDuration,
                        uiState.hydrationLiters,
                        uiState.hydrationGoalLiters,
                        uiState.isHealthPermissionGranted
                    ),
                    onEvent = {},
                    navTo = navTo
                )
            }

            item {
                QuickActions(uiState = Unit, onEvent = {}, navTo = navTo)
            }

            item {
                val routine = if (uiState.isDaytime) uiState.morningRoutine else uiState.eveningRoutine
                val title = if (uiState.isDaytime) "Morning Ritual" else "Evening Ritual"
                
                if (routine != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SectionTitle(uiState = SectionTitleUiState(title, "Your bio-synced ritual"), onEvent = {}, navTo = {})
                        RoutineSummaryCard(
                            uiState = routine to uiState.isDaytime,
                            onEvent = {},
                            navTo = navTo
                        )
                    }
                }
            }

            if (uiState.totalCosmetics > 0) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SectionTitle(uiState = SectionTitleUiState("The Vanity", "${uiState.totalCosmetics} items tracked"), onEvent = {}, navTo = {})
                        InventoryDashboard(uiState = uiState, onEvent = {}, navTo = navTo)
                    }
                }
            }

            if (uiState.totalClothing > 0) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SectionTitle(uiState = SectionTitleUiState("The Wardrobe", "${uiState.totalClothing} pieces curated"), onEvent = {}, navTo = {})
                        WardrobeDashboard(uiState = uiState, onEvent = {}, navTo = navTo)
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }
}

data class HomeHeaderUiState(
    val fashionProfile: FashionProfile?,
    val isDaytime: Boolean,
    val beautyTip: String
)

@Preview(showBackground = true)
@Composable
private fun HomeHeaderPreview() {
    MaterialTheme {
        HomeHeader(uiState = HomeHeaderUiState(null, true, "Stay Radiant!"), onEvent = {}, navTo = {})
    }
}

@Composable
fun HomeHeader(
    uiState: HomeHeaderUiState,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val gradientColors = if (uiState.isDaytime) {
        listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface)
    } else {
        listOf(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.surfaceVariant)
    }

    val expressiveShape = RoundedCornerShape(topStart = 48.dp, topEnd = 12.dp, bottomEnd = 48.dp, bottomStart = 12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(expressiveShape)
            .background(Brush.linearGradient(colors = gradientColors))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), expressiveShape)
            .padding(32.dp)
    ) {
        Column {
            Text(
                text = if (uiState.isDaytime) "Radiant Morning." else "Deep Restoration.",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alpha(0.8f)) {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = uiState.beautyTip, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }

            if (uiState.fashionProfile != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                        Text(text = uiState.fashionProfile.seasonalType.name, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "· ${uiState.fashionProfile.undertone.name.lowercase().replaceFirstChar { it.uppercase() }} Undertone", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
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

@Preview(showBackground = true)
@Composable
private fun WellnessInsightsSectionPreview() {
    MaterialTheme {
        WellnessInsightsSection(
            uiState = WellnessInsightsUiState(emptyList(), "8h", 1.5, 2.0, true),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
fun WellnessInsightsSection(
    uiState: WellnessInsightsUiState,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(uiState = SectionTitleUiState("Bio-Markers", "Style from the inside out"), onEvent = {}, navTo = {})
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().clickable { navTo(KoColorRoute.Health) },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                if (!uiState.isPermissionGranted) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Sync Health Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Tap to connect your style vitals.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        BioMarkerItem(uiState = BioMarkerUiState(Icons.Default.Bedtime, "Sleep", uiState.sleepDuration ?: "--", Color(0xFF9C27B0)), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                        VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        BioMarkerItem(uiState = BioMarkerUiState(Icons.Default.WaterDrop, "Hydration", "%.1fL".format(uiState.hydrationLiters), Color(0xFF2196F3)), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                        VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        BioMarkerItem(uiState = BioMarkerUiState(Icons.Default.AutoAwesome, "Vitals", if (uiState.insights.isEmpty()) "Optimal" else "${uiState.insights.size} Alerts", if (uiState.insights.isEmpty()) Color(0xFF4CAF50) else Color(0xFFF44336)), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

data class BioMarkerUiState(val icon: ImageVector, val label: String, val value: String, val color: Color)

@Preview(showBackground = true)
@Composable
private fun BioMarkerItemPreview() {
    MaterialTheme {
        BioMarkerItem(uiState = BioMarkerUiState(Icons.Default.Info, "Label", "Value", Color.Red), onEvent = {}, navTo = {})
    }
}

@Composable
fun BioMarkerItem(uiState: BioMarkerUiState, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(color = uiState.color.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(uiState.icon, null, modifier = Modifier.size(18.dp), tint = uiState.color) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = uiState.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = uiState.value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}

@Preview(showBackground = true)
@Composable
private fun QuickActionsPreview() {
    MaterialTheme {
        QuickActions(uiState = Unit, onEvent = {}, navTo = {})
    }
}

@Composable
private fun QuickActions(uiState: Unit, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        QuickActionCard(uiState = QuickActionUiState("Analyze Style", "AI Visual Analysis", Icons.Default.AutoAwesome, MaterialTheme.colorScheme.primary), onEvent = { navTo(KoColorRoute.StyleSimulator) }, navTo = navTo, modifier = Modifier.weight(1f))
        QuickActionCard(uiState = QuickActionUiState("Capture Product", "Gemini Scanner", Icons.Default.CameraAlt, MaterialTheme.colorScheme.secondary), onEvent = { navTo(KoColorRoute.Analyzer()) }, navTo = navTo, modifier = Modifier.weight(1f))
    }
}

data class QuickActionUiState(val title: String, val subtitle: String, val icon: ImageVector, val color: Color)

@Preview(showBackground = true)
@Composable
private fun QuickActionCardPreview() {
    MaterialTheme {
        QuickActionCard(uiState = QuickActionUiState("Title", "Subtitle", Icons.Default.Info, Color.Red), onEvent = {}, navTo = {})
    }
}

@Composable
private fun QuickActionCard(uiState: QuickActionUiState, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(onClick = { onEvent(Unit) }, modifier = modifier, shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(uiState.icon, null, tint = uiState.color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text(text = uiState.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = uiState.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

data class SectionTitleUiState(val title: String, val subtitle: String)

@Preview(showBackground = true)
@Composable
private fun SectionTitlePreview() {
    MaterialTheme {
        SectionTitle(uiState = SectionTitleUiState("Title", "Subtitle"), onEvent = {}, navTo = {})
    }
}

@Composable
fun SectionTitle(uiState: SectionTitleUiState, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit) {
    Column {
        Text(text = uiState.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        Text(text = uiState.subtitle.uppercase(), style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(uiState = HomeUiState(), onEvent = {}, navTo = {})
}
