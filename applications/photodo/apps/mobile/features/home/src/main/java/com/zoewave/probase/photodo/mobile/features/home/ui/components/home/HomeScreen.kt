package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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

            item {            // --- 3. View All Categories Button (Preserved) ---
                Button(
                    onClick = { navTo(PhotoTodoRoute.CategoryGrid) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.applications_photodo_apps_mobile_features_home_view_all_categories))
                }
            }

            item {
                // --- 4. Jump Back In Section (Preserved Urgent/Fav List) ---
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Text(
                            stringResource(R.string.applications_photodo_apps_mobile_features_home_jump_back_in),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    TaskSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { onEvent(HomeEvent.OnSearchQueryChanged(it)) }
                    )
                }
            }

            if (uiState.searchQuery.isNotBlank()) {
                item {
                    TaskSearchResultsList(
                        results = uiState.taskSearchResults,
                        navTo = navTo
                    )
                }
            } else {
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
                                navTo = navTo      // Pass the channel down
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
