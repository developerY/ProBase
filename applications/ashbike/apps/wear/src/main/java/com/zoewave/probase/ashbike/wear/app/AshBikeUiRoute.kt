package com.zoewave.probase.ashbike.wear.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.ashbike.wear.presentation.screens.ride.WearBikeViewModel

/**
 * The Route is a state-hoisting wrapper. It has NO UI of its own.
 * Its only job is to bind the ViewModel to the Compose lifecycle
 * and pass pure data/callbacks down to the App container.
 */
@Composable
fun AshBikeUiRoute(
    viewModel: WearBikeViewModel = hiltViewModel()
) {
    // 1. COLLECT STATE
    // collectAsStateWithLifecycle() is critical on Wear OS.
    // It automatically stops collecting flows (like GPS/Heart Rate updates)
    // when the watch screen goes to sleep (Ambient mode) or the app is hidden.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 2. DELEGATE TO THE UI
    // We pass the immutable state down, and pass a method reference
    // (viewModel::onEvent) to handle user interactions flowing back up.
    AshBikeApp(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}