package com.zoewave.probase.features.camera.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    // 1. Safely extract JUST the one-time event URI
// 🚀 Listen to the continuous stream of one-time events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { capturedUri ->
            Log.d("CameraDebug", "3. Route received URI from Channel: $capturedUri")
            Log.d("CameraDebug", "4. Firing navTo warp pipe!")
            navTo("result_ok:$capturedUri")
        }
    }
    CameraScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo,
        modifier = modifier.fillMaxSize()
    )
}