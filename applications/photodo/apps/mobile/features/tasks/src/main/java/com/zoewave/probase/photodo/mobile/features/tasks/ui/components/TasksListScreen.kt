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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.tasks.R
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TasksUiState
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TasksListScreen(
    uiState: TasksUiState,
    onEvent: (TasksEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit, // ✅ Standardized Navigation Channel
    modifier: Modifier = Modifier
) {
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    // Confirmation Dialog State
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(uiState.categoryName) },
                navigationIcon = {
                    // Assume we can always go back if we are in this specific screen? 
                    // Actually, for top-level it might not show. 
                    // But in standard Nav3, we just call back.
                    IconButton(onClick = { navTo(null) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_back_content_desc)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_delete_category_content_desc)
                        )
                    }
                }
            )
        },
        // ✅ 1. THE FAB IS RESTORED
        floatingActionButton = {
            FloatingActionButtonMenu(
                expanded = fabMenuExpanded,
                button = {
                    ToggleFloatingActionButton(
                        checked = fabMenuExpanded,
                        onCheckedChange = { fabMenuExpanded = it }
                    ) {
                        val imageVector by remember {
                            derivedStateOf {
                                if (checkedProgress > 0.5f) Icons.Default.Close else Icons.Default.Add
                            }
                        }
                        Icon(
                            painter = rememberVectorPainter(imageVector),
                            contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_toggle_add_menu_content_desc),
                            modifier = Modifier.animateIcon({ checkedProgress })
                        )
                    }
                }
            ) {
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onEvent(TasksEvent.OnAddListClicked) // Opens the Project Sheet!
                    },
                    icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = null) },
                    text = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_new_project)) }
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onEvent(TasksEvent.OnAddCategoryClicked) // Opens the Category Sheet!
                    },
                    icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                    text = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_new_category)) }
                )
            }
        }
    ) { localPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(localPadding)
        ) {
            // Your exact if/else logic for the Empty States vs Data Lists
            if (uiState.isNoCategoriesYet) {
                // Scenario A: Database is completely empty
                Text(
                    text = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_no_categories),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.projectLists.isEmpty()) {
                // Scenario B: This specific category is empty
                Text(
                    text = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_no_projects_in_category, uiState.categoryName),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
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
                    items(uiState.projectLists, key = { it.projectId }) { project ->
                        ProjectCard(
                            project = project,
                            onEvent = onEvent, // Pass the channel straight down!
                            navTo = navTo
                        )
                    }
                }
            }
        }
    }


    // ✅ 2. THE SHEETS ARE ACTUALLY RENDERED HERE!
    // They sit outside the Scaffold so they can float over the entire screen.

    if (uiState.isAddListSheetOpen) {
        AddProjectBottomSheet(
            uiState = uiState.draftState,
            onEvent = onEvent,
            navTo = navTo
        )
    }

    if (uiState.isAddCategorySheetOpen) {
        AddCategorySheet(
            uiState = uiState.draftState,
            onEvent = onEvent,
            navTo = navTo
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_delete_category_title)) },
            text = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_delete_category_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        uiState.categoryId?.let { id ->
                            onEvent(TasksEvent.OnDeleteCategoryClicked(id))
                        }
                    }
                ) {
                    Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_delete_button), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_cancel_button))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TasksListScreenPreview() {
    PhotoDoTheme {
        TasksListScreen(
            uiState = TasksUiState(
                categoryName = "Work",
                projectLists = listOf(
                    ProjectListUiModel(1, "Fix leaking roof", "Home", isUrgent = true),
                    ProjectListUiModel(2, "Buy groceries", "Personal", isFavorite = true),
                    ProjectListUiModel(3, "Update resume", "Work")
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TasksListScreenEmptyPreview() {
    PhotoDoTheme {
        TasksListScreen(
            uiState = TasksUiState(
                categoryName = "Vacation",
                isNoCategoriesYet = false,
                projectLists = emptyList()
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TasksListScreenNoCategoriesPreview() {
    PhotoDoTheme {
        TasksListScreen(
            uiState = TasksUiState(
                isNoCategoriesYet = true
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TasksListBudgetScreenPreview() {
    PhotoDoTheme {
        TasksListScreen(
            uiState = TasksUiState(
                categoryName = "Work",
                projectLists = listOf(
                    ProjectListUiModel(
                        projectId = 1,
                        title = "Fix leaking roof",
                        categoryName = "Home",
                        isUrgent = true,
                        currentSpend = 1200.0,
                        projectBudget = 1000.0 // 🔴 Will show up RED (Over budget)
                    ),
                    ProjectListUiModel(
                        projectId = 2,
                        title = "Buy groceries",
                        categoryName = "Personal",
                        isFavorite = true,
                        currentSpend = 150.0,
                        projectBudget = 200.0, // 🟢 Will show up GREEN (Under budget),
                        dueDateMillis = System.currentTimeMillis() + 86400000L * 3,
                    ),
                    ProjectListUiModel(
                        projectId = 3,
                        title = "Update resume",
                        categoryName = "Work"
                        // No budget set! The card will cleanly shrink and hide the progress bar.
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
