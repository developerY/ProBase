package com.zoewave.probase.goswift.mobile.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.goswift.features.main.navigation.GoSwiftDestination
import java.util.Locale

@Composable
fun HomeUiRoute(
    navTo: (GoSwiftDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeUiRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo,
        modifier = modifier
    )
}

@Composable
internal fun HomeUiRoute(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    navTo: (GoSwiftDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    @Suppress("UnusedParameter") navTo: (GoSwiftDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GoSwift Dashboard") },
                actions = {
                    IconButton(onClick = { onEvent(HomeUiEvent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Health Data")
                    }
                }
            )
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

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoSection(
                            title = "Sleep Duration", 
                            content = uiState.sleepDuration, 
                            modifier = Modifier.weight(1f)
                        )
                        InfoSection(
                            title = "Exercise", 
                            content = "${uiState.exerciseMinutes} min", 
                            modifier = Modifier.weight(1f)
                        )
                    }

                    InfoSection(
                        title = "Daily Hydration", 
                        content = String.format(Locale.getDefault(), "%.2f L logged", uiState.hydrationProgress)
                    )

                    InfoSection(
                        title = "Daily Calories", 
                        content = String.format(Locale.getDefault(), "%.0f kcal logged", uiState.caloriesIntake)
                    )

                    InfoSection(title = "Recommendation", content = uiState.nextDoseRecommendation)
                    InfoSection(title = "Sleep Impact", content = uiState.sleepQualityImpact)
                }
            }
        }
    }
}

@Composable
fun InfoSection(title: String, content: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(4.dp))
        Text(content, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoSectionPreview() {
    MaterialTheme {
        InfoSection(title = "Title", content = "Content text")
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeUiRoutePreview() {
    MaterialTheme {
        HomeUiRoute(
            uiState = HomeUiState.Success(
                currentCaffeineMg = 120,
                nextDoseRecommendation = "Energy level optimal. Wait 2 hours.",
                sleepQualityImpact = "High levels might disrupt sleep.",
                sleepDuration = "7h 30m",
                exerciseMinutes = 45
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            uiState = HomeUiState.Success(
                currentCaffeineMg = 120,
                nextDoseRecommendation = "Energy level optimal. Wait 2 hours.",
                sleepQualityImpact = "High levels might disrupt sleep.",
                sleepDuration = "7h 30m",
                exerciseMinutes = 45
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
