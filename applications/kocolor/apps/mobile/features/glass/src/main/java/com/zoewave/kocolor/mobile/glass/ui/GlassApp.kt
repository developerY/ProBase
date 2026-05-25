package com.zoewave.kocolor.mobile.glass.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun GlassApp(
    areVisualsOn: Boolean,
    isVisualUiSupported: Boolean,
    isPermissionDenied: Boolean,
    onRetryPermission: () -> Unit,
    onClose: () -> Unit,
    onSpeak: (String) -> Unit,
    viewModel: GlassViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    KoColorGlassLayout(
        uiState = uiState,
        areVisualsOn = areVisualsOn,
        isVisualUiSupported = isVisualUiSupported,
        isPermissionDenied = isPermissionDenied,
        onRetryPermission = onRetryPermission,
        onEvent = { event ->
            when (event) {
                GlassUiEvent.CloseApp -> onClose()
                is GlassUiEvent.ToggleStep -> {
                    viewModel.onEvent(event)
                    val step = uiState.morningRoutine?.steps?.find { it.id == event.stepId }
                    if (step != null && !step.isCompleted) {
                        onSpeak("Step ${step.title} completed.")
                    }
                }
            }
        }
    )
}
