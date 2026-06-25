package com.zoewave.probase.rxlogic.apps.mobile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.rxlogic.features.daily.DailyScreen
import com.zoewave.probase.rxlogic.features.daily.DailyViewModel
import com.zoewave.probase.rxlogic.features.medications.MedicationsScreen
import com.zoewave.probase.rxlogic.features.medications.MedicationsViewModel
import com.zoewave.probase.rxlogic.features.settings.SettingsScreen
import com.zoewave.probase.rxlogic.features.settings.SettingsViewModel
import com.zoewave.probase.rxlogic.model.navigation.RxLogicRoute

@Composable
fun RxLogicMainScreen(
    viewModel: RxLogicMainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RxLogicMainScreen(
        uiState = uiState,
        onRouteSelected = viewModel::navigateTo,
        onBack = viewModel::navigateBack,
        entryProvider = { route ->
            when (route) {
                RxLogicRoute.Main -> NavEntry(RxLogicRoute.Main) {
                    val dailyViewModel: DailyViewModel = hiltViewModel()
                    val dailyUiState by dailyViewModel.uiState.collectAsStateWithLifecycle()
                    DailyScreen(
                        uiState = dailyUiState,
                        onEvent = dailyViewModel::onEvent,
                        navTo = viewModel::navigateTo
                    )
                }
                RxLogicRoute.Medications -> NavEntry(RxLogicRoute.Medications) {
                    val medicationsViewModel: MedicationsViewModel = hiltViewModel()
                    val medicationsUiState by medicationsViewModel.uiState.collectAsStateWithLifecycle()
                    MedicationsScreen(
                        uiState = medicationsUiState,
                        onEvent = medicationsViewModel::onEvent,
                        navTo = viewModel::navigateTo
                    )
                }
                RxLogicRoute.Settings -> NavEntry(RxLogicRoute.Settings) {
                    val settingsViewModel: SettingsViewModel = hiltViewModel()
                    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
                    SettingsScreen(
                        uiState = settingsUiState,
                        onEvent = settingsViewModel::onEvent,
                        navTo = viewModel::navigateTo
                    )
                }
                is RxLogicRoute.MedicationDetail -> NavEntry(route) {
                    Text(
                        text = "Medication Detail: ${route.medicationId}",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    )
}

@Composable
private fun RxLogicMainScreen(
    uiState: RxLogicMainUiState,
    onRouteSelected: (RxLogicRoute) -> Unit,
    onBack: () -> Unit,
    entryProvider: (RxLogicRoute) -> NavEntry<RxLogicRoute>
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            RxLogicBottomBar(
                currentRoute = uiState.currentRoute,
                onRouteSelected = onRouteSelected
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = uiState.backStack,
            modifier = Modifier.padding(innerPadding),
            onBack = onBack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider
        )
    }
}

@Composable
fun RxLogicBottomBar(
    currentRoute: RxLogicRoute,
    onRouteSelected: (RxLogicRoute) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == RxLogicRoute.Main,
            onClick = { onRouteSelected(RxLogicRoute.Main) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Main") },
            label = { Text("Main") }
        )
        NavigationBarItem(
            selected = currentRoute == RxLogicRoute.Medications,
            onClick = { onRouteSelected(RxLogicRoute.Medications) },
            icon = { Icon(Icons.Default.Medication, contentDescription = "Medications") },
            label = { Text("Medications") }
        )
        NavigationBarItem(
            selected = currentRoute == RxLogicRoute.Settings,
            onClick = { onRouteSelected(RxLogicRoute.Settings) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RxLogicMainScreenPreview() {
    RxLogicMainScreen(
        uiState = RxLogicMainUiState(),
        onRouteSelected = {},
        onBack = {},
        entryProvider = { route ->
            NavEntry(route) {
                Text(
                    text = "Screen for $route",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )
}
