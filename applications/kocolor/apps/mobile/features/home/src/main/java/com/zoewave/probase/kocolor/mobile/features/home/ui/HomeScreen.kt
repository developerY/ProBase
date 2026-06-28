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
import com.zoewave.probase.features.health.core.ui.components.BioMarkersCard
import com.zoewave.probase.features.health.core.ui.components.BioMarkersUiState
import com.zoewave.probase.features.health.core.ui.components.BioRoutineSummaryCard
import com.zoewave.probase.features.health.core.ui.components.BioRoutineSummaryUiState
import com.zoewave.probase.features.weather.ui.components.layered.AtmosphericHeaderCard
import com.zoewave.probase.features.weather.ui.components.layered.AtmosphericHeaderUiState
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherUiState
import com.zoewave.probase.kocolor.features.store.ui.StoreEvent
import com.zoewave.probase.kocolor.features.store.ui.components.BioStoreCard
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.CollectionHubCard
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.LuxuryBrandLogo
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.QuickActions
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.SectionTitle
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.SectionTitleUiState
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.features.routines.R as RoutinesR

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
                AtmosphericHeaderCard(
                    uiState = AtmosphericHeaderUiState(
                        fashionProfile = uiState.fashionProfile,
                        isDaytime = uiState.isDaytime,
                        tip = uiState.beautyTip,
                        weather = uiState.weather,
                        locationName = uiState.locationName,
                        isLocationFallback = uiState.isLocationFallback,
                        backgroundUrl = uiState.headerBackgroundUrl,
                        tempUnit = uiState.temperatureUnit
                    ),
                    onWeatherClick = { navTo(KoColorRoute.Weather) }
                )
            }

            item {
                BioMarkersCard(
                    uiState = BioMarkersUiState(
                        insights = uiState.wellnessInsights,
                        sleepDuration = uiState.lastNightSleepDuration,
                        hydrationLiters = uiState.hydrationLiters,
                        hydrationGoalLiters = uiState.hydrationGoalLiters,
                        isPermissionGranted = uiState.isHealthPermissionGranted
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navTo(KoColorRoute.Health) },
                    onGrantPermissionsClick = { navTo(KoColorRoute.Health) }
                )
            }

            item {
                if (uiState.currentRoutine != null && uiState.currentRoutineTitle != null && uiState.currentRoutineDescription != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SectionTitle(
                            uiState = SectionTitleUiState(
                                uiState.currentRoutineTitle, 
                                stringResource(R.string.applications_kocolor_apps_mobile_features_home_biosynced_ritual)
                            ), 
                            onEvent = {}, 
                            navTo = {}
                        )
                        BioRoutineSummaryCard(
                            uiState = BioRoutineSummaryUiState(
                                title = uiState.currentRoutineTitle,
                                description = uiState.currentRoutineDescription,
                                completedCount = uiState.currentRoutine.steps.count { it.isCompleted },
                                totalCount = uiState.currentRoutine.steps.size,
                                isDaytime = uiState.isDaytime,
                                backgroundModel = when (uiState.currentRoutine.time) {
                                    com.zoewave.probase.core.model.ritual.RoutineTime.MORNING -> RoutinesR.drawable.morning_routine_bg
                                    com.zoewave.probase.core.model.ritual.RoutineTime.MEALS -> RoutinesR.drawable.meals_ritual_bg
                                    else -> RoutinesR.drawable.night_routine_bg
                                }
                            ),
                            onClick = { navTo(KoColorRoute.RoutineDetail(uiState.currentRoutine.id)) },
                            onLayersClick = { navTo(KoColorRoute.Routines) }
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
                BioStoreCard(
                    uiState = uiState.storeUiState,
                    onEvent = { event ->
                        when (event) {
                            StoreEvent.ToggleExpansion -> onEvent(HomeEvent.ToggleStoreExpansion)
                            StoreEvent.EnterStore -> { /* TODO */ }
                        }
                    },
                    navTo = navTo,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }
}

