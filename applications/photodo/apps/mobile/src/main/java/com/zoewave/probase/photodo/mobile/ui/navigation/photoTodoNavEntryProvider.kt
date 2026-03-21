package com.zoewave.probase.photodo.mobile.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.photodo.mobile.features.home.ui.HomeUiRoute
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksListUiRoute
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.TaskDetailUiRoute
import com.zoewave.probase.photodo.mobile.ui.navigation.PhotoTodoRoute.Home
import com.zoewave.probase.photodo.mobile.ui.navigation.PhotoTodoRoute.Settings
import com.zoewave.probase.photodo.mobile.ui.navigation.PhotoTodoRoute.TaskDetail
import com.zoewave.probase.photodo.mobile.ui.navigation.PhotoTodoRoute.TasksList

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
            is Home -> {
                HomeUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToCategory = { categoryId, categoryName ->
                        // THE CROSS-TAB JUMP! 🪄
                        // 1. (Optional) Tell the Bottom Bar to highlight the Tasks Tab
                        // onTabSelected(TasksList())

                        // 2. Tell Nav3 to push the filtered Tasks list onto the stack!
                        navigateTo(TasksList(categoryId = categoryId, categoryName = categoryName))
                    }
                )
            }

            // --- TAB 2: WORKSPACE ---
            is TasksList -> {
                TasksListUiRoute(
                    // ✅ Catch the ID and Name from the Nav3 key and pass them to the Route!
                    categoryId = key.categoryId,
                    categoryName = key.categoryName,
                    onNavigateToDetail = { id, title ->
                        navigateTo(TaskDetail(listId = id, listTitle = title))
                    }
                )
            }

            // --- DEEP DETAIL SCREEN ---
            is TaskDetail -> {
                TaskDetailUiRoute(
                    listId = key.listId,
                    listTitle = key.listTitle,
                    onNavigateBack = navigateBack
                )
            }

            // --- TAB 3: SETTINGS ---
            is Settings -> {
                Text("Settings")
            }

            PhotoTodoRoute.CategoryGrid -> TODO()
            is PhotoTodoRoute.CategoryTasks -> TODO()
        }
    }
}