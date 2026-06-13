package com.zoewave.probase.kocolor.features.boxcapture.ui

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zoewave.probase.kocolor.model.CosmeticItem

@Composable
fun BoxCaptureRoute(
    mode: String,
    onSuccess: (CosmeticItem) -> Unit,
    onDismiss: () -> Unit,
    viewModel: BoxCaptureViewModel = hiltViewModel()
) {
    androidx.compose.runtime.LaunchedEffect(mode) {
        viewModel.setMode(mode)
    }

    BoxCaptureUiRoute(
        viewModel = viewModel,
        onSuccess = onSuccess,
        onDismiss = onDismiss
    )
}
