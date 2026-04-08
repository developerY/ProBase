package com.zoewave.probase.goswift.wear.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.*
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.rememberColumnState
import com.zoewave.probase.goswift.features.main.navigation.GoSwiftDestination

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    navigateTo: (GoSwiftDestination) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val columnState = rememberColumnState()

    HomeDashboard(
        modifier = modifier,
        uiState = uiState,
        columnState = columnState,
        onLogClick = { navigateTo(GoSwiftDestination.Log) }
    )
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun HomeDashboard(
    uiState: HomeUiState,
    columnState: ScalingLazyColumnState,
    onLogClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScalingLazyColumn(
        columnState = columnState,
        modifier = modifier.fillMaxSize()
    ) {
        item {
            ListHeader {
                Text("GoSwift")
            }
        }

        when (uiState) {
            HomeUiState.Loading -> {
                item {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
            is HomeUiState.Success -> {
                item {
                    CaffeineChip(uiState.currentCaffeineMg)
                }
                item {
                    Card(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors()
                    ) {
                        Text("Sleep: ${uiState.sleepDuration}")
                    }
                }
                item {
                    Card(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors()
                    ) {
                        Text("Exercise: ${uiState.exerciseMinutes}m")
                    }
                }
                item {
                    Button(
                        onClick = onLogClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log Activity")
                    }
                }
            }
        }
    }
}

@Composable
fun CaffeineChip(mg: Int) {
    Card(
        onClick = {},
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Caffeine", style = MaterialTheme.typography.labelSmall)
            Text("${mg}mg", style = MaterialTheme.typography.displaySmall)
        }
    }
}
