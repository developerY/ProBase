package com.zoewave.probase.kocolor.mobile.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun KoColorMainRoute(
    windowSizeClass: WindowSizeClass,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    KoColorMainScreen(
        uiState = KoColorMainUiState(
            mainState = uiState,
            windowSizeClass = windowSizeClass
        ),
        onEvent = viewModel::onEvent,
        navTo = { viewModel.onEvent(MainEvent.NavigateTo(it)) }
    )
}
