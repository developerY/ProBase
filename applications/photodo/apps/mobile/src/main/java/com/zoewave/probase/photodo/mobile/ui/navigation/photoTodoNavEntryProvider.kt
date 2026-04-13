package com.zoewave.probase.photodo.mobile.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.features.camera.ui.CameraUIRoute
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.HomeOverviewScreen
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.AdaptiveHomeScreen
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.HomeViewModel
import com.zoewave.probase.photodo.mobile.features.settings.ui.SettingsUiRoute
import com.zoewave.probase.features.smartcapture.ui.SmartCaptureUiRoute
import com.zoewave.probase.photodo.features.camera.ui.CameraResultHandler
import com.zoewave.probase.photodo.features.camera.ui.SavePhotoViewModel
import com.zoewave.probase.photodo.features.camera.ui.components.SavePhotoBottomSheet
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksSideEffect
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksViewModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.components.TasksListScreen
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.TaskDetailScreen
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.TaskDetailViewModel
import com.zoewave.probase.photodo.mobile.ui.components.AdaptivePhotoDoScreen
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute.Settings
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute.TaskDetail
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute.TasksList

fun photoTodoNavEntryProvider(
    key: PhotoTodoRoute,
    windowSizeClass: WindowSizeClass,
    isAiEnabled: Boolean,
    navigateTo: (PhotoTodoRoute) -> Unit,
    navigateBack: () -> Unit,
): NavEntry<PhotoTodoRoute> {

    return NavEntry(key) {
        val isExpanded = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

        when (key) {

            // --- TAB 1: DASHBOARD ---
            // ... inside your when(key) block:

            is PhotoTodoRoute.Home -> {
                val viewModel: HomeViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                AdaptiveHomeScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    navTo = { route ->
                        if (route == null) navigateBack() else navigateTo(route)
                    },
                    windowSizeClass = windowSizeClass,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is PhotoTodoRoute.CategoryGrid -> {
                if (isExpanded) {
                    AdaptivePhotoDoScreen(
                        windowSizeClass = windowSizeClass,
                        navTo = navigateTo,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val viewModel: HomeViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    HomeOverviewScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        navTo = { route ->
                            if (route == null) navigateBack() else navigateTo(route)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // --- TAB 2: WORKSPACE ---
            is TasksList -> {
                if (isExpanded) {
                    AdaptivePhotoDoScreen(
                        windowSizeClass = windowSizeClass,
                        navTo = navigateTo,
                        initialCategoryId = key.categoryId,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val viewModel: TasksViewModel = hiltViewModel()
                    
                    // 🚀 NEW: Ensure ViewModel is updated with categoryId in compact mode
                    LaunchedEffect(key.categoryId) {
                        key.categoryId?.let { viewModel.setCategoryId(it) }
                    }

                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    // 🚀 NEW: Auto-navigate Home if no categories exist
                    LaunchedEffect(uiState.isNoCategoriesYet) {
                        if (uiState.isNoCategoriesYet) {
                            navigateBack()
                        }
                    }

                    LaunchedEffect(viewModel.effects) {
                        viewModel.effects.collect { effect ->
                            when (effect) {
                                TasksSideEffect.NavigateBack -> navigateBack()
                            }
                        }
                    }

                    TasksListScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        navTo = { route ->
                            if (route == null) navigateBack() else navigateTo(route)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // --- DEEP DETAIL SCREEN ---
            is TaskDetail -> {
                if (isExpanded) {
                    AdaptivePhotoDoScreen(
                        windowSizeClass = windowSizeClass,
                        navTo = navigateTo,
                        initialProjectId = key.projectId,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val viewModel: TaskDetailViewModel = hiltViewModel()
                    
                    // 🚀 NEW: Ensure ViewModel is updated with projectId in compact mode
                    LaunchedEffect(key.projectId) {
                        viewModel.loadTaskDetails(key.projectId)
                    }

                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(viewModel.effects) {
                        viewModel.effects.collect { effect ->
                            when (effect) {
                                TasksSideEffect.NavigateBack -> navigateBack()
                            }
                        }
                    }

                    TaskDetailScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        navTo = { route ->
                            if (route == null) navigateBack() else navigateTo(route)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }



            is PhotoTodoRoute.Camera -> {
                // Grab our stateless Action Handler
                val resultHandler: CameraResultHandler = hiltViewModel()
                // We launch the isolated Camera UI from your feature module
                CameraUIRoute(
                    navTo = { routeString ->
                        Log.d("CameraDebug", "5. Host App received navTo string: $routeString")

                        if (routeString.startsWith("result_ok:")) {
                            val uriString = routeString.removePrefix("result_ok:")
                            Log.d("CameraDebug", "G. Host App extracted URI, executing save UseCase...")
                            val projectId = key.projectId
                            if (projectId != null) {
                                resultHandler.execute(projectId = projectId, uri = uriString)
                                navigateBack()
                            } else if (isAiEnabled) {
                                // 🚀 NEW: Tier 1 (Cloud) enabled, go to SmartCapture review first!
                                navigateBack() // Pop camera
                                navigateTo(PhotoTodoRoute.SmartCapture(uriString))
                            } else {
                                // standard flow
                                navigateBack()
                                navigateTo(PhotoTodoRoute.SavePhoto(uriString))
                            }
                        } else {
                            navigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            is PhotoTodoRoute.SmartCapture -> {
                SmartCaptureUiRoute(
                    initialPhotoUri = key.photoUri,
                    onCaptureComplete = { draft ->
                        // Navigate to Workspace/TasksList and prefill!
                        navigateBack() // Pop SmartCapture
                        navigateTo(PhotoTodoRoute.TasksList(prefilledAiDraft = draft))
                    },
                    onDismiss = navigateBack
                )
            }

            is PhotoTodoRoute.SavePhoto -> {
                val viewModel: SavePhotoViewModel = hiltViewModel()
                LaunchedEffect(key.photoUri) {
                    viewModel.setPhotoUri(key.photoUri)
                }
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // 🚀 NEW: Automatic navigation back to Home dashboard after success
                if (uiState.isSaved) {
                    val savedProjectId = uiState.savedProjectId
                    val savedProjectTitle = uiState.savedProjectTitle
                    LaunchedEffect(Unit) {
                        if (savedProjectId != null && savedProjectTitle != null) {
                            // First go back to pop SavePhoto, then navigate to TaskDetail
                            navigateBack()
                            navigateTo(PhotoTodoRoute.TaskDetail(savedProjectId, savedProjectTitle))
                        } else {
                            navigateBack()
                        }
                    }
                }

                SavePhotoBottomSheet(
                    uiState = uiState,
                    onCategorySelected = viewModel::selectCategory,
                    onProjectSelected = viewModel::selectProject,
                    onNewCategoryNameChanged = viewModel::setNewCategoryName,
                    onNewProjectNameChanged = viewModel::setNewProjectName,
                    onAddCategoryClicked = viewModel::createAndSelectCategory,
                    onAddProjectClicked = viewModel::createAndSelectProject,
                    onSaveClicked = viewModel::savePhoto,
                    onDismiss = navigateBack
                )
            }

            // --- TAB 3: SETTINGS ---
            is Settings -> {
                SettingsUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    initialCardKeyToExpand = key.title,
                    navTo = { route ->
                        if (route == null) navigateBack() else navigateTo(route)
                    },
                )
            }

            is PhotoTodoRoute.CategoryTasks -> {
                val viewModel: TasksViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // 🚀 NEW: Auto-navigate Home if no categories exist
                LaunchedEffect(uiState.isNoCategoriesYet) {
                    if (uiState.isNoCategoriesYet) {
                        navigateBack()
                    }
                }

                TasksListScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    navTo = { if (it == null) navigateBack() else navigateTo(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
