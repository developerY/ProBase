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
import com.zoewave.probase.features.ai.capture.ui.SmartCaptureUiRoute
import com.zoewave.probase.features.camera.ui.CameraUIRoute
import com.zoewave.probase.photodo.features.camera.ui.CameraResultHandler
import com.zoewave.probase.photodo.features.camera.ui.SavePhotoViewModel
import com.zoewave.probase.photodo.features.camera.ui.components.SavePhotoBottomSheet
import com.zoewave.probase.photodo.features.smartadvice.ui.SmartAdviceUiRoute
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.HomeOverviewScreen
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.AdaptiveHomeScreen
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.HomeScreen
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.HomeViewModel
import com.zoewave.probase.photodo.mobile.features.settings.ui.SettingsUiRoute
import com.zoewave.probase.photodo.mobile.features.settings.ui.SettingsViewModel
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
            is PhotoTodoRoute.Home -> {
                val viewModel: HomeViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                
                if (isExpanded) {
                    AdaptiveHomeScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        navTo = { route ->
                            if (route == null) navigateBack() else navigateTo(route)
                        }
                    )
                } else {
                    HomeScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        navTo = { route ->
                            if (route == null) navigateBack() else navigateTo(route)
                        }
                    )
                }
            }

            is PhotoTodoRoute.CategoryGrid -> {
                if (isExpanded) {
                    AdaptivePhotoDoScreen(
                        navTo = navigateTo
                    )
                } else {
                    val viewModel: HomeViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    HomeOverviewScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        navTo = { route ->
                            if (route == null) navigateBack() else navigateTo(route)
                        }
                    )
                }
            }

            // --- TAB 2: WORKSPACE ---
            is TasksList -> {
                if (isExpanded) {
                    AdaptivePhotoDoScreen(
                        navTo = navigateTo,
                        initialCategoryId = key.categoryId
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
                                is TasksSideEffect.ProjectCreated -> {
                                    navigateTo(TaskDetail(effect.projectId, effect.title))
                                }
                            }
                        }
                    }

                    TasksListScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        navTo = { route ->
                            if (route == null) navigateBack() else navigateTo(route)
                        }
                    )
                }
            }

            // --- DEEP DETAIL SCREEN ---
            is TaskDetail -> {
                if (isExpanded) {
                    AdaptivePhotoDoScreen(
                        navTo = navigateTo,
                        initialProjectId = key.projectId
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
                                is TasksSideEffect.ProjectCreated -> {
                                    navigateTo(TaskDetail(effect.projectId, effect.title))
                                }
                            }
                        }
                    }

                    TaskDetailScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        navTo = { route ->
                            if (route == null) navigateBack() else navigateTo(route)
                        }
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
                                navigateTo(PhotoTodoRoute.SmartCapture(photoUri = uriString))
                            } else {
                                // standard flow
                                navigateBack()
                                navigateTo(PhotoTodoRoute.SavePhoto(photoUri = uriString))
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
                        // 🚀 NEW: Navigate to the Smart Form (SavePhoto) with the AI details!
                        navigateBack() // Pop SmartCapture
                        navigateTo(PhotoTodoRoute.SavePhoto(photoUri = key.photoUri, prefilledAiDraft = draft))
                    },
                    onRetakeRequest = {
                        navigateBack() // Pop SmartCapture
                        navigateTo(PhotoTodoRoute.Camera())
                    },
                    onDismiss = navigateBack
                )
            }

            is PhotoTodoRoute.SavePhoto -> {
                val viewModel: SavePhotoViewModel = hiltViewModel()
                
                LaunchedEffect(key.photoUri, key.prefilledAiDraft) {
                    Log.d("SavePhotoDebug", "NavEntry: Calling setInitialData for uri: ${key.photoUri}")
                    viewModel.setInitialData(key.photoUri, key.prefilledAiDraft)
                }
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // 🚀 NEW: Automatic navigation back to Home dashboard after success
                LaunchedEffect(uiState.isSaved) {
                    Log.d("SavePhotoDebug", "NavEntry: uiState.isSaved changed to: ${uiState.isSaved}")
                    if (uiState.isSaved) {
                        val id = uiState.savedProjectId
                        val title = uiState.savedProjectTitle
                        Log.d("SavePhotoDebug", "NavEntry: Auto-navigating to Detail for ID: $id")
                        if (id != null && title != null) {
                            navigateBack()
                            navigateTo(PhotoTodoRoute.TaskDetail(id, title))
                        } else {
                            navigateBack()
                        }
                    }
                }

                SavePhotoBottomSheet(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    navTo = { route ->
                        if (route == null) navigateBack() else navigateTo(route)
                    }
                )
            }

            is PhotoTodoRoute.SmartAdvice -> {
                SmartAdviceUiRoute(
                    projectId = key.projectId,
                    onDismiss = navigateBack
                )
            }

            // --- TAB 3: SETTINGS ---
            is Settings -> {
                val viewModel: SettingsViewModel = hiltViewModel()
                LaunchedEffect(key.title) {
                    viewModel.setInitialExpandedKey(key.title)
                }

                SettingsUiRoute(
                    navTo = { route ->
                        if (route == null) navigateBack() else navigateTo(route)
                    },
                    viewModel = viewModel
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
                    navTo = { route ->
                        if (route == null) navigateBack() else navigateTo(route)
                    }
                )
            }
        }
    }
}
