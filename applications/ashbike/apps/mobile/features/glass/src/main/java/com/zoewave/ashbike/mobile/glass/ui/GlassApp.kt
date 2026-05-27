package com.zoewave.ashbike.mobile.glass.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.ashbike.mobile.glass.newui.AshGlassLayout

// Simple Enum to handle local navigation
enum class ScreenState {
    HOME,
    BIKE,
    GEAR_LIST
}

@Composable
fun GlassApp(
    areVisualsOn: Boolean,
    isVisualUiSupported: Boolean,
    onClose: () -> Unit,
    viewModel: GlassViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Go straight to the Bike Layout
    AshGlassLayout(
        uiState = uiState,
        areVisualsOn = areVisualsOn,
        isVisualUiSupported = isVisualUiSupported,
        onEvent = { event ->
            when (event) {
                GlassUiEvent.CloseApp -> onClose()
                else -> viewModel.onEvent(event)
            }
        }
    )
}
