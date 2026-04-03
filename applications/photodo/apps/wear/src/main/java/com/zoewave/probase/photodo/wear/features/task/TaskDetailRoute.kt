package com.zoewave.probase.photodo.wear.features.task

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TaskDetailRoute(
    viewModel: TaskDetailViewModel,
    projectId: Long?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) {
        viewModel.setProjectId(projectId)
    }

    TaskDetailScreen(
        uiState = uiState,
        onEvent = { _ ->
            // Handle events if any
        },
        modifier = modifier
    )
}
