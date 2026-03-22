package com.zoewave.probase.photodo.mobile.features.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.mobile.features.home.ui.components.HomeScreen
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel

@Composable
fun HomeUiRoute(
    navTo: (PhotoTodoRoute) -> Unit, // ✅ The single unified navigation channel
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent, // ✅ Business logic goes here
        navTo = navTo,                // ✅ Screen jumps go here
        modifier = modifier
    )
}