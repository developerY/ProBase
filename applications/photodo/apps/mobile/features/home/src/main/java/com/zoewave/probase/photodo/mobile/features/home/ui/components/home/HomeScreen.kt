package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.home.R
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.HomeOverviewDialogs
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.HomeOverviewFab
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import components.home.CategoryQuickJumpRow
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.components.HomeProjectRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    navTo: (PhotoTodoRoute) -> Unit, // ✅ Restrictive navigation channel enforced
    modifier: Modifier = Modifier
) {
    var showAddCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<CategoryOverviewUiModel?>(null) }
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        // 🚀 Upgrade: HomeOverviewFab for consistent FAB actions
        floatingActionButton = {
            HomeOverviewFab(
                fabMenuExpanded = fabMenuExpanded,
                onFabToggle = { fabMenuExpanded = it },
                onAddCategoryClick = {
                    fabMenuExpanded = false
                    showAddCategoryDialog = true
                },
                onHomeProjectClick = {
                    fabMenuExpanded = false
                    onEvent(HomeEvent.OnAddQuickProjectClicked("Home"))
                },
                onCameraClick = {
                    fabMenuExpanded = false
                    navTo(PhotoTodoRoute.Camera(projectId = null))
                },
                onSmartCaptureClick = {
                    fabMenuExpanded = false
                    navTo(PhotoTodoRoute.SmartCapture())
                },
                isAiEnabled = uiState.isAiEnabled
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp), // Only pad the sides, let the list handle vertical padding
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp), // 🚀 Extra bottom padding to clear the FAB!
            verticalArrangement = Arrangement.spacedBy(24.dp), // 🚀 MAGIC: Replaces all your Spacers!
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- 🚀 NEW: Search Bar ---
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { onEvent(HomeEvent.OnSearchQueryChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_home_search_tasks_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { onEvent(HomeEvent.OnSearchQueryChanged("")) }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }

            if (uiState.isSearching) {
                if (uiState.searchResults.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.applications_photodo_apps_mobile_features_home_no_search_results, uiState.searchQuery),
                            modifier = Modifier.padding(top = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    uiState.searchResults.forEach { group ->
                        item(key = "project_${group.projectId}") {
                            Text(
                                text = group.projectName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navTo(PhotoTodoRoute.TaskDetail(projectId = group.projectId, projectTitle = group.projectName))
                                    }
                                    .padding(vertical = 8.dp)
                            )
                        }
                        items(group.tasks, key = { "task_${it.taskId}" }) { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navTo(PhotoTodoRoute.TaskDetail(projectId = group.projectId, projectTitle = group.projectName))
                                    }
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (task.isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (task.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = task.taskText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = if (task.isChecked) TextDecoration.LineThrough else null,
                                    color = if (task.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            } else {

                // --- 🚀 NEW: Graphic/AI Overview Section (Derivative State) ---
                if (!uiState.isLoading && !uiState.isEmpty) {
                    // Compute the models on recomposition
                    item {
                        // 🚀 1. The main "High-Density Wheel" summary card
                        OverviewSummaryCard(categories = uiState.categories) // ✅ Pass list directly
                    }

                    item {
                        val importantCategories = remember(uiState.categories) {
                            // Logic: Categories with most pending tasks
                            uiState.categories
                                .sortedByDescending { it.totalTasks - it.completedTasks }
                                .take(5) // Show top 5
                        }
                        // 2. The horizontal quick-jump section (Preserved)
                        CategoryQuickJumpRow(
                            importantCategories = importantCategories,
                            onEvent = onEvent,
                            navTo = navTo
                        )
                    }
                }
                // --- End New Sections ---

                item { // --- 3. View All Categories Button (Preserved) ---
                    Button(
                        onClick = { navTo(PhotoTodoRoute.CategoryGrid) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.applications_photodo_apps_mobile_features_home_view_all_categories))
                    }
                }

                item {
                    // --- 4. Jump Back In Section (Preserved Urgent/Fav List) ---
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Text(
                            stringResource(R.string.applications_photodo_apps_mobile_features_home_jump_back_in),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (uiState.isLoading) {
                    item { CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp)) }
                } else if (uiState.isEmpty) {
                    item {
                        Text(
                            stringResource(R.string.applications_photodo_apps_mobile_features_home_no_data_seed),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                } else {
                    if (uiState.urgentProjects.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.applications_photodo_apps_mobile_features_home_no_urgent_projects),
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    } else {
                        // Use `items` for the dynamic data! It scrolls seamlessly with the `item` blocks above.
                        items(items = uiState.urgentProjects, key = { it.projectId }) { project ->
                            HomeProjectRow(
                                project = project,
                                onEvent = onEvent, // Pass the channel down
                                navTo = navTo // Pass the channel down
                            )
                        }
                    }
                }
            }
        }
    }

    HomeOverviewDialogs(
        showAddCategorySheet = showAddCategoryDialog,
        onDismissAddCategory = { showAddCategoryDialog = false },
        uiState = uiState,
        categoryToDelete = categoryToDelete,
        onDismissDeleteConfirmation = { categoryToDelete = null },
        onEvent = onEvent,
    )
}


// --- PREVIEWS ---

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    PhotoDoTheme {
        HomeScreen(
            uiState = HomeUiState(
                categories = listOf(
                    CategoryOverviewUiModel(1L, "Nature", 3, 10, 5, 0.5f),
                    CategoryOverviewUiModel(2L, "Urban", 2, 8, 2, 0.25f)
                ),
                urgentProjects = listOf(
                    ProjectListUiModel(
                        projectId = 1L,
                        title = "Sunset shoot",
                        categoryName = "Nature",
                        isFavorite = true,
                        isUrgent = true,
                        dueDateMillis = System.currentTimeMillis() + 86400000L * 7 // 1 week from now
                    ),
                    ProjectListUiModel(
                        projectId = 2L,
                        title = "Portrait session",
                        categoryName = "Work",
                        isFavorite = false,
                        isUrgent = false,
                        currentSpend = 270.0,
                        projectBudget = 200.0,
                        dueDateMillis = System.currentTimeMillis() + 86400000L * 3 // 3 days from now
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    PhotoDoTheme {
        HomeScreen(
            uiState = HomeUiState(isLoading = true),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    PhotoDoTheme {
        HomeScreen(
            uiState = HomeUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}
