package com.zoewave.probase.goswift.mobile.input.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.zoewave.probase.goswift.features.main.navigation.GoSwiftDestination
import com.zoewave.probase.features.health.hydration.ui.HydrationScreen
import com.zoewave.probase.features.health.hydration.ui.HydrationViewModel
import com.zoewave.probase.features.health.hydration.ui.HydrationUiEvent
import com.zoewave.probase.features.health.hydration.ui.HydrationUiState
import com.zoewave.probase.goswift.mobile.nutrition.ui.NutritionScreen
import com.zoewave.probase.goswift.mobile.nutrition.ui.NutritionViewModel
import com.zoewave.probase.goswift.mobile.shots.ui.ShotsScreen
import com.zoewave.probase.goswift.mobile.shots.ui.ShotsViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun InputUiRoute(
    modifier: Modifier = Modifier,
    shotsViewModel: ShotsViewModel = hiltViewModel(),
    hydrationViewModel: HydrationViewModel = hiltViewModel(),
    nutritionViewModel: NutritionViewModel = hiltViewModel(),
    navTo: (GoSwiftDestination) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        InputTabItem("Caffeine", Icons.Default.Coffee),
        InputTabItem("Water", Icons.Default.WaterDrop),
        InputTabItem("Calories", Icons.Default.Restaurant)
    )

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(tab.title) },
                    icon = { Icon(tab.icon, contentDescription = null) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                val shotsUiState by shotsViewModel.uiState.collectAsStateWithLifecycle()
                ShotsScreen(
                    uiState = shotsUiState,
                    onEvent = shotsViewModel::onEvent,
                    navTo = navTo,
                    modifier = Modifier.weight(1f)
                )
            }
            1 -> {
                val hydrationUiState by hydrationViewModel.uiState.collectAsStateWithLifecycle()
                HydrationScreen(
                    uiState = hydrationUiState,
                    onEvent = hydrationViewModel::onEvent,
                    onNavigateToSettings = { navTo(GoSwiftDestination.Settings) },
                    modifier = Modifier.weight(1f)
                )
            }
            2 -> {
                val nutritionUiState by nutritionViewModel.uiState.collectAsStateWithLifecycle()
                NutritionScreen(
                    uiState = nutritionUiState,
                    onEvent = nutritionViewModel::onEvent,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

data class InputTabItem(val title: String, val icon: ImageVector)
