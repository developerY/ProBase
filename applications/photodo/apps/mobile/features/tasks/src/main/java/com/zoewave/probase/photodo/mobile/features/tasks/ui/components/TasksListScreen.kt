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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(
    uiState: TasksUiState,
    onEvent: (TasksEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Localized Scaffold ensures all buttons talk directly to this screen's onEvent
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                actions = {
                    // 🐞 Debug Button: Populate Mock Data
                    IconButton(onClick = { onEvent(TasksEvent.OnGenerateFullMockDataClicked) }) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "Populate DB with Mock Data"
                        )
                    }
                    // 🗑️ Debug Button: Wipe Database
                    IconButton({onEvent(TasksEvent.OnClearDatabaseClicked)}) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Entire Database"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(TasksEvent.OnAddRandomTaskClicked) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Random Task")
            }
        }
    ) { localPadding ->

        // localPadding now automatically accounts for BOTH the TopAppBar and the Bottom Navigation!
        Box(modifier = Modifier.fillMaxSize().padding(localPadding)) {
            if (uiState.tasks.isEmpty()) {
                Text(
                    text = "Tap the bug icon to generate data, or + for a single task!",
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