package com.zoewave.probase.features.weather.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.weather.ui.components.WeatherScreen


@Composable
fun WeatherUiRoute(
    onBack: () -> Unit,
    onNavigateToSunIntelligence: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WeatherUiRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onNavigateToSunIntelligence = onNavigateToSunIntelligence,
        modifier = modifier
    )
}

@Composable
internal fun WeatherUiRoute(
    uiState: WeatherUiState,
    onEvent: (WeatherEvent) -> Unit,
    onBack: () -> Unit,
    onNavigateToSunIntelligence: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is WeatherUiState.Loading -> {
            LoadingScreen(modifier = modifier)
        }

        is WeatherUiState.Error -> {
            ErrorScreen(
                errorMessage = uiState.message,
                onRetry = { onEvent(WeatherEvent.LoadBike) },
                modifier = modifier
            )
        }

        is WeatherUiState.Success -> {
            WeatherScreen(
                weather = uiState.weatherOpen,
                settings = uiState.settings,
                location = uiState.location,
                onEvent = onEvent,
                onBack = onBack,
                onNavigateToSunIntelligence = onNavigateToSunIntelligence,
                modifier = modifier
            )
        }
    }
}

// These will be move to a common directory.
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Text(text = "Loading...", modifier = modifier.fillMaxSize())
}

@Composable
fun ErrorScreen(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            text = "Error: $errorMessage",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Retry",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .clickable { onRetry() }
                .padding(vertical = 8.dp)
        )
    }
}

