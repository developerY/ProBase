package com.zoewave.probase.photodo.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class PhotoTodoRoute(val title: String, val icon: ImageVector) {
    data object Home : PhotoTodoRoute("Home", Icons.Default.Home)
    data object TasksList : PhotoTodoRoute("Tasks", Icons.Default.CheckCircle)
    data object Settings : PhotoTodoRoute("Settings", Icons.Default.Settings)

    // The Category drill-down route!
    data class CategoryTasks(
        val categoryId: Long,
        val categoryName: String
    ) : PhotoTodoRoute(categoryName, Icons.Default.Folder)

    // The Detail Route holds your specific TaskList data
    data class TaskDetail(
        val listId: Long,
        val listTitle: String
    ) : PhotoTodoRoute(listTitle, Icons.Default.List)
}

// Your Bottom Bar uses this, so it naturally ignores TaskDetail. Perfect!
val topLevelRoutes = listOf(
    PhotoTodoRoute.Home,
    PhotoTodoRoute.TasksList,
    PhotoTodoRoute.Settings
)