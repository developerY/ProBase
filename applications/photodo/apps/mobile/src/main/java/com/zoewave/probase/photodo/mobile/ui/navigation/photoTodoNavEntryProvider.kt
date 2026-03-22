package com.zoewave.probase.photodo.mobile.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import com.zoewave.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.photodo.model.navigation.PhotoTodoRoute.Settings
import com.zoewave.photodo.model.navigation.PhotoTodoRoute.TaskDetail
import com.zoewave.photodo.model.navigation.PhotoTodoRoute.TasksList
import com.zoewave.probase.photodo.mobile.features.home.ui.CategoryGridUiRoute
import com.zoewave.probase.photodo.mobile.features.home.ui.HomeUiRoute
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksListUiRoute
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.TaskDetailUiRoute

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
                // 1. The New Root (Chart & AI Placeholder)
                HomeUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    navTo = navigateTo
                )
            }

            is PhotoTodoRoute.CategoryGrid -> {
                // 2. The Drill-Down (The Grid of Cards)
                CategoryGridUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToCategory = { categoryId, categoryName ->
                        // 3. THE CROSS-TAB JUMP 🪄
                        navigateTo(PhotoTodoRoute.TasksList(categoryId = categoryId, categoryName = categoryName))
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