package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TasksUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TasksListScreen(
    uiState: TasksUiState,
    onEvent: (TasksEvent) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToDetail: (Long, String) -> Unit
) {
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("My Projects") },
                actions = {
                    // 🐞 Debug Buttons
                    IconButton(onClick = { onEvent(TasksEvent.OnGenerateFullMockDataClicked) }) {
                        Icon(Icons.Default.BugReport, contentDescription = "Populate DB")
                    }
                    IconButton(onClick = { onEvent(TasksEvent.OnClearDatabaseClicked) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear DB")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButtonMenu(
                expanded = fabMenuExpanded,
                button = {
                    ToggleFloatingActionButton(
                        checked = fabMenuExpanded,
                        onCheckedChange = { fabMenuExpanded = !fabMenuExpanded }
                    ) {
                        val imageVector by remember {
                            derivedStateOf {
                                if (checkedProgress > 0.5f) Icons.Default.Close else Icons.Default.Add
                            }
                        }
                        Icon(
                            painter = rememberVectorPainter(imageVector),
                            contentDescription = "Toggle Add Menu",
                            modifier = Modifier.animateIcon({ checkedProgress })
                        )
                    }
                }
            ) {
                // 1. Add Task List (The "Bucket")
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onEvent(TasksEvent.OnAddListClicked)
                    },
                    icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = null) },
                    text = { Text("New List") }
                )
                // 2. Add Category (The "Super Bucket")
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onEvent(TasksEvent.OnAddCategoryClicked)
                    },
                    icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                    text = { Text("New Category") }
                )
            }
        }
    ) { localPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(localPadding)) {

            if (uiState.projectLists.isEmpty()) {
                Text(
                    text = "No projects yet. Tap + to create a list or category!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Iterate over the new projectLists state!
                    items(uiState.projectLists, key = { it.id }) { project ->
                        ProjectRow(
                            project = project,
                            // ✅ TRIGGER THE NAVIGATION EVENT HERE!
                            onClick = { onNavigateToDetail(project.id, project.title) }
                        )
                    }
                }
            }


            if (uiState.tasks.isEmpty()) {
                Text(
                    text = "No projects yet. Tap + to create a list or category!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // We will update this to show Categories containing Task Lists
                    items(uiState.tasks, key = { it.id }) { taskList ->
                        // Tapping this row will trigger navigation to the Detail Screen!
                        TaskRow(
                            task = taskList,
                            onToggle = { isCompleted ->
                                onEvent(TasksEvent.OnTaskToggled(taskList.id, isCompleted))
                            }
                        )
                    }
                }
            }
        }
    }
}

// ... (Your TasksListScreen composable up here) ...

@Preview(showBackground = true, name = "1. Populated Projects List")
@Composable
private fun TasksListScreenPopulatedPreview() {
    MaterialTheme {
        // Create a mock state with some realistic relational data
        val mockState = TasksUiState(
            projectLists = listOf(
                ProjectListUiModel(
                    id = 1001L,
                    title = "Boxabl PreFab Home Build",
                    categoryName = "Real Estate"
                ),
                ProjectListUiModel(
                    id = 1002L,
                    title = "AshBike Mobile App",
                    categoryName = "Development"
                ),
                ProjectListUiModel(
                    id = 1003L,
                    title = "KoColor Brand Launch",
                    categoryName = "Business"
                )
            )
        )

        Surface {
            TasksListScreen(
                uiState = TasksUiState(projectLists = emptyList()),
                onEvent = TODO(),
                modifier = TODO(),
                onNavigateToDetail = TODO(),
            )
            // Note: If you ended up adding `onNavigateToDetail` directly to this
            // screen's signature instead of the Route wrapper, just add a dummy
            // lambda here like: `onNavigateToDetail = { _, _ -> }`

        }
    }
}

@Preview(showBackground = true, name = "2. Empty State")
@Composable
private fun TasksListScreenEmptyPreview() {
    MaterialTheme {
        Surface {
            TasksListScreen(
                uiState = TasksUiState(projectLists = emptyList()),
                onEvent = TODO(),
                modifier = TODO(),
                onNavigateToDetail = TODO(),
            )
        }
    }
}