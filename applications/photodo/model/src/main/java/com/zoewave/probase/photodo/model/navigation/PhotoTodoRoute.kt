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
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class PhotoTodoRoute(
    @Transient val title: String? = null,
    @Transient @StringRes val titleRes: Int? = null,
    @Transient val icon: ImageVector = Icons.Default.CheckCircle
) {

    // --- TAB 1: DASHBOARD ---
    @Serializable
    data object Home : PhotoTodoRoute(titleRes = R.string.applications_photodo_model_route_home, icon = Icons.Default.Home)

    @Serializable
    data object CategoryGrid : PhotoTodoRoute(titleRes = R.string.applications_photodo_model_route_categories, icon = Icons.Default.GridView)

    // --- TAB 2: WORKSPACE ---
    @Serializable
    data class TasksList(
        val categoryId: Long? = null,
        val categoryName: String? = null,
        val prefilledAiDraft: SmartTaskDraft? = null
    ) : PhotoTodoRoute(
        title = categoryName,
        titleRes = if (categoryName == null) R.string.applications_photodo_model_route_tasks else null,
        icon = Icons.Default.CheckCircle
    )

    @Serializable
    data class CategoryTasks(
        val categoryId: Long,
        val categoryName: String
    ) : PhotoTodoRoute(title = categoryName, icon = Icons.Default.Folder)

    @Serializable
    data class TaskDetail(
        val projectId: Long,
        val projectTitle: String
    ) : PhotoTodoRoute(title = projectTitle, icon = Icons.AutoMirrored.Filled.List)

    // --- TAB 3 ---
    @Serializable
    data object Settings : PhotoTodoRoute(titleRes = R.string.applications_photodo_model_route_settings, icon = Icons.Default.Settings)

    @Serializable
    data class Camera(val projectId: Long? = null) : PhotoTodoRoute(titleRes = R.string.applications_photodo_model_route_camera, icon = Icons.Default.CameraAlt)

    @Serializable
    data class SavePhoto(
        val photoUri: String,
        val prefilledAiDraft: SmartTaskDraft? = null
    ) : PhotoTodoRoute(icon = Icons.Default.CameraAlt)

    @Serializable
    data class SmartCapture(val photoUri: String) : PhotoTodoRoute(icon = Icons.Default.CameraAlt)
}

// Your Bottom Bar uses this, so it naturally ignores TaskDetail. Perfect!
val topLevelRoutes = listOf(
    PhotoTodoRoute.Home,
    PhotoTodoRoute.TasksList(categoryId = null, categoryName = null),
    PhotoTodoRoute.Settings
)
