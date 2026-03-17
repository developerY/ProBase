package com.zoewave.probase.photodo.mobile.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.photodo.mobile.features.home.ui.HomeUiRoute
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TaskDetailUiRoute
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksListUiRoute

fun photoTodoNavEntryProvider(
    key: PhotoTodoRoute,
    navigateTo: (PhotoTodoRoute) -> Unit,
    navigateBack: () -> Unit // ✅ ADDED: To handle popping the Detail Screen
): NavEntry<PhotoTodoRoute> {

    return NavEntry(key) {
        when (key) {
            is PhotoTodoRoute.Home -> {
                HomeUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToCategory = { categoryId, categoryName ->
                        // When a category card is tapped, open its Detail screen!
                        navigateTo(PhotoTodoRoute.TaskDetail(listId = categoryId, listTitle = categoryName))
                    }
                )
            }

            is PhotoTodoRoute.TasksList -> {
                TasksListUiRoute(
                    // ✅ Pass the navigation event down to the list
                    onNavigateToDetail = { id, title ->
                        navigateTo(PhotoTodoRoute.TaskDetail(listId = id, listTitle = title))
                    }
                )
            }

            // ✅ NEW: Handle the Detail Route
            is PhotoTodoRoute.TaskDetail -> {
                TaskDetailUiRoute(
                    listId = key.listId,
                    listTitle = key.listTitle,
                    onNavigateBack = navigateBack
                )
            }

            is PhotoTodoRoute.Settings -> {
                Text("Settings")
            }
        }
    }
}