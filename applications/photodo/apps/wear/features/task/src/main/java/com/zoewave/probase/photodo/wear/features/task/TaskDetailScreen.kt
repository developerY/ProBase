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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
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

@Composable
fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    onEvent: (TaskDetailEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberScalingLazyListState()
    val context = LocalContext.current
    val projectBitmaps = remember { mutableStateListOf<android.graphics.Bitmap>() }

    if (uiState is TaskDetailUiState.Success) {
        LaunchedEffect(uiState.projectId, uiState.photoCount, uiState.hasPhoto) {
            projectBitmaps.clear()
            if (uiState.hasPhoto) {
                // Try to load up to 5 synced photos
                for (i in 0 until 5) {
                    val bitmap = loadAssetAsBitmap(context, "/photodo/sync_state", "photo_${uiState.projectId}_$i")
                    if (bitmap != null) {
                        projectBitmaps.add(bitmap)
                    } else {
                        if (i > 0) break 
                    }
                }
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
                Text(stringResource(R.string.applications_photodo_apps_wear_features_task_project_not_found))
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

                    // --- PROJECT PHOTOS (VERTICAL LIST) ---
                    if (projectBitmaps.isNotEmpty()) {
                        items(projectBitmaps) { bitmap ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = stringResource(R.string.applications_photodo_apps_wear_features_task_thumbnail_cd),
                                    modifier = Modifier
                                        .size(140.dp)
                                        .rotate(90f)
                                        .clip(MaterialTheme.shapes.large),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // --- PHOTOS COUNT ---
                    if (uiState.photoCount > 0) {
                        item {
                            Text(
                                text = stringResource(R.string.applications_photodo_apps_wear_features_task_photos_count_format, uiState.photoCount),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                            )
                        }
                    }

                    // --- TASKS ---
                    item {
                        Text(
                            text = stringResource(R.string.applications_photodo_apps_wear_features_task_tasks),
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
private fun TaskItem(task: TaskEntity) {
    // One-Way Sync: Watch is View-Only. Checkboxes are disabled.
    CheckboxButton(
        checked = task.isChecked,
        onCheckedChange = { /* Disabled in View-Only mode */ },
        enabled = false, // Purely read-only icon
        label = { Text(task.text) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showBackground = true, showSystemUi = true)
@Composable
fun TaskDetailScreenPreview() {
    val sampleTasks = listOf(
        TaskEntity(taskId = 1, projectId = 1, text = "Buy white paint", isChecked = false),
        TaskEntity(taskId = 2, projectId = 1, text = "Measure cabinets", isChecked = true),
        TaskEntity(taskId = 3, projectId = 1, text = "Hire a plumber", isChecked = false)
    )
    val sampleUiState = TaskDetailUiState.Success(
        projectId = 1,
        projectTitle = "Kitchen Reno",
        tasks = sampleTasks,
        photos = emptyList(),
        photoCount = 2,
        hasPhoto = true
    )
    TaskDetailScreen(
        uiState = sampleUiState,
        onEvent = {}
    )
}
