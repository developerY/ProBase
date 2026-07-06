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
    target: String = "back",
    navTo: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(target) {
        viewModel.setCameraTarget(target)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { capturedUri ->
            Log.d("CameraDebug", "3. Route received URI from Channel: $capturedUri")
            Log.d("CameraDebug", "4. Firing navTo warp pipe!")
            navTo("result_ok:$capturedUri")
        }
    }

    CameraUIRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo,
        modifier = modifier
    )
}

@Composable
internal fun CameraUIRoute(
    uiState: CamUIState,
    onEvent: (CamEvent) -> Unit,
    navTo: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    CameraScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo,
        modifier = modifier.fillMaxSize()
    )
}

/*
@Preview(showBackground = true)
@Composable
private fun CameraUIRoutePreview() {
    val state = CamUIState()
    CameraUIRoute(
        uiState = state,
        onEvent = {},
        navTo = {}
    )
}
*/