package com.zoewave.probase.goswift.mobile.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.goswift.features.main.navigation.GoSwiftDestination

@Composable
fun HomeUiRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    navTo: (GoSwiftDestination) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("GoSwift Dashboard") })
        },
        modifier = modifier
    ) { padding ->
        when (uiState) {
            HomeUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Current Caffeine", style = MaterialTheme.typography.labelLarge)
                            Text("${uiState.currentCaffeineMg} mg", style = MaterialTheme.typography.displayLarge)
                        }
                    }

                    InfoSection(title = "Recommendation", content = uiState.nextDoseRecommendation)
                    InfoSection(title = "Sleep Impact", content = uiState.sleepQualityImpact)
                }
            }
        }
    }
}

@Composable
fun InfoSection(title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(4.dp))
        Text(content, style = MaterialTheme.typography.bodyLarge)
    }
}
