package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute

/**
 * A specialized project row for the home screen "Jump Back In" section.
 */
@Composable
fun HomeProjectRow(
    project: ProjectListUiModel,
    onEvent: (HomeEvent) -> Unit,
    navTo: (PhotoTodoRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                navTo(PhotoTodoRoute.TaskDetail(projectId = project.projectId, projectTitle = project.title))
            },
        colors = CardDefaults.cardColors(
            containerColor = if (project.isUrgent) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when {
                    project.isUrgent -> Icons.Default.Error
                    project.isFavorite -> Icons.Default.Favorite
                    else -> Icons.Default.Star
                }
                val tint = when {
                    project.isUrgent -> MaterialTheme.colorScheme.error
                    project.isFavorite -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
                Icon(imageVector = icon, contentDescription = null, tint = tint)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(project.title, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        project.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Go")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeProjectRowPreview() {
    PhotoDoTheme {
        HomeProjectRow(
            project = ProjectListUiModel(
                projectId = 1L,
                title = "Sunset shoot",
                categoryName = "Nature",
                isFavorite = true,
                isUrgent = true
            ),
            onEvent = {},
            navTo = {}
        )
    }
}