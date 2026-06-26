package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.zoewave.probase.features.health.core.SkinInsight
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherUiState
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.CollectionHubCard
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.HomeHeader
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.HomeHeaderUiState
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.LuxuryBrandLogo
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.RoutineSummaryCard
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.RoutineSummaryUiState
import com.zoewave.probase.kocolor.model.KoColorRoute

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
    navTo: (KoColorRoute) -> Unit
) {
    HomeScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onEvent: (HomeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
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
                title = { LuxuryBrandLogo(uiState = Unit, modifier = Modifier, onEvent = {}, navTo = {}) },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.Settings()) }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.applications_kocolor_apps_mobile_features_home_settings), tint = MaterialTheme.colorScheme.onSurface)
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
                    uiState = HomeHeaderUiState(
                        uiState.fashionProfile,
                        uiState.isDaytime,
                        uiState.beautyTip,
                        uiState.weather,
                        uiState.locationName,
                        uiState.isLocationFallback,
                        uiState.headerBackgroundUrl
                    ),
                    onEvent = {},
                    navTo = navTo
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
                        modifier = Modifier.fillMaxWidth(),
                        onEvent = {},
                        navTo = navTo
                    )
            }

            item {
                QuickActions(
                    uiState = Unit,
                    onEvent = {},
                    navTo = navTo
                )
            }

            item {
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val activeRoutine = when {
                    hour in 5..9 -> uiState.morningRoutine
                    hour in 10..19 -> uiState.mealsRoutine
                    else -> uiState.eveningRoutine
                }
                
                val title = when {
                    hour in 5..9 -> stringResource(R.string.applications_kocolor_apps_mobile_features_home_morning_ritual_default)
                    hour in 10..19 -> stringResource(R.string.applications_kocolor_apps_mobile_features_home_meals_ritual_default)
                    else -> stringResource(R.string.applications_kocolor_apps_mobile_features_home_evening_ritual_default)
                }
                
                if (activeRoutine != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SectionTitle(
                            uiState = SectionTitleUiState(title, stringResource(R.string.applications_kocolor_apps_mobile_features_home_biosynced_ritual)), 
                            onEvent = {}, 
                            navTo = {}
                        )
                        RoutineSummaryCard(
                            uiState = RoutineSummaryUiState(activeRoutine, uiState.isDaytime),
                            onEvent = {},
                            navTo = navTo
                        )
                    }
                }
            }

            if (uiState.totalCosmetics > 0 || uiState.totalClothing > 0) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SectionTitle(
                            uiState = SectionTitleUiState(
                                stringResource(R.string.applications_kocolor_apps_mobile_features_home_the_hub), 
                                stringResource(R.string.applications_kocolor_apps_mobile_features_home_unified_archive)
                            ), 
                            onEvent = {}, 
                            navTo = {}
                        )
                        CollectionHubCard(
                            uiState = uiState, 
                            modifier = Modifier,
                            onEvent = {}, 
                            navTo = navTo
                        )
                    }
                }
            }
            
            item {
                BoutiqueCard(
                    modifier = Modifier.padding(top = 16.dp),
                    onEvent = {},
                    navTo = {}
                )
            }
            
            item { Spacer(modifier = Modifier.height(48.dp)) }
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
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
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
                        BioMarkerItem(uiState = BioMarkerUiState(Icons.Default.AutoAwesome, stringResource(R.string.applications_kocolor_apps_mobile_features_home_vitals), if (uiState.insights.isEmpty()) stringResource(R.string.applications_kocolor_apps_mobile_features_home_optimal) else stringResource(R.string.applications_kocolor_apps_mobile_features_home_alerts_format, uiState.insights.size), if (uiState.insights.isEmpty()) Color(0xFF4CAF50) else Color(0xFFF44336)), modifier = Modifier.weight(1f), onEvent = {}, navTo = {})
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
        Surface(color = uiState.color.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(uiState.icon, null, modifier = Modifier.size(18.dp), tint = uiState.color) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = uiState.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = uiState.value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}

@Composable
fun QuickActions(
    uiState: Unit, 
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit, 
    navTo: (KoColorRoute) -> Unit
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        QuickActionCard(uiState = QuickActionUiState(stringResource(R.string.applications_kocolor_apps_mobile_features_home_analyze_style), stringResource(R.string.applications_kocolor_apps_mobile_features_home_ai_visual_analysis), Icons.Default.AutoAwesome, MaterialTheme.colorScheme.primary, KoColorRoute.StyleSimulator), modifier = Modifier.weight(1f), onEvent = {}, navTo = navTo)
        QuickActionCard(uiState = QuickActionUiState(stringResource(R.string.applications_kocolor_apps_mobile_features_home_capture_product), stringResource(R.string.applications_kocolor_apps_mobile_features_home_gemini_scanner), Icons.Default.CameraAlt, MaterialTheme.colorScheme.secondary, KoColorRoute.Analyzer()), modifier = Modifier.weight(1f), onEvent = {}, navTo = navTo)
    }
}

data class QuickActionUiState(val title: String, val subtitle: String, val icon: ImageVector, val color: Color, val route: KoColorRoute)

@Composable
fun QuickActionCard(
    uiState: QuickActionUiState, 
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit, 
    navTo: (KoColorRoute) -> Unit
) {
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

@Composable
fun SectionTitle(
    uiState: SectionTitleUiState, 
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit, 
    navTo: (KoColorRoute) -> Unit
) {
    Column(modifier = modifier) {
        Text(text = uiState.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        Text(text = uiState.subtitle.uppercase(), style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun BoutiqueCard(
    modifier: Modifier = Modifier, 
    onEvent: (Unit) -> Unit, 
    navTo: (KoColorRoute) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(440.dp)
            .clickable { uriHandler.openUri("https://www.kocolor.com") },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = R.drawable.boutique_bg,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.applications_kocolor_apps_mobile_features_home_art_of_color),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.applications_kocolor_apps_mobile_features_home_boutique_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = stringResource(R.string.applications_kocolor_apps_mobile_features_home_boutique_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.applications_kocolor_apps_mobile_features_home_enter_atelier),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8D6E63)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF8D6E63),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
