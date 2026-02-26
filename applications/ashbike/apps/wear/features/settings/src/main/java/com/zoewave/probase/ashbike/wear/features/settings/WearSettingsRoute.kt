package com.zoewave.probase.ashbike.wear.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun WearSettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToEBikeSetup: () -> Unit = {}
) {
    // Collecting state from DataStore via the ViewModel
    val isAutoPause by viewModel.isAutoPauseEnabled.collectAsState(initial = false)
    val isMetric by viewModel.isMetricUnits.collectAsState(initial = true)
    val isHealthSync by viewModel.isHealthConnectEnabled.collectAsState(initial = false)

    WearSettingsPage(
        isAutoPauseEnabled = isAutoPause,
        onAutoPauseToggled = { viewModel.setAutoPause(it) },
        isMetricUnits = isMetric,
        onMetricUnitsToggled = { viewModel.setMetricUnits(it) },
        isHealthConnectEnabled = isHealthSync,
        onHealthConnectToggled = { viewModel.setHealthConnect(it) },
        onManageEBikeClick = onNavigateToEBikeSetup
    )
}