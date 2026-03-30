package com.zoewave.probase.photodo.model.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.zoewave.photodo.model.R

sealed class PhotoTodoRoute(
    val title: String? = null,
    @StringRes val titleRes: Int? = null,
    val icon: ImageVector
) {

    // --- TAB 1: DASHBOARD ---
    // The new root: High-level graphic showing overall progress
    data object Home : PhotoTodoRoute(titleRes = R.string.applications_photodo_model_route_home, icon = Icons.Default.Home)

    // The drill-down: The grid of specific Category cards
    data object CategoryGrid : PhotoTodoRoute(titleRes = R.string.applications_photodo_model_route_categories, icon = Icons.Default.GridView)

    // --- TAB 2: WORKSPACE ---
    // Upgraded to a data class!
    // Defaults are null so the BottomBar can still launch it without a specific category.
    data class TasksList(
        val categoryId: Long? = null,
        val categoryName: String? = null
    ) : PhotoTodoRoute(
        title = categoryName,
        titleRes = if (categoryName == null) R.string.applications_photodo_model_route_tasks else null,
        icon = Icons.Default.CheckCircle
    )

    // The Category drill-down route!
    data class CategoryTasks(
        val categoryId: Long,
        val categoryName: String
    ) : PhotoTodoRoute(title = categoryName, icon = Icons.Default.Folder)

    // The Detail Route holds your specific Project data
    data class TaskDetail(
        val projectId: Long,
        val projectTitle: String
    ) : PhotoTodoRoute(title = projectTitle, icon = Icons.AutoMirrored.Filled.List)

    // --- TAB 3 ---
    data object Settings : PhotoTodoRoute(titleRes = R.string.applications_photodo_model_route_settings, icon = Icons.Default.Settings)

    data class Camera(val projectId: Long) : PhotoTodoRoute(titleRes = R.string.applications_photodo_model_route_camera, icon = Icons.Default.CameraAlt)
}

// Your Bottom Bar uses this, so it naturally ignores TaskDetail. Perfect!
val topLevelRoutes = listOf(
    PhotoTodoRoute.Home,
    PhotoTodoRoute.TasksList(categoryId = null, categoryName = null),
    PhotoTodoRoute.Settings
)
