package com.zoewave.probase.photodo.wear.features.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import com.zoewave.probase.photodo.wear.ui.components.ProjectCard

@Composable
fun ProjectListRoute(
    modifier: Modifier = Modifier,
    categoryId: Long?,
    viewModel: ProjectListViewModel = hiltViewModel(),
    onProjectClick: (Long, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(categoryId) {
        viewModel.setCategoryId(categoryId)
    }

    ProjectListScreen(
        modifier = modifier,
        uiState = uiState,
        onProjectClick = onProjectClick
    )
}

@Composable
fun ProjectListScreen(
    modifier: Modifier = Modifier,
    uiState: ProjectListUiState,
    onProjectClick: (Long, String) -> Unit
) {
    val listState = rememberScalingLazyListState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            ProjectListUiState.Loading -> {
                CircularProgressIndicator()
            }
            ProjectListUiState.Empty -> {
                Text("No Projects Yet")
            }
            is ProjectListUiState.Success -> {
                ScalingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
                ) {
                    item {
                        ListHeader {
                            Text(uiState.categoryName)
                        }
                    }
                    items(uiState.projects) { project ->
                        ProjectCard(
                            project = project,
                            onClick = { onProjectClick(project.id, project.name) }
                        )
                    }
                }
            }
        }
    }
}
