package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.LocalPaneContrast
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.home.R
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.HomeOverviewContent
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.HomeOverviewDialogs
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.HomeOverviewFab
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AdaptiveHomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            HomeOverviewFab(
                fabMenuExpanded = uiState.fabMenuExpanded,
                onFabToggle = { onEvent(HomeEvent.OnFabMenuToggle(it)) },
                onAddCategoryClick = {
                    onEvent(HomeEvent.OnFabMenuToggle(false))
                    onEvent(HomeEvent.OnShowAddCategoryDialog(true))
                },
                onHomeProjectClick = {
                    onEvent(HomeEvent.OnFabMenuToggle(false))
                    onEvent(HomeEvent.OnAddQuickProjectClicked("Home"))
                },
                onCameraClick = {
                    onEvent(HomeEvent.OnFabMenuToggle(false))
                    navTo(PhotoTodoRoute.Camera(projectId = null))
                },
                onSmartCaptureClick = {
                    onEvent(HomeEvent.OnFabMenuToggle(false))
                    navTo(PhotoTodoRoute.SmartCapture())
                },
                isAiEnabled = uiState.isAiEnabled,
                animationsEnabled = uiState.animationsEnabled
            )
        }
    ) { paddingValues ->
        val paneContrast = LocalPaneContrast.current
        
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                // LEFT PANE: The Dashboard (Summary + Jump Back In)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (paneContrast == "TINTED") MaterialTheme.colorScheme.surfaceContainerLow
                            else MaterialTheme.colorScheme.surface
                        )
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isLoading) {
                        item { CircularProgressIndicator() }
                    } else if (uiState.isEmpty) {
                        item {
                            Text(
                                stringResource(R.string.applications_photodo_apps_mobile_features_home_no_data_seed),
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                        } else {
                            item {
                                OverviewSummaryCard(
                                    categories = uiState.categories,
                                    isExpanded = uiState.isCategoriesSummaryExpanded,
                                    onToggleExpand = { onEvent(HomeEvent.OnToggleCategoriesSummary) },
                                    onViewAllClick = { navTo(PhotoTodoRoute.CategoryGrid) },
                                    animationsEnabled = uiState.animationsEnabled
                                )
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
                                    query = uiState.taskSearchQuery,
                                    onQueryChange = { onEvent(HomeEvent.OnTaskSearchQueryChanged(it)) }
                                )
                            }
                        }

                        if (uiState.taskSearchQuery.isNotBlank()) {
                            item {
                                TaskSearchResultsList(
                                    results = uiState.taskSearchResults,
                                    navTo = navTo
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
                                items(items = uiState.urgentProjects, key = { it.projectId }) { project ->
                                    HomeProjectRow(
                                        project = project,
                                        onEvent = onEvent,
                                        navTo = navTo
                                    )
                                }
                            }
                        }
                    }
                }
            },
            detailPane = {
                // RIGHT PANE: The Directory (Full Category Grid)
                HomeOverviewContent(
                    uiState = uiState,
                    onEvent = onEvent,
                    navTo = { route -> if (route != null) navTo(route) },
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    showSummaryHeader = false
                )
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    HomeOverviewDialogs(
        showAddCategorySheet = uiState.showAddCategoryDialog,
        onDismissAddCategory = { onEvent(HomeEvent.OnShowAddCategoryDialog(false)) },
        uiState = uiState,
        categoryToDelete = uiState.categoryToDelete,
        onDismissDeleteConfirmation = { onEvent(HomeEvent.OnCategoryToDeleteChanged(null)) },
        onEvent = onEvent
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun AdaptiveHomeScreenPreviewCompact() {
    PhotoDoTheme {
        AdaptiveHomeScreen(
            uiState = HomeUiState(
                categories = listOf(
                    CategoryOverviewUiModel(1L, "Work", 3, 10, 5, 0.5f),
                    CategoryOverviewUiModel(2L, "Personal", 2, 5, 2, 0.4f)
                ),
                urgentProjects = listOf(
                    ProjectListUiModel(1L, "Project A", "Work", isUrgent = true),
                    ProjectListUiModel(2L, "Project B", "Personal", isUrgent = true)
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, widthDp = 1000, heightDp = 800)
@Composable
fun AdaptiveHomeScreenPreviewExpanded() {
    PhotoDoTheme {
        AdaptiveHomeScreen(
            uiState = HomeUiState(
                categories = listOf(
                    CategoryOverviewUiModel(1L, "Work", 3, 10, 5, 0.5f),
                    CategoryOverviewUiModel(2L, "Personal", 2, 5, 2, 0.4f)
                ),
                urgentProjects = listOf(
                    ProjectListUiModel(1L, "Project A", "Work", isUrgent = true),
                    ProjectListUiModel(2L, "Project B", "Personal", isUrgent = true)
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
