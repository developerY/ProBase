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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TasksUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TasksListScreen(
    modifier: Modifier = Modifier,
    uiState: TasksUiState,
    onEvent: (TasksEvent) -> Unit,
    onNavigateToDetail: (Long, String) -> Unit,
    screenTitle: String = "My Projects", // Default for the Global tab
    onNavigateBack: (() -> Unit)? = null // Null means we are on the Global tab (no back button)
) {
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                // ✅ FIX 1: Use the dynamic screenTitle so it says "Mock Category 451"
                title = { Text(screenTitle) },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
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
            // ... (Keep your existing FAB Menu code exactly as it is here!) ...
        }
    ) { localPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(localPadding)
        ) {
            // ✅ FIX 2: A strict if/else chain so UI elements NEVER overlap!

            if (uiState.isNoCategoriesYet) {
                // Scenario A: Database is completely empty
                Text(
                    text = "No categories yet.\nTap + to create a Category first!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.projectLists.isEmpty()) {
                // Scenario B: This specific category is empty
                Text(
                    text = "No projects in ${uiState.categoryName}.\nTap + to create one!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Scenario C: We have data! Render the list cleanly.
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