package com.zoewave.probase.photodo.mobile.features.tasks.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.photodo.mobile.features.tasks.ui.components.TasksListScreen

@Composable
fun TasksListUiRoute(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TasksListScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun TasksListScreenPreview() {
    TasksListScreen(
        uiState = TasksUiState(
            tasks = listOf(
                TaskItemUiModel(1, "Preview DB Task 1", false),
                TaskItemUiModel(2, "Preview DB Task 2", true)
            )
        ),
        onEvent = {}
    )
}