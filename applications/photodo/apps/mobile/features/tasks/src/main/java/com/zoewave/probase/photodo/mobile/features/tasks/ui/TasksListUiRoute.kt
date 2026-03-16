package com.zoewave.probase.photodo.mobile.features.tasks.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.photodo.mobile.features.tasks.ui.components.AddCategorySheet
import com.zoewave.probase.photodo.mobile.features.tasks.ui.components.AddListSheet
import com.zoewave.probase.photodo.mobile.features.tasks.ui.components.TasksListScreen
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TasksUiState

@Composable
fun TasksListUiRoute(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = hiltViewModel(),
    onNavigateToDetail: (Long, String) -> Unit, // ✅ Accept the Nav3 callback
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Grab the draft state too!
    val draftState by viewModel.draftState.collectAsStateWithLifecycle()

    TasksListScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )

    // Render the isolated Bottom Sheets based on the state flags
    if (uiState.isAddCategorySheetOpen) {
        AddCategorySheet(
            draftState = draftState,
            onEvent = viewModel::onEvent,
            onDismiss = { viewModel.onEvent(TasksEvent.OnDismissBottomSheet) }
        )
    }

    if (uiState.isAddListSheetOpen) {
        AddListSheet(
            draftState = draftState,
            onEvent = viewModel::onEvent,
            onDismiss = { viewModel.onEvent(TasksEvent.OnDismissBottomSheet) }
        )
    }
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