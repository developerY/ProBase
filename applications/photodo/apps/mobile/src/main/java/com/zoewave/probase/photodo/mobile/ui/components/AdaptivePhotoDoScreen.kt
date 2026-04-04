package com.zoewave.probase.photodo.mobile.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.launch

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
    var currentState by rememberSaveable {
        mutableStateOf(
            if (initialProjectId != null) PhotoDoFoldableState.PROJECTS_AND_TASKS
            else PhotoDoFoldableState.CATEGORY_AND_PROJECTS
        )
    }
    var selectedCategoryId by rememberSaveable { mutableStateOf(initialCategoryId) }
    var selectedProjectId by rememberSaveable { mutableStateOf(initialProjectId) }

    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val scope = rememberCoroutineScope()
    val paneContrast = LocalPaneContrast.current

    // --- VIEW MODELS & DATA FETCHING (Moved to top level for stability!) ---
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val tasksViewModel = hiltViewModel<TasksViewModel>()
    val detailViewModel = hiltViewModel<TaskDetailViewModel>()

    // Sync categories and projects viewmodels with our top-level remembered state
    LaunchedEffect(selectedCategoryId) {
        selectedCategoryId?.let { tasksViewModel.setCategoryId(it) }
    }
    LaunchedEffect(selectedProjectId) {
        selectedProjectId?.let { detailViewModel.loadTaskDetails(it) }
    }

    // 🚀 NEW: Ensure we navigate to the Detail pane if starting with a project ID
    LaunchedEffect(Unit) {
        if (initialProjectId != null) {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
        }
    }

    // Collect States
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val tasksUiState by tasksViewModel.uiState.collectAsStateWithLifecycle()
    val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()

    // 2. Fixed Back Button Logic for Compact Mode
    BackHandler(enabled = navigator.canNavigateBack() || currentState == PhotoDoFoldableState.PROJECTS_AND_TASKS) {
        if (navigator.canNavigateBack()) {
            // If viewing Detail pane in compact mode, slide back to List pane
            scope.launch {
                navigator.navigateBack()
            }
        } else {
            // If on List pane, shift the custom state back
            currentState = PhotoDoFoldableState.CATEGORY_AND_PROJECTS
        }
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            val listBackground = if (paneContrast == "TINTED") MaterialTheme.colorScheme.surfaceContainerLow
            else MaterialTheme.colorScheme.surface

            if (currentState == PhotoDoFoldableState.CATEGORY_AND_PROJECTS) {
                // Left Pane: Categories
                HomeOverviewScreen(
                    uiState = homeUiState,
                    onEvent = homeViewModel::onEvent,
                    navTo = { route: PhotoTodoRoute? ->
                        when (route) {
                            is PhotoTodoRoute.TasksList -> {
                                selectedCategoryId = route.categoryId
                                // 🚀 Update ViewModel directly to minimize race conditions
                                route.categoryId?.let { tasksViewModel.setCategoryId(it) }
                                currentState = PhotoDoFoldableState.PROJECTS_AND_TASKS
                            }
                            is PhotoTodoRoute.TaskDetail -> {
                                selectedProjectId = route.projectId
                                // 🚀 Update ViewModel directly to minimize race conditions
                                detailViewModel.loadTaskDetails(route.projectId)
                                currentState = PhotoDoFoldableState.PROJECTS_AND_TASKS
                                // 4. CRITICAL: Tell the navigator to slide the detail pane in Compact mode!
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                                }
                            }
                            else -> route?.let(navTo)
                        }
                    },
                    modifier = Modifier.fillMaxSize().background(listBackground)
                )
            } else {
                // Left Pane: Projects
                TasksListScreen(
                    uiState = tasksUiState,
                    onEvent = tasksViewModel::onEvent,
                    navTo = { route: PhotoTodoRoute? ->
                        when (route) {
                            null -> currentState = PhotoDoFoldableState.CATEGORY_AND_PROJECTS
                            is PhotoTodoRoute.TaskDetail -> {
                                selectedProjectId = route.projectId
                                // 🚀 Update ViewModel directly to minimize race conditions
                                detailViewModel.loadTaskDetails(route.projectId)
                                // 4. CRITICAL: Tell the navigator to slide the detail pane in Compact mode!
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                                }
                            }
                            else -> route.let(navTo)
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
                TasksListScreen(
                    uiState = tasksUiState,
                    onEvent = tasksViewModel::onEvent,
                    navTo = { route: PhotoTodoRoute? ->
                        when (route) {
                            null -> currentState = PhotoDoFoldableState.CATEGORY_AND_PROJECTS
                            is PhotoTodoRoute.TaskDetail -> {
                                selectedProjectId = route.projectId
                                // 🚀 Update ViewModel directly to minimize race conditions
                                detailViewModel.loadTaskDetails(route.projectId)
                                currentState = PhotoDoFoldableState.PROJECTS_AND_TASKS
                                // 4. CRITICAL: Tell the navigator to slide the detail pane in Compact mode!
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                                }
                            }
                            else -> route.let(navTo)
                        }
                    },
                    modifier = Modifier.fillMaxSize().background(detailBackground)
                )
            } else {
                // Right Pane: Tasks (Detail)
                TaskDetailScreen(
                    uiState = detailUiState,
                    onEvent = detailViewModel::onEvent,
                    navTo = { route: PhotoTodoRoute? ->
                        if (route == null) {
                            if (navigator.canNavigateBack()) {
                                scope.launch {
                                    navigator.navigateBack()
                                }
                            } else {
                                currentState = PhotoDoFoldableState.CATEGORY_AND_PROJECTS
                            }
                        } else {
                            route.let(navTo)
                        }
                    },
                    modifier = Modifier.fillMaxSize().background(detailBackground)
                )
            }
        },
        modifier = modifier.fillMaxSize()
    )
}