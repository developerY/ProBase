package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherUiState
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.BoutiqueCard
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.CollectionHubCard
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.HomeHeader
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.HomeHeaderUiState
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.LuxuryBrandLogo
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.QuickActions
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.RoutineSummaryCard
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.RoutineSummaryUiState
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.SectionTitle
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.SectionTitleUiState
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.WellnessInsightsSection
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.WellnessInsightsUiState
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
                        uiState.headerBackgroundUrl,
                        uiState.temperatureUnit
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
                            uiState = RoutineSummaryUiState(activeRoutine, uiState.isDaytime, title),
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
                QuickActions(
                    uiState = Unit,
                    onEvent = {},
                    navTo = navTo
                )
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

