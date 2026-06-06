package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.zoewave.probase.features.health.core.SkinInsight
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherSquareCard
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherUiState
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.CollectionHubCard
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.LuxuryBrandLogo
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.RoutineSummaryCard
import com.zoewave.probase.kocolor.model.FashionProfile
import com.zoewave.probase.kocolor.model.KoColorRoute

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
private fun HomeUiRoutePreview() {
    MaterialTheme {
        HomeUiRoute(
            uiState = HomeUiState(
                weather = LayeredWeatherUiState(temperature = 22.0, uvIndex = 4.0)
            ),
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            onEvent(HomeEvent.RefreshWeather)
        }
    }

    // Explicitly request permissions on first launch if not already granted
    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

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
                    IconButton(onClick = { navTo(KoColorRoute.Settings()) }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.applications_kocolor_apps_mobile_settings), tint = MaterialTheme.colorScheme.onSurface)
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
                    uiState = HomeHeaderUiState(uiState.fashionProfile, uiState.isDaytime, uiState.beautyTip, uiState.weather, uiState.locationName, uiState.headerBackgroundUrl),
                    onEvent = {},
                    navTo = {}
                )
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    WellnessInsightsSection(
                        uiState = WellnessInsightsUiState(
                            uiState.wellnessInsights,
                            uiState.lastNightSleepDuration,
                            uiState.hydrationLiters,
                            uiState.hydrationGoalLiters,
                            uiState.isHealthPermissionGranted
                        ),
                        onEvent = {},
                        navTo = navTo,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                QuickActions(
                    uiState = Unit,
                    onEvent = {},
                    navTo = navTo
                )
            }

            item {
                val routine = if (uiState.isDaytime) uiState.morningRoutine else uiState.eveningRoutine
                val title = if (uiState.isDaytime) stringResource(R.string.applications_kocolor_apps_mobile_morning_ritual) else stringResource(R.string.applications_kocolor_apps_mobile_evening_ritual)
                
                if (routine != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SectionTitle(uiState = SectionTitleUiState(title, stringResource(R.string.applications_kocolor_apps_mobile_biosynced_ritual)), onEvent = {}, navTo = {})
                        RoutineSummaryCard(
                            uiState = routine to uiState.isDaytime,
                            onEvent = {},
                            navTo = navTo
                        )
                    }
                }
            }

            if (uiState.totalCosmetics > 0 || uiState.totalClothing > 0) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SectionTitle(uiState = SectionTitleUiState(stringResource(R.string.applications_kocolor_apps_mobile_the_hub), stringResource(R.string.applications_kocolor_apps_mobile_unified_archive)), onEvent = {}, navTo = {})
                        CollectionHubCard(uiState = uiState, onEvent = {}, navTo = navTo)
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
    val beautyTip: String,
    val weather: LayeredWeatherUiState? = null,
    val locationName: String? = null,
    val backgroundUrl: String? = null
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
    ) {
        // Frosted Glass Background Image
        uiState.backgroundUrl?.let { url ->
            coil.compose.AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.4f)
                    .blur(16.dp), // Frosted glass effect
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Top Section: Header + Weather Card - Now Centered Vertically
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isDaytime) stringResource(R.string.applications_kocolor_apps_mobile_radiant_morning) else stringResource(R.string.applications_kocolor_apps_mobile_deep_restoration),
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                LayeredWeatherSquareCard(
                    uiState = uiState.weather?.copy(locationName = uiState.locationName),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            // Bottom Section: The Phrase
            Row(
                modifier = Modifier.fillMaxWidth().alpha(0.9f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).padding(top = 4.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = uiState.beautyTip,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (uiState.fashionProfile != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                        Text(text = uiState.fashionProfile.seasonalType.name, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = stringResource(R.string.applications_kocolor_apps_mobile_undertone_format, uiState.fashionProfile.undertone.name.lowercase().replaceFirstChar { it.uppercase() }), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(uiState = SectionTitleUiState(stringResource(R.string.applications_kocolor_apps_mobile_bio_markers), stringResource(R.string.applications_kocolor_apps_mobile_style_inside_out)), onEvent = {}, navTo = {})
        
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
                        Text(text = stringResource(R.string.applications_kocolor_apps_mobile_sync_health), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = stringResource(R.string.applications_kocolor_apps_mobile_connect_vitals), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        BioMarkerItem(uiState = BioMarkerUiState(Icons.Default.Bedtime, stringResource(R.string.applications_kocolor_apps_mobile_sleep), uiState.sleepDuration ?: "--", Color(0xFF9C27B0)), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                        VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        BioMarkerItem(uiState = BioMarkerUiState(Icons.Default.WaterDrop, stringResource(R.string.applications_kocolor_apps_mobile_hydration), stringResource(R.string.applications_kocolor_apps_mobile_hydration_format, uiState.hydrationLiters), Color(0xFF2196F3)), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                        VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        BioMarkerItem(uiState = BioMarkerUiState(Icons.Default.AutoAwesome, stringResource(R.string.applications_kocolor_apps_mobile_vitals), if (uiState.insights.isEmpty()) stringResource(R.string.applications_kocolor_apps_mobile_optimal) else "${uiState.insights.size} Alerts", if (uiState.insights.isEmpty()) Color(0xFF4CAF50) else Color(0xFFF44336)), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
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
fun QuickActions(uiState: Unit, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        QuickActionCard(uiState = QuickActionUiState(stringResource(R.string.applications_kocolor_apps_mobile_analyze_style), stringResource(R.string.applications_kocolor_apps_mobile_ai_visual_analysis), Icons.Default.AutoAwesome, MaterialTheme.colorScheme.primary, KoColorRoute.StyleSimulator), onEvent = {}, navTo = navTo, modifier = Modifier.weight(1f))
        QuickActionCard(uiState = QuickActionUiState(stringResource(R.string.applications_kocolor_apps_mobile_capture_product), stringResource(R.string.applications_kocolor_apps_mobile_gemini_scanner), Icons.Default.CameraAlt, MaterialTheme.colorScheme.secondary, KoColorRoute.Analyzer()), onEvent = {}, navTo = navTo, modifier = Modifier.weight(1f))
    }
}

data class QuickActionUiState(val title: String, val subtitle: String, val icon: ImageVector, val color: Color, val route: KoColorRoute)

@Preview(showBackground = true)
@Composable
private fun QuickActionCardPreview() {
    MaterialTheme {
        QuickActionCard(uiState = QuickActionUiState("Title", "Subtitle", Icons.Default.Info, Color.Red, KoColorRoute.Home), onEvent = {}, navTo = {})
    }
}

@Composable
fun QuickActionCard(uiState: QuickActionUiState, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(onClick = { navTo(uiState.route) }, modifier = modifier, shape = RoundedCornerShape(24.dp)) {
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
private fun HomeScreenPreview_Default() {
    MaterialTheme {
        HomeScreen(uiState = HomeUiState(), onEvent = {}, navTo = {})
    }
}
