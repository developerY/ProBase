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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.components.icons.BudgetTrendIcon
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 1. The Dynamic "Smart Subtitle"
                        val subtitleText = remember(project.categoryName, project.dueDateMillis, project.progressText) {
                            buildString {
                                append(project.categoryName)
                                if (project.progressText.isNotEmpty()) {
                                    append(" • ${project.progressText}")
                                }
                                project.dueDateMillis?.let { dueDate ->
                                    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                                    val dateStr = formatter.format(Date(dueDate))
                                    append(" • $dateStr")
                                }
                            }
                        }

                        // 🚀 2. Display the newly merged string
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // --- BUDGET DISPLAY ---
                        if (project.hasBudget) {
                            Text(
                                " • ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val budgetColor = when {
                                project.isOverBudget -> MaterialTheme.colorScheme.error
                                project.isNearBudgetLimit -> androidx.compose.ui.graphics.Color(0xFFE5B800)
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Text(
                                "$${project.currentSpend.toInt()} / $${project.projectBudget.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = budgetColor
                            )
                        }
                    }
                }
            }

            // --- RIGHT SIDE ICONS ---
            // Wrapped in a row to ensure they stay pinned together on the far right
            Row(verticalAlignment = Alignment.CenterVertically) {
                BudgetTrendIcon(
                    project = project,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeProjectRowGreenPreview() {
    PhotoDoTheme {
        HomeProjectRow(
            project = ProjectListUiModel(
                projectId = 1L,
                title = "Portrait session",
                categoryName = "Work",
                isFavorite = true,
                isUrgent = false,
                currentSpend = 120.0,
                projectBudget = 200.0,
                // 🚀 ADDED: A mock timestamp (roughly ~3 days from now) just so you can preview it!
                dueDateMillis = System.currentTimeMillis() + 259200000L,
                doneTasksCount = 2,
                totalTasksCount = 5
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeProjectRowYellowPreview() {
    PhotoDoTheme {
        HomeProjectRow(
            project = ProjectListUiModel(
                projectId = 1L,
                title = "Portrait session",
                categoryName = "Work",
                isFavorite = true,
                isUrgent = false,
                currentSpend = 200.0,
                projectBudget = 200.0,
                dueDateMillis = System.currentTimeMillis() + 259200000L,
                doneTasksCount = 4,
                totalTasksCount = 5
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeProjectRowRedPreview() {
    PhotoDoTheme {
        HomeProjectRow(
            project = ProjectListUiModel(
                projectId = 1L,
                title = "Portrait session",
                categoryName = "Work",
                isFavorite = true,
                isUrgent = false,
                currentSpend = 270.0,
                projectBudget = 200.0,
                doneTasksCount = 1,
                totalTasksCount = 5
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeProjectRowWithDueDatePreview() {
    PhotoDoTheme {
        HomeProjectRow(
            project = ProjectListUiModel(
                projectId = 4L,
                title = "Engagement shoot",
                categoryName = "Events",
                isFavorite = false,
                isUrgent = false,
                currentSpend = 270.0,
                projectBudget = 200.0,
                dueDateMillis = System.currentTimeMillis() + 86400000L * 7, // 1 week from now
                doneTasksCount = 3,
                totalTasksCount = 3
            ),
            onEvent = {},
            navTo = {}
        )
    }
}