package com.zoewave.probase.photodo.mobile.features.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.mobile.features.settings.ui.components.SettingsScreen


@Composable
fun SettingsUiRoute(
    navTo: (PhotoTodoRoute?) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo
    )
}
