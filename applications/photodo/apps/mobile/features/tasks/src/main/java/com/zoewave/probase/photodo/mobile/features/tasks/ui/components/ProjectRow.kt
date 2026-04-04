package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.tasks.R
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute

@Composable
fun ProjectRow(
    project: ProjectListUiModel, // ✅ The strict UiState for this component
    onEvent: (TasksEvent) -> Unit, // ✅ The single event channel
    navTo: (PhotoTodoRoute?) -> Unit, // ✅ The single navigation channel
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            // Send the click directly through the navigation channel!
            .clickable { navTo(PhotoTodoRoute.TaskDetail(project.projectId, project.title)) },
        colors = CardDefaults.cardColors(
            containerColor = if (project.isUrgent)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(text = project.title, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = project.categoryName, style = MaterialTheme.typography.bodySmall)
                    if (project.progressText.isNotEmpty()) {
                        Text(
                            text = " • ${project.progressText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Row {
                // Urgent Toggle
                IconToggleButton(
                    checked = project.isUrgent,
                    onCheckedChange = { isUrgent ->
                        onEvent(TasksEvent.OnToggleProjectUrgent(project.projectId, isUrgent))
                    }
                ) {
                    Icon(
                        imageVector = if (project.isUrgent) Icons.Filled.Error else Icons.Outlined.ErrorOutline,
                        contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_urgent_content_desc),
                        tint = if (project.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Favorite Toggle
                IconToggleButton(
                    checked = project.isFavorite,
                    onCheckedChange = { isFav ->
                        onEvent(TasksEvent.OnToggleProjectFavorite(project.projectId, isFav))
                    }
                ) {
                    Icon(
                        imageVector = if (project.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_favorite_content_desc),
                        tint = if (project.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ProjectRowPreview() {
    PhotoDoTheme {
        ProjectRow(
            project = ProjectListUiModel(
                projectId = 1,
                title = "Sample Project",
                categoryName = "Work",
                isFavorite = false,
                isUrgent = false,
                dueDateMillis = System.currentTimeMillis() + 86400000L * 3,
                isCompleted = false,
                doneTasksCount = 1,
                totalTasksCount = 2
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectRowUrgentFavoritePreview() {
    PhotoDoTheme {
        ProjectRow(
            project = ProjectListUiModel(
                projectId = 2,
                title = "Urgent Favorite Project",
                categoryName = "Personal",
                isFavorite = true,
                isUrgent = true,
                // budget
                currentSpend = 10.0,
                projectBudget = 100.0,
                dueDateMillis = System.currentTimeMillis() + 259200000L,
                isCompleted = false,
                doneTasksCount = 3,
                totalTasksCount = 5
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
