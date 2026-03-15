package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksUiState

@Composable
fun TasksListScreen(
    uiState: TasksUiState,
    onEvent: (TasksEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Localized Scaffold ensures the FAB talks directly to this screen's onEvent
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(TasksEvent.OnAddRandomTaskClicked) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Random Task")
            }
        }
    ) { localPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(localPadding)) {
            if (uiState.tasks.isEmpty()) {
                Text(
                    text = "Tap the + button to test DB inserts!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.tasks, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onToggle = { isCompleted ->
                                onEvent(TasksEvent.OnTaskToggled(task.id, isCompleted))
                            }
                        )
                    }
                }
            }
        }
    }
}
