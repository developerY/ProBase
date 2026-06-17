package com.zoewave.probase.kocolor.mobile.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun KoColorMainRoute(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    KoColorMainScreen(
        uiState = KoColorMainUiState(
            mainState = uiState,
            windowSizeClass = windowSizeClass
        ),
        modifier = modifier,
        onEvent = viewModel::onEvent,
        navTo = { viewModel.onEvent(MainEvent.NavigateTo(it)) }
    )
}
