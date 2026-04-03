package com.zoewave.probase.photodo.wear.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.wear.features.home.HomeRoute
import com.zoewave.probase.photodo.wear.features.home.HomeViewModel
import com.zoewave.probase.photodo.wear.features.project.ProjectListRoute
import com.zoewave.probase.photodo.wear.features.project.ProjectListViewModel
import com.zoewave.probase.photodo.wear.features.task.TaskDetailRoute
import com.zoewave.probase.photodo.wear.features.task.TaskDetailViewModel

fun photoDoWearNavEntryProvider(
    key: PhotoTodoRoute,
    navigateTo: (PhotoTodoRoute) -> Unit,
    onBack: () -> Unit
): NavEntry<PhotoTodoRoute> {
    return NavEntry(key) {
        when (key) {
            PhotoTodoRoute.Home -> {
                val viewModel: HomeViewModel = hiltViewModel()
                HomeRoute(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToCategory = { categoryId, categoryName ->
                        navigateTo(PhotoTodoRoute.TasksList(categoryId = categoryId, categoryName = categoryName))
                    }
                )
            }
            is PhotoTodoRoute.TasksList -> {
                val viewModel: ProjectListViewModel = hiltViewModel()
                ProjectListRoute(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    categoryId = key.categoryId,
                    onNavigateToProject = { projectId, projectTitle ->
                        navigateTo(PhotoTodoRoute.TaskDetail(projectId = projectId, projectTitle = projectTitle))
                    }
                )
            }
            is PhotoTodoRoute.TaskDetail -> {
                val viewModel: TaskDetailViewModel = hiltViewModel()
                TaskDetailRoute(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    projectId = key.projectId,
                    onBack = onBack
                )
            }
            else -> {
                // Not supported on Wear yet, fallback to Home
                val viewModel: HomeViewModel = hiltViewModel()
                HomeRoute(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToCategory = { categoryId, categoryName ->
                        navigateTo(PhotoTodoRoute.TasksList(categoryId = categoryId, categoryName = categoryName))
                    }
                )
            }
        }
    }
}
