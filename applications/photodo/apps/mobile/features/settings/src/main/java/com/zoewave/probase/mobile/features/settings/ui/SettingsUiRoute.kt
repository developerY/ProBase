package com.zoewave.probase.mobile.features.settings.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zoewave.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.mobile.features.settings.ui.components.SettingsScreen


@Composable
fun SettingsUiRoute(
    initialCardKeyToExpand: String?,
    navTo: (PhotoTodoRoute?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Pass the deep-link argument into the ViewModel on first load
    LaunchedEffect(initialCardKeyToExpand) {
        viewModel.setInitialExpandedKey(initialCardKeyToExpand)
    }

    SettingsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo,
        modifier = modifier.fillMaxSize()
    )
}
