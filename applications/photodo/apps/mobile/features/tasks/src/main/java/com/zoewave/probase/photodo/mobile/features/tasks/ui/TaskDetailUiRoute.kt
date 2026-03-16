package com.zoewave.probase.photodo.mobile.features.tasks.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zoewave.probase.photodo.mobile.features.tasks.ui.components.TaskDetailScreen

@Composable
fun TaskDetailUiRoute(
    listId: Long,
    listTitle: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    // viewModel: TaskDetailViewModel = hiltViewModel() // You'll create this next!
) {
    // val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TaskDetailScreen(
        listTitle = listTitle,
        onEvent = { /* viewModel::onEvent */ },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}