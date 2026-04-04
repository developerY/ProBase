package com.zoewave.probase.photodo.mobile.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
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
import com.zoewave.probase.photodo.mobile.core.ui.theme.LocalPaneContrast
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
    navTo: (PhotoTodoRoute) -> Unit,
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

    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()

    // Back button logic: Shift panes back instead of exiting
    BackHandler(enabled = currentState == PhotoDoFoldableState.PROJECTS_AND_TASKS) {
        currentState = PhotoDoFoldableState.CATEGORY_AND_PROJECTS
    }

    val paneContrast = LocalPaneContrast.current

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            val listBackground = if (paneContrast == "TINTED") MaterialTheme.colorScheme.surfaceContainerLow
                                 else MaterialTheme.colorScheme.surface
            if (currentState == PhotoDoFoldableState.CATEGORY_AND_PROJECTS) {
                // Left Pane: Categories
                val homeViewModel = hiltViewModel<HomeViewModel>()
                val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
                HomeOverviewScreen(
                    uiState = homeUiState,
                    onEvent = homeViewModel::onEvent,
                    navTo = { route: PhotoTodoRoute? ->
                        when (route) {
                            null -> { /* Handle back if needed, but categories is root here */ }
                            is PhotoTodoRoute.TasksList -> {
                                selectedCategoryId = route.categoryId
                                currentState = PhotoDoFoldableState.PROJECTS_AND_TASKS
                            }
                            else -> route.let(navTo) // 🚀 Delegate other routes (like Camera)
                        }
                    },
                    modifier = Modifier.fillMaxSize().background(listBackground)
                )
            } else {
                // Left Pane: Projects
                val tasksViewModel = hiltViewModel<TasksViewModel>()
                selectedCategoryId?.let { tasksViewModel.setCategoryId(it) }
                val tasksUiState by tasksViewModel.uiState.collectAsStateWithLifecycle()
                TasksListScreen(
                    uiState = tasksUiState,
                    onEvent = tasksViewModel::onEvent,
                    navTo = { route: PhotoTodoRoute? ->
                        when {
                            route == null -> {
                                currentState = PhotoDoFoldableState.CATEGORY_AND_PROJECTS
                            }
                            route is PhotoTodoRoute.TaskDetail -> {
                                selectedProjectId = route.projectId
                                // Stay in current state, but update detail pane
                            }
                            else -> route.let(navTo) // 🚀 Delegate other routes
                        }
                    },
                    modifier = Modifier.fillMaxSize().background(listBackground)
                )
            }
        },
        detailPane = {
            val detailBackground = MaterialTheme.colorScheme.surface
            if (currentState == PhotoDoFoldableState.CATEGORY_AND_PROJECTS) {
                // Right Pane: Projects
                val tasksViewModel = hiltViewModel<TasksViewModel>()
                selectedCategoryId?.let { tasksViewModel.setCategoryId(it) }
                val tasksUiState by tasksViewModel.uiState.collectAsStateWithLifecycle()
                TasksListScreen(
                    uiState = tasksUiState,
                    onEvent = tasksViewModel::onEvent,
                    navTo = { route: PhotoTodoRoute? ->
                        when {
                            route == null -> {
                                currentState = PhotoDoFoldableState.CATEGORY_AND_PROJECTS
                            }
                            route is PhotoTodoRoute.TaskDetail -> {
                                selectedProjectId = route.projectId
                                currentState = PhotoDoFoldableState.PROJECTS_AND_TASKS
                            }
                            else -> route.let(navTo) // 🚀 Delegate other routes
                        }
                    },
                    modifier = Modifier.fillMaxSize().background(detailBackground)
                )
            } else {
                // Right Pane: Tasks (Detail)
                val detailViewModel = hiltViewModel<TaskDetailViewModel>()
                selectedProjectId?.let { detailViewModel.loadTaskDetails(it) }
                val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
                TaskDetailScreen(
                    uiState = detailUiState,
                    onEvent = detailViewModel::onEvent,
                    navTo = { route: PhotoTodoRoute? ->
                        if (route == null) {
                            currentState = PhotoDoFoldableState.CATEGORY_AND_PROJECTS
                        } else {
                            route.let(navTo) // 🚀 Delegate other routes (like Camera!)
                        }
                    },
                    modifier = Modifier.fillMaxSize().background(detailBackground)
                )
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
