package com.zoewave.kocolor.mobile.glass.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun GlassApp(
    onClose: () -> Unit,
    onSpeak: (String) -> Unit,
    viewModel: GlassViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    KoColorGlassLayout(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                GlassUiEvent.CloseApp -> onClose()
                is GlassUiEvent.ToggleStep -> {
                    viewModel.onEvent(event)
                    // Optional: Speak completion status
                    // Note: This logic might be better in ViewModel if we want to wait for DB update
                    val step = uiState.morningRoutine?.steps?.find { it.id == event.stepId }
                    if (step != null && !step.isCompleted) {
                        onSpeak("Step ${step.title} completed.")
                    }
                }
            }
        }
    )
}
