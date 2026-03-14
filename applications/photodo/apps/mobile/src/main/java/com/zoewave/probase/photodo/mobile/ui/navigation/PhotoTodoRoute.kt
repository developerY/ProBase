package com.zoewave.probase.photodo.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class PhotoTodoRoute(val title: String, val icon: ImageVector) {
    data object Home : PhotoTodoRoute("Home", Icons.Default.Home)
    data object TasksList : PhotoTodoRoute("Tasks", Icons.Default.CheckCircle)
    data object Settings : PhotoTodoRoute("Settings", Icons.Default.Settings)
}

val topLevelRoutes = listOf(
    PhotoTodoRoute.Home,
    PhotoTodoRoute.TasksList,
    PhotoTodoRoute.Settings
)