package com.zoewave.probase.photodo.wear.features.task

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
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
        uiState = uiState
    )
}

@Composable
fun TaskDetailScreen(
    modifier: Modifier = Modifier,
    uiState: TaskDetailUiState
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

                    // --- PHOTOS COUNT ---
                    if (uiState.photoCount > 0) {
                        item {
                            Text(
                                "Photos (${uiState.photoCount})",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        item {
                            Text(
                                "View photos on phone",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
