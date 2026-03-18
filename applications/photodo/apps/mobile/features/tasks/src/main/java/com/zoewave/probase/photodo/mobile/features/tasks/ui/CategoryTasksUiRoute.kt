package com.zoewave.probase.photodo.mobile.features.tasks.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.photodo.mobile.features.tasks.ui.components.TasksListScreen

@Composable
fun CategoryTasksUiRoute(
    categoryId: Long,
    categoryName: String,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long, String) -> Unit, // The drill-down to photos!
    modifier: Modifier = Modifier,
    viewModel: CategoryTasksViewModel = hiltViewModel()
) {
    // 1. Tell the ViewModel which category to load
    LaunchedEffect(categoryId) {
        viewModel.loadCategory(categoryId, categoryName)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 2. Reuse your existing screen!
    TasksListScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToDetail = onNavigateToDetail,
        // We pass the dynamic title and back button down to the screen
        screenTitle = categoryName,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}