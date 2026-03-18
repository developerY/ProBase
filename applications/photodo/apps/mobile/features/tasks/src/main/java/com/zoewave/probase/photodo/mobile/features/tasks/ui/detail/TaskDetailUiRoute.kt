package com.zoewave.probase.photodo.mobile.features.tasks.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TaskDetailUiRoute(
    listId: Long,
    listTitle: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskDetailViewModel = hiltViewModel() // Scoped automatically by Nav3!
) {
    // 1. Kick off the database query when this route enters the composition
    LaunchedEffect(listId) {
        viewModel.loadTaskDetails(listId)
    }

    // 2. Safely collect the Room database flow
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 3. Auto-Navigation Rule: If the user deletes the list, pop the screen
    LaunchedEffect(uiState.loadState) {
        if (uiState.loadState is DetailLoadState.Error) {
            onNavigateBack()
        }
    }

    // 4. Finally, call the "Dumb" Screen Composable!
    TaskDetailScreen(
        listTitle = listTitle,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}