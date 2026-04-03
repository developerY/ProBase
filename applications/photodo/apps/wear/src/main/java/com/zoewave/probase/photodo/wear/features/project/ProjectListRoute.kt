package com.zoewave.probase.photodo.wear.features.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProjectListRoute(
    viewModel: ProjectListViewModel,
    categoryId: Long?,
    onNavigateToProject: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(categoryId) {
        viewModel.setCategoryId(categoryId)
    }

    ProjectListScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is ProjectListEvent.OnProjectClick -> onNavigateToProject(event.id, event.name)
            }
        },
        modifier = modifier
    )
}
