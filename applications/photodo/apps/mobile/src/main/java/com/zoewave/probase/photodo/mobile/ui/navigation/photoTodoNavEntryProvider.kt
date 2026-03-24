package com.zoewave.probase.photodo.mobile.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import com.zoewave.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.photodo.model.navigation.PhotoTodoRoute.Settings
import com.zoewave.photodo.model.navigation.PhotoTodoRoute.TaskDetail
import com.zoewave.photodo.model.navigation.PhotoTodoRoute.TasksList
import com.zoewave.probase.features.camera.ui.CameraUIRoute
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.HomeViewModel
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.HomeOverviewScreen
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.HomeScreen
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksViewModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.components.TasksListScreen
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.TaskDetailScreen
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.TaskDetailViewModel

fun photoTodoNavEntryProvider(
    key: PhotoTodoRoute,
    navigateTo: (PhotoTodoRoute) -> Unit,
    navigateBack: () -> Unit,
    // Un-comment this if you need to manually tell your BottomBar to switch tabs!
    // onTabSelected: (PhotoTodoRoute) -> Unit = {}
): NavEntry<PhotoTodoRoute> {

    return NavEntry(key) {
        when (key) {

            // --- TAB 1: DASHBOARD ---
            // ... inside your when(key) block:

            is PhotoTodoRoute.Home -> {
                val viewModel: HomeViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                HomeScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    navTo = { route ->
                        if (route == null) navigateBack() else navigateTo(route)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            is PhotoTodoRoute.CategoryGrid -> {
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

            // --- TAB 2: WORKSPACE ---
            is TasksList -> {
                val viewModel: TasksViewModel = hiltViewModel()
                LaunchedEffect(key.categoryId) {
                    viewModel.setCategoryId(key.categoryId)
                }
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                TasksListScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    navTo = { route ->
                        if (route == null) navigateBack() else navigateTo(route)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // --- DEEP DETAIL SCREEN ---
            is TaskDetail -> {
                val viewModel: TaskDetailViewModel = hiltViewModel()
                LaunchedEffect(key.listId) {
                    viewModel.loadTaskDetails(key.listId)
                }
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                TaskDetailScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    navTo = { route ->
                        if (route == null) navigateBack() else navigateTo(route)
                    },
                    modifier = Modifier.fillMaxSize()
                )
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
                            resultHandler.execute(listId = key.listId, uri = uriString)
                        }

                        navigateBack()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // --- TAB 3: SETTINGS ---
            is Settings -> {
                Text("Settings")
            }

            is PhotoTodoRoute.CategoryTasks -> TODO()
        }
    }
}
