package com.zoewave.probase.photodo.mobile.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.photodo.mobile.features.home.ui.HomeUiRoute
import com.zoewave.probase.photodo.mobile.features.tasks.ui.CategoryTasksUiRoute
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksListUiRoute
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.TaskDetailUiRoute
import com.zoewave.probase.photodo.mobile.ui.navigation.PhotoTodoRoute.CategoryTasks
import com.zoewave.probase.photodo.mobile.ui.navigation.PhotoTodoRoute.Home
import com.zoewave.probase.photodo.mobile.ui.navigation.PhotoTodoRoute.Settings
import com.zoewave.probase.photodo.mobile.ui.navigation.PhotoTodoRoute.TaskDetail
import com.zoewave.probase.photodo.mobile.ui.navigation.PhotoTodoRoute.TasksList

fun photoTodoNavEntryProvider(
    key: PhotoTodoRoute,
    navigateTo: (PhotoTodoRoute) -> Unit,
    navigateBack: () -> Unit // ✅ ADDED: To handle popping the Detail Screen
    // Optional: Pass an onTabSelected callback if you want to do the Cross-Tab Jump!
    // onTabSelected: (PhotoTodoRoute) -> Unit
): NavEntry<PhotoTodoRoute> {

    return NavEntry(key) {
        when (key) {
            is Home -> {
                HomeUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToCategory = { categoryId, categoryName ->
                        // ✅ FIXED: Navigate to the Category drill-down screen, NOT the detail screen!
                        navigateTo(CategoryTasks(categoryId, categoryName))
                    }
                )
            }

            is TasksList -> {
                TasksListUiRoute(
                    // ✅ Pass the navigation event down to the list
                    onNavigateToDetail = { id, title ->
                        navigateTo(TaskDetail(listId = id, listTitle = title))
                    }
                )
            }

            // ✅ NEW: Handle the Detail Route
            is TaskDetail -> {
                TaskDetailUiRoute(
                    listId = key.listId,
                    listTitle = key.listTitle,
                    onNavigateBack = navigateBack
                )
            }

            is Settings -> {
                Text("Settings")
            }

            is PhotoTodoRoute.CategoryTasks -> {
                CategoryTasksUiRoute(
                    categoryId = key.categoryId,
                    categoryName = key.categoryName,
                    onNavigateBack = navigateBack,
                    onNavigateToDetail = { listId, listTitle -> // ✅ Matches your signature

                        // THE CROSS-TAB JUMP!
                        // 1. If you have a state hoisted for your Bottom Bar, change it to Tasks Tab here!
                        // onTabSelected(PhotoTodoRoute.TasksList)

                        // 2. Push the deep detail screen onto the Nav3 state stack
                        navigateTo(PhotoTodoRoute.TaskDetail(listId = listId, listTitle = listTitle))
                    }
                )
            }
        }
    }
}