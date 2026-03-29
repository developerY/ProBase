package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute

@Composable
fun ProjectCard(
    project: ProjectListUiModel,
    onEvent: (TasksEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. NO MORE MATH! We just ask the model for its state.
    val financialStatusColor = when {
        project.isOverBudget -> MaterialTheme.colorScheme.error // Red
        project.isNearBudgetLimit -> androidx.compose.ui.graphics.Color(0xFFFFD54F) // Warning Yellow
        else -> androidx.compose.ui.graphics.Color(0xFF81C784) // Safe Green
    }

    val currencyFormatter = java.text.NumberFormat.getCurrencyInstance(LocalLocale.current.platformLocale)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { navTo(PhotoTodoRoute.TaskDetail(project.projectId, project.title)) }
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (project.isUrgent)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // --- TOP SECTION: Titles and Actions ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 16.dp).size(28.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = project.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconToggleButton(
                        checked = project.isUrgent,
                        onCheckedChange = { onEvent(TasksEvent.OnToggleProjectUrgent(project.projectId, it)) }
                    ) {
                        Icon(
                            imageVector = if (project.isUrgent) Icons.Filled.Error else Icons.Outlined.ErrorOutline,
                            contentDescription = "Urgent",
                            tint = if (project.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconToggleButton(
                        checked = project.isFavorite,
                        onCheckedChange = { onEvent(TasksEvent.OnToggleProjectFavorite(project.projectId, it)) }
                    ) {
                        Icon(
                            imageVector = if (project.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (project.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTTOM SECTION: The Financial Progress Bar ---
            // Only show the budget UI if the user actually set a budget for this project!
            if (project.hasBudget) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Financial Status",
                            tint = financialStatusColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Budget",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "${currencyFormatter.format(project.currentSpend)} / ${currencyFormatter.format(project.projectBudget)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (project.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    // Safe coercion directly in the UI layer
                    progress = { project.budgetUsagePercent.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (project.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectCardPreview() {
    PhotoDoTheme {
        ProjectCard(
            project = ProjectListUiModel(
                projectId = 1,
                title = "Standard Project",
                categoryName = "Work",
                isFavorite = false,
                isUrgent = false,
                dueDateMillis = System.currentTimeMillis() + 86400000L * 3,
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectCardBudgetPreview() {
    PhotoDoTheme {
        ProjectCard(
            project = ProjectListUiModel(
                projectId = 2,
                title = "Project with Budget",
                categoryName = "Home",
                isFavorite = true,
                isUrgent = false,
                currentSpend = 750.0,
                projectBudget = 1000.0
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectCardUrgentOverBudgetPreview() {
    PhotoDoTheme {
        ProjectCard(
            project = ProjectListUiModel(
                projectId = 3,
                title = "Urgent & Over Budget",
                categoryName = "Critical",
                isFavorite = false,
                isUrgent = true,
                currentSpend = 1200.0,
                projectBudget = 1000.0
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
