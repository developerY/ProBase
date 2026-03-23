package com.zoewave.probase.features.camera.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.camera.ui.components.CameraScreen

@Composable
fun CameraUIRoute(
    navTo: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CamViewModel = hiltViewModel()
) {
    // 1. Collect State
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 2. Pass strict unidirectional data flow to the Dumb Screen
    CameraScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo,
        modifier = modifier.fillMaxSize()
    )
}