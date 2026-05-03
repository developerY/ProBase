package com.zoewave.probase.feature.places.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.feature.places.ui.components.CoffeeShopList
import com.zoewave.probase.core.ui.R as CoreUiR

@Composable
fun CoffeeShopUIRoute(
    navTo: (String) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    viewModel: CoffeeShopViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CoffeeShopUIRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo,
        modifier = modifier.padding(paddingValues)
    )
}

@Composable
internal fun CoffeeShopUIRoute(
    uiState: CoffeeShopUIState,
    onEvent: (CoffeeShopEvent) -> Unit,
    navTo: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is CoffeeShopUIState.Loading -> LoadingScreen(modifier = modifier)
        is CoffeeShopUIState.Success -> CoffeeShopList(
            coffeeShops = uiState.coffeeShops,
        )
        is CoffeeShopUIState.Error -> ErrorScreen(
            message = uiState.message,
            onRetry = { onEvent(CoffeeShopEvent.LoadCoffeeShops) },
            modifier = modifier
        )
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text(stringResource(CoreUiR.string.core_ui_action_retry))
        }
    }
}

