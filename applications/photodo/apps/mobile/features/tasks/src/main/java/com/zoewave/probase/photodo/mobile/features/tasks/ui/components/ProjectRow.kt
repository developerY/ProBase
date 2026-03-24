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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel

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
            .clickable { navTo(PhotoTodoRoute.TaskDetail(project.id, project.title)) },
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
                Text(text = project.categoryName, style = MaterialTheme.typography.bodySmall)
            }

            Row {
                // Urgent Toggle
                IconToggleButton(
                    checked = project.isUrgent,
                    onCheckedChange = { isUrgent ->
                        onEvent(TasksEvent.OnToggleProjectUrgent(project.id, isUrgent))
                    }
                ) {
                    Icon(
                        imageVector = if (project.isUrgent) Icons.Filled.Error else Icons.Outlined.ErrorOutline,
                        contentDescription = "Urgent",
                        tint = if (project.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Favorite Toggle
                IconToggleButton(
                    checked = project.isFavorite,
                    onCheckedChange = { isFav ->
                        onEvent(TasksEvent.OnToggleProjectFavorite(project.id, isFav))
                    }
                ) {
                    Icon(
                        imageVector = if (project.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
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
    MaterialTheme {
        ProjectRow(
            project = ProjectListUiModel(
                id = 1,
                title = "Sample Project",
                categoryName = "Work",
                isFavorite = false,
                isUrgent = false
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectRowUrgentFavoritePreview() {
    MaterialTheme {
        ProjectRow(
            project = ProjectListUiModel(
                id = 2,
                title = "Urgent Favorite Project",
                categoryName = "Personal",
                isFavorite = true,
                isUrgent = true
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
