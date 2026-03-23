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


    // TODO: [Architecture Optimization] Replace Nav3 String Return with Injected Domain UseCase
    // Currently, the camera passes the saved URI back to the host app via a Nav3 string
    // payload ("result_ok:[URI]"). While this perfectly isolates the camera module, a more
    // direct and type-safe approach for enterprise scaling is:
    //
    // 1. Create a lightweight `interface ImageStorageUseCase { suspend fun saveImage(uri: String) }`
    //    inside the `:core:domain` module.
    // 2. Write the concrete implementation (`PhotoDoImageStorageImpl`) in the `:app` module
    //    where the Room Database repository lives.
    // 3. Bind the implementation in Hilt, and `@Inject` the interface directly into `CamViewModel`.
    //
    // This allows the Camera module to save directly to the DB without knowing what a DB is,
    // eliminating the need to parse navigation strings or rely on the UI layer for data handoffs.

    // 1. Safely extract JUST the one-time event URI
    // Listen to the continuous stream of one-time events
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