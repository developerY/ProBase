package com.zoewave.probase.photodo.wear.features.task

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.photodo.data.util.loadAssetAsBitmap
import com.zoewave.probase.photodo.wear.ui.theme.PhotoDoWearTheme
import kotlinx.coroutines.launch

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
    val projectBitmaps = remember { mutableStateListOf<android.graphics.Bitmap>() }

    if (uiState is TaskDetailUiState.Success && uiState.hasPhoto) {
        LaunchedEffect(uiState.projectId) {
            projectBitmaps.clear()
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

                    // --- PROJECT PHOTOS (HORIZONTAL GALLERY WITH ARROWS) ---
                    if (projectBitmaps.isNotEmpty()) {
                        item {
                            HorizontalImageGallery(bitmaps = projectBitmaps)
                        }
                    }

                    // --- PHOTOS COUNT ---
                    if (uiState.photoCount > 0) {
                        item {
                            Text(
                                "Total Photos: ${uiState.photoCount}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                            )
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
fun HorizontalImageGallery(bitmaps: List<android.graphics.Bitmap>) {
    val rowState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    val canScrollBackward by remember {
        derivedStateOf { rowState.firstVisibleItemIndex > 0 }
    }
    val canScrollForward by remember {
        derivedStateOf { 
            val lastVisibleIndex = rowState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex < bitmaps.size - 1 
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Left Arrow
        IconButton(
            onClick = {
                scope.launch {
                    val prevIndex = (rowState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                    rowState.animateScrollToItem(prevIndex)
                }
            },
            modifier = Modifier.size(24.dp),
            enabled = canScrollBackward
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous",
                modifier = Modifier.size(16.dp)
            )
        }

        // Horizontal List of Small Images (50dp)
        LazyRow(
            state = rowState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            itemsIndexed(bitmaps) { _, bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Project Thumbnail",
                    modifier = Modifier.size(50.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Right Arrow
        IconButton(
            onClick = {
                scope.launch {
                    val nextIndex = (rowState.firstVisibleItemIndex + 1).coerceAtMost(bitmaps.size - 1)
                    rowState.animateScrollToItem(nextIndex)
                }
            },
            modifier = Modifier.size(24.dp),
            enabled = canScrollForward
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next",
                modifier = Modifier.size(16.dp)
            )
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
