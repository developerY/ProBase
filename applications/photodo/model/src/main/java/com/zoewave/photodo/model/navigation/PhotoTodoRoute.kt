package com.zoewave.photodo.model.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class PhotoTodoRoute(val title: String, val icon: ImageVector) {

    // --- TAB 1: DASHBOARD ---
    // The new root: High-level graphic showing overall progress
    data object Home : PhotoTodoRoute("Home", Icons.Default.Home)

    // The drill-down: The grid of specific Category cards
    data object CategoryGrid : PhotoTodoRoute("Categories", Icons.Default.GridView)

    // --- TAB 2: WORKSPACE ---
    // Upgraded to a data class!
    // Defaults are null so the BottomBar can still launch it without a specific category.
    data class TasksList(
        val categoryId: Long? = null,
        val categoryName: String? = null
    ) : PhotoTodoRoute(categoryName ?: "Tasks", Icons.Default.CheckCircle)

    // The Category drill-down route!
    data class CategoryTasks(
        val categoryId: Long,
        val categoryName: String
    ) : PhotoTodoRoute(categoryName, Icons.Default.Folder)

    // The Detail Route holds your specific TaskList data
    data class TaskDetail(
        val listId: Long,
        val listTitle: String
    ) : PhotoTodoRoute(listTitle, Icons.AutoMirrored.Filled.List)

    // --- TAB 3 ---
    data object Settings : PhotoTodoRoute("Settings", Icons.Default.Settings)

    data class Camera(val listId: Long) : PhotoTodoRoute("Camera", Icons.Default.CameraAlt)
}

// Your Bottom Bar uses this, so it naturally ignores TaskDetail. Perfect!
val topLevelRoutes = listOf(
    PhotoTodoRoute.Home,
    PhotoTodoRoute.TasksList(categoryId = null, categoryName = null),
    PhotoTodoRoute.Settings
)
