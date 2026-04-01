package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.home.R
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.HomeOverviewContent
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.HomeOverviewDialogs
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.HomeOverviewFab
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.HomeOverviewScreen
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import components.home.CategoryQuickJumpRow

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
        var showQuickTemplateBottomSheet by rememberSaveable { mutableStateOf(false) }
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
                    onQuickProjectClick = {
                        fabMenuExpanded = false
                        showQuickTemplateBottomSheet = true
                    },
                    onCameraClick = {
                        fabMenuExpanded = false
                        navTo(PhotoTodoRoute.Camera(projectId = null))
                    }
                )
            }
        ) { paddingValues ->
            ListDetailPaneScaffold(
                directive = navigator.scaffoldDirective,
                value = navigator.scaffoldValue,
                listPane = {
                    // LEFT PANE: The Dashboard (Summary + Jump Back In)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uiState is HomeUiState.Success) {
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
                        } else if (uiState is HomeUiState.Loading) {
                            item { CircularProgressIndicator() }
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
            showQuickTemplateBottomSheet = showQuickTemplateBottomSheet,
            onDismissQuickTemplate = { showQuickTemplateBottomSheet = false },
            categoryToDelete = categoryToDelete,
            onDismissDeleteConfirmation = { categoryToDelete = null },
            onEvent = onEvent
        )
    }
}
