package com.zoewave.probase.photodo.wear.features.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import com.zoewave.probase.photodo.wear.features.project.ui.ProjectCard

@Composable
fun ProjectListScreen(
    uiState: ProjectListUiState,
    onEvent: (ProjectListEvent) -> Unit,
    modifier: Modifier = Modifier
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
                Text(stringResource(R.string.applications_photodo_apps_wear_features_project_no_projects))
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
                            onClick = { onEvent(ProjectListEvent.OnProjectClick(project.id, project.name)) }
                        )
                    }
                }
            }
        }
    }
}
