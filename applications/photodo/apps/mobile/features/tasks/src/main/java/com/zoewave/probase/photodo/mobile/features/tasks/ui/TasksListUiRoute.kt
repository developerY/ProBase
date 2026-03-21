package com.zoewave.probase.photodo.mobile.features.tasks.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.photodo.mobile.features.tasks.ui.components.TasksListScreen

@Composable
fun TasksListUiRoute(
    categoryId: Long?,
    categoryName: String?,
    onNavigateToDetail: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = hiltViewModel()
) {
    // 1. Tell the ViewModel which category to load
    LaunchedEffect(categoryId) {
        viewModel.setCategoryId(categoryId)
    }

    // 2. Collect ONLY the main UI state
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 3. Pass ONLY State, Events, and Navigation to the dumb screen
    TasksListScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToDetail = onNavigateToDetail,
        screenTitle = uiState.categoryName,
        modifier = modifier
    )
}