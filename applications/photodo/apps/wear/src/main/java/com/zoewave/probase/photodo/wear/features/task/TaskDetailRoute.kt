package com.zoewave.probase.photodo.wear.features.task

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.wear.tooling.preview.devices.WearDevices
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.photodo.data.util.loadAssetAsBitmap
import com.zoewave.probase.photodo.wear.ui.theme.PhotoDoWearTheme

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
        uiState = uiState
    )
}

@Composable
fun TaskDetailScreen(
    modifier: Modifier = Modifier,
    uiState: TaskDetailUiState
) {
    val listState = rememberScalingLazyListState()
    val context = LocalContext.current
    var projectBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    if (uiState is TaskDetailUiState.Success && uiState.hasPhoto) {
        LaunchedEffect(uiState.projectId) {
            android.util.Log.d("PhotoDoSync_UI", "TaskDetail: Loading photo for project ${uiState.projectId}")
            projectBitmap = loadAssetAsBitmap(context, "/photodo/sync_state", "photo_${uiState.projectId}")
            if (projectBitmap != null) {
                android.util.Log.d("PhotoDoSync_UI", "TaskDetail: Successfully loaded photo for project ${uiState.projectId}")
            } else {
                android.util.Log.w("PhotoDoSync_UI", "TaskDetail: Failed to load photo for project ${uiState.projectId}")
            }
        }
    }

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

                    // --- PRIMARY PHOTO (WEAR VERSION) ---
                    if (projectBitmap != null) {
                        item {
                            Image(
                                bitmap = projectBitmap!!.asImageBitmap(),
                                contentDescription = "Project Thumbnail",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .size(100.dp)
                                    .padding(vertical = 8.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // --- PHOTOS COUNT ---
                    if (uiState.photoCount > 0) {
                        item {
                            Text(
                                "Photos (${uiState.photoCount})",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        if (projectBitmap == null) {
                            item {
                                Text(
                                    "View photos on phone",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                        TaskItem(task = task)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: TaskEntity) {
    // One-Way Sync: Watch is View-Only. Checkboxes are disabled.
    CheckboxButton(
        checked = task.isChecked,
        onCheckedChange = { /* Disabled in View-Only mode */ },
        enabled = false, // Purely read-only icon
        label = { Text(task.text) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun TaskDetailScreenPreview() {
    val sampleTasks = listOf(
        TaskEntity(taskId = 1, projectId = 1, text = "Buy white paint", isChecked = false),
        TaskEntity(taskId = 2, projectId = 1, text = "Measure cabinets", isChecked = true),
        TaskEntity(taskId = 3, projectId = 1, text = "Pick up brushes", isChecked = false)
    )
    val uiState = TaskDetailUiState.Success(
        projectId = 1,
        projectTitle = "Kitchen Renovation",
        tasks = sampleTasks,
        photos = emptyList(),
        photoCount = 2,
        hasPhoto = true
    )
    PhotoDoWearTheme {
        TaskDetailScreen(uiState = uiState)
    }
}
