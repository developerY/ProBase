package com.zoewave.probase.photodo.mobile.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.HomeOverviewScreen
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.HomeViewModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksViewModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.components.TasksListScreen
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.TaskDetailScreen
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.TaskDetailViewModel
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute

enum class PhotoDoFoldableState {
    CATEGORY_AND_PROJECTS,
    PROJECTS_AND_TASKS
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AdaptivePhotoDoScreen(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    initialCategoryId: Long? = null,
    initialProjectId: Long? = null
) {
    val isExpanded = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    
    if (!isExpanded) {
        return
    }

    var currentState by remember { 
        mutableStateOf(
            if (initialProjectId != null) PhotoDoFoldableState.PROJECTS_AND_TASKS 
            else PhotoDoFoldableState.CATEGORY_AND_PROJECTS
        ) 
    }
    var selectedCategoryId by remember { mutableStateOf(initialCategoryId) }
    var selectedProjectId by remember { mutableStateOf(initialProjectId) }

    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()

    // Back button logic: Shift panes back instead of exiting
    BackHandler(enabled = currentState == PhotoDoFoldableState.PROJECTS_AND_TASKS) {
        currentState = PhotoDoFoldableState.CATEGORY_AND_PROJECTS
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            if (currentState == PhotoDoFoldableState.CATEGORY_AND_PROJECTS) {
                // Left Pane: Categories
                val homeViewModel: HomeViewModel = hiltViewModel()
                val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
                HomeOverviewScreen(
                    uiState = homeUiState,
                    onEvent = homeViewModel::onEvent,
                    navTo = { route ->
                        when (route) {
                            is PhotoTodoRoute.TasksList -> {
                                selectedCategoryId = route.categoryId
                                // In dual-pane, clicking category updates the right pane
                            }
                            else -> { /* Handle other nav if needed */ }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Left Pane: Projects
                val tasksViewModel: TasksViewModel = hiltViewModel()
                selectedCategoryId?.let { tasksViewModel.setCategoryId(it) }
                val tasksUiState by tasksViewModel.uiState.collectAsStateWithLifecycle()
                TasksListScreen(
                    uiState = tasksUiState,
                    onEvent = tasksViewModel::onEvent,
                    navTo = { route ->
                        if (route == null) {
                            currentState = PhotoDoFoldableState.CATEGORY_AND_PROJECTS
                        } else if (route is PhotoTodoRoute.TaskDetail) {
                            selectedProjectId = route.projectId
                            // Stay in current state, but update detail pane
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        detailPane = {
            if (currentState == PhotoDoFoldableState.CATEGORY_AND_PROJECTS) {
                // Right Pane: Projects
                val tasksViewModel: TasksViewModel = hiltViewModel()
                selectedCategoryId?.let { tasksViewModel.setCategoryId(it) }
                val tasksUiState by tasksViewModel.uiState.collectAsStateWithLifecycle()
                TasksListScreen(
                    uiState = tasksUiState,
                    onEvent = tasksViewModel::onEvent,
                    navTo = { route ->
                        if (route is PhotoTodoRoute.TaskDetail) {
                            selectedProjectId = route.projectId
                            currentState = PhotoDoFoldableState.PROJECTS_AND_TASKS
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Right Pane: Tasks (Detail)
                val detailViewModel: TaskDetailViewModel = hiltViewModel()
                selectedProjectId?.let { detailViewModel.loadTaskDetails(it) }
                val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
                TaskDetailScreen(
                    uiState = detailUiState,
                    onEvent = detailViewModel::onEvent,
                    navTo = { route ->
                        if (route == null) {
                            currentState = PhotoDoFoldableState.CATEGORY_AND_PROJECTS
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
