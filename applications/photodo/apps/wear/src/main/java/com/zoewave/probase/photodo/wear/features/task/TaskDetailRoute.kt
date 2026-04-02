package com.zoewave.probase.photodo.wear.features.task

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity

@Composable
fun TaskDetailRoute(
    modifier: Modifier = Modifier,
    projectId: Long?,
    viewModel: TaskDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) {
        viewModel.setProjectId(projectId)
    }

    TaskDetailScreen(
        modifier = modifier,
        uiState = uiState,
        onToggleTask = viewModel::onToggleTask
    )
}

@Composable
fun TaskDetailScreen(
    modifier: Modifier = Modifier,
    uiState: TaskDetailUiState,
    onToggleTask: (TaskEntity, Boolean) -> Unit
) {
    val listState = rememberScalingLazyListState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            TaskDetailUiState.Loading -> {
                CircularProgressIndicator()
            }
            TaskDetailUiState.Empty -> {
                Text("Project Not Found")
            }
            is TaskDetailUiState.Success -> {
                ScalingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
                ) {
                    item {
                        ListHeader {
                            Text(uiState.projectTitle)
                        }
                    }

                    // --- PHOTOS ---
                    if (uiState.photos.isNotEmpty()) {
                        item {
                            Text(
                                "Photos",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(uiState.photos) { photo ->
                            PhotoItem(photo = photo)
                        }
                    }

                    // --- TASKS ---
                    item {
                        Text(
                            "Tasks",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(uiState.tasks) { task ->
                        TaskItem(
                            task = task,
                            onCheckedChange = { onToggleTask(task, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: TaskEntity,
    onCheckedChange: (Boolean) -> Unit
) {
    CheckboxButton(
        checked = task.isChecked,
        onCheckedChange = onCheckedChange,
        label = { Text(task.text) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PhotoItem(photo: PhotoEntity) {
    AsyncImage(
        model = photo.photoUri,
        contentDescription = "Project Photo",
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 4.dp),
        contentScale = ContentScale.Crop
    )
}
