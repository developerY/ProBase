package com.zoewave.probase.ashbike.wear.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


@Composable
fun WearSettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToAbout: () -> Unit, // Add this so the Route can tell the NavGraph to move
    onNavigateToEBikeSetup: () -> Unit = {},
    onNavigateToExperiments: () -> Unit = {}
) {
    // 1. Collect the single source of truth from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // 2. Pass everything down to the dumb UI
    WearSettingsPage(
        uiState = uiState,
        onEvent = viewModel::onEvent, // Neatly passes all toggle events to the ViewModel
        onNavigateToAbout = onNavigateToAbout,
        onManageEBikeClick = onNavigateToEBikeSetup,
        onNavigateToExperiments = onNavigateToExperiments
    )
}