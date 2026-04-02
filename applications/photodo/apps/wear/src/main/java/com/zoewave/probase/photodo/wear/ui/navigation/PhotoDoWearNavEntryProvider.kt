package com.zoewave.probase.photodo.wear.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.wear.features.home.HomeRoute
import com.zoewave.probase.photodo.wear.features.project.ProjectListRoute
import com.zoewave.probase.photodo.wear.features.task.TaskDetailRoute

fun photoDoWearNavEntryProvider(
    key: PhotoTodoRoute,
    navigateTo: (PhotoTodoRoute) -> Unit,
    onBack: () -> Unit
): NavEntry<PhotoTodoRoute> {
    return NavEntry(key) {
        when (key) {
            PhotoTodoRoute.Home -> {
                HomeRoute(
                    modifier = Modifier.fillMaxSize(),
                    onCategoryClick = { categoryId, categoryName ->
                        navigateTo(PhotoTodoRoute.TasksList(categoryId = categoryId, categoryName = categoryName))
                    }
                )
            }
            is PhotoTodoRoute.TasksList -> {
                ProjectListRoute(
                    modifier = Modifier.fillMaxSize(),
                    categoryId = key.categoryId,
                    onProjectClick = { projectId, projectTitle ->
                        navigateTo(PhotoTodoRoute.TaskDetail(projectId = projectId, projectTitle = projectTitle))
                    }
                )
            }
            is PhotoTodoRoute.TaskDetail -> {
                TaskDetailRoute(
                    modifier = Modifier.fillMaxSize(),
                    projectId = key.projectId,
                    onBack = onBack
                )
            }
            else -> {
                // Not supported on Wear yet, fallback to Home
                HomeRoute(
                    modifier = Modifier.fillMaxSize(),
                    onCategoryClick = { categoryId, categoryName ->
                        navigateTo(PhotoTodoRoute.TasksList(categoryId = categoryId, categoryName = categoryName))
                    }
                )
            }
        }
    }
}
