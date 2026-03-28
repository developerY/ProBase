package com.zoewave.probase.photodo.mobile.features.home.ui.components.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.HomeEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute

@Composable
fun HomeProjectRow(
    project: ProjectListUiModel,      // ✅ UiState
    onEvent: (HomeEvent) -> Unit,     // ✅ onEvent Channel
    navTo: (PhotoTodoRoute) -> Unit,  // ✅ navTo Channel
    modifier: Modifier = Modifier
) {
    // 1. The Financial Color Logic (Same as your rich ProjectCard)
    val financialStatusColor = when {
        project.isOverBudget -> MaterialTheme.colorScheme.error // Red
        project.isNearBudgetLimit -> Color(0xFFFFD54F) // Warning Yellow
        else -> Color(0xFF81C784) // Safe Green
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                navTo(PhotoTodoRoute.TaskDetail(projectId = project.projectId, projectTitle = project.title))
            },
        colors = CardDefaults.cardColors(
            containerColor = if (project.isUrgent)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- LEFT SIDE: Status Icon & Titles ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
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

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = project.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- RIGHT SIDE: Financial Status & Navigation Arrow ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Only show the dollar sign if a budget was actually set!
                if (project.hasBudget) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = "Financial Status",
                        tint = financialStatusColor,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Go",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                title = "Portrait session",
                categoryName = "Work",
                isFavorite = true,
                isUrgent = false,
                currentSpend = 120.0,
                projectBudget = 200.0
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeProjectRowUrgentPreview() {
    PhotoDoTheme {
        HomeProjectRow(
            project = ProjectListUiModel(
                projectId = 2L,
                title = "Wedding shoot",
                categoryName = "Events",
                isFavorite = false,
                isUrgent = true,
                currentSpend = 500.0,
                projectBudget = 400.0
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
