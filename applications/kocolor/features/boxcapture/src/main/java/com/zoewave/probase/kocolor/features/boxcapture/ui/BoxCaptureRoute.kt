package com.zoewave.probase.kocolor.features.boxcapture.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun BoxCaptureRoute(
    mode: String,
    onSuccess: (CosmeticItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BoxCaptureViewModel = hiltViewModel()
) {
    androidx.compose.runtime.LaunchedEffect(mode) {
        viewModel.setMode(mode)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BoxCaptureUiRoute(
        uiState = uiState,
        modifier = modifier,
        onEvent = { event ->
            when (event) {
                is BoxCaptureEvent.Success -> onSuccess(event.item)
                BoxCaptureEvent.Dismiss -> onDismiss()
                else -> viewModel.onEvent(event)
            }
        },
        navTo = {} 
    )
}
