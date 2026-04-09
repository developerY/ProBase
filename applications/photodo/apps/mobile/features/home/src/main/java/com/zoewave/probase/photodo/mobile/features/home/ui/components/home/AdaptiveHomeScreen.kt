package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass.Companion.calculateFromSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import androidx.compose.ui.unit.DpSize
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
    navTo: (PhotoTodoRoute) -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val isExpanded = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    if (!isExpanded) {
        HomeScreen(
            uiState = uiState,
            onEvent = onEvent,
            navTo = navTo,
            modifier = modifier
        )
    } else {
        val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()

        // Shared dialog/fab states for the adaptive view
        var showAddCategoryDialog by rememberSaveable { mutableStateOf(false) }
        var categoryToDelete by remember { mutableStateOf<CategoryOverviewUiModel?>(null) }
        var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

        Scaffold(
            modifier = modifier.fillMaxSize(),
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
                    }
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
                                OverviewSummaryCard(categories = uiState.categories)
                            }

                            item {
                                Text(
                                    stringResource(R.string.applications_photodo_apps_mobile_features_home_jump_back_in),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

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
                },
                detailPane = {
                    // RIGHT PANE: The Directory (Full Category Grid)
                    HomeOverviewContent(
                        uiState = uiState,
                        onEvent = onEvent,
                        navTo = { route -> if (route != null) navTo(route) },
                        onDeleteClicked = { categoryToDelete = it },
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        showSummaryHeader = false // 🚀 Donut chart is already in the left pane!
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        HomeOverviewDialogs(
            showAddCategoryDialog = showAddCategoryDialog,
            onDismissAddCategory = { showAddCategoryDialog = false },
            uiState = uiState,
            categoryToDelete = categoryToDelete,
            onDismissDeleteConfirmation = { categoryToDelete = null },
            onEvent = onEvent
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun AdaptiveHomeScreenPreviewCompact() {
    PhotoDoTheme {
        AdaptiveHomeScreen(
            uiState = HomeUiState(
                categories = listOf(
                    CategoryOverviewUiModel(1L, "Work", 10, 5, 0.5f),
                    CategoryOverviewUiModel(2L, "Personal", 5, 2, 0.4f)
                ),
                urgentProjects = listOf(
                    ProjectListUiModel(1L, "Project A", "Work", isUrgent = true),
                    ProjectListUiModel(2L, "Project B", "Personal", isUrgent = true)
                )
            ),
            onEvent = {},
            navTo = {},
            windowSizeClass = calculateFromSize(DpSize(400.dp, 800.dp))
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
                    CategoryOverviewUiModel(1L, "Work", 10, 5, 0.5f),
                    CategoryOverviewUiModel(2L, "Personal", 5, 2, 0.4f)
                ),
                urgentProjects = listOf(
                    ProjectListUiModel(1L, "Project A", "Work", isUrgent = true),
                    ProjectListUiModel(2L, "Project B", "Personal", isUrgent = true)
                )
            ),
            onEvent = {},
            navTo = {},
            windowSizeClass = calculateFromSize(DpSize(1000.dp, 800.dp))
        )
    }
}
