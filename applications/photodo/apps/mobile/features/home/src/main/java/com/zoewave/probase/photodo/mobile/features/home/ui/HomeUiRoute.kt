package com.zoewave.probase.photodo.mobile.features.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.photodo.mobile.features.home.ui.components.HomeScreen

@Composable
fun HomeUiRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    // navTo: (PhotoTodoRoute) -> Unit = {} // Add this when you need to navigate away from Home
) {
    // collectAsStateWithLifecycle is MAD-recommended over collectAsState
    // as it safely pauses collection when the app is in the background
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}