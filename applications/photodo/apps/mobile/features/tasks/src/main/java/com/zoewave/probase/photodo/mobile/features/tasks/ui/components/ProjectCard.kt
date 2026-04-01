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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.tasks.R
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectCard(
    project: ProjectListUiModel,
    onEvent: (TasksEvent) -> Unit,
    onDeleteClicked: (ProjectListUiModel) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit,
    modifier: Modifier = Modifier
) {
    val errorColor = MaterialTheme.colorScheme.error
    val financialStatusColor = remember(project.isOverBudget, project.isNearBudgetLimit) {
        when {
            project.isOverBudget -> errorColor // Red
            project.isNearBudgetLimit -> Color(0xFFFFD54F) // Warning Yellow
            else -> Color(0xFF81C784) // Safe Green
        }
    }

    val locale = LocalLocale.current.platformLocale
    val currencyFormatter = remember(locale) {
        NumberFormat.getCurrencyInstance(locale)
    }

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
                    // 🚀 The Dynamic "Smart Subtitle"
                    val subtitleText = remember(project.categoryName, project.dueDateMillis) {
                        buildString {
                            append(project.categoryName)
                            project.dueDateMillis?.let { dueDate ->
                                val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                                val dateStr = formatter.format(Date(dueDate))
                                append(" • $dateStr")
                            }
                        }
                    }
                    Text(
                        text = subtitleText,
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
                            contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_urgent_content_desc),
                            tint = if (project.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconToggleButton(
                        checked = project.isFavorite,
                        onCheckedChange = { onEvent(TasksEvent.OnToggleProjectFavorite(project.projectId, it)) }
                    ) {
                        Icon(
                            imageVector = if (project.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_favorite_content_desc),
                            tint = if (project.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    /*IconButton(
                        onClick = { onDeleteClicked(project) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Project",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }*/
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
                            contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_financial_status_content_desc),
                            tint = financialStatusColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_budget_label),
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
            onDeleteClicked = {},
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
            onDeleteClicked = {},
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
            onDeleteClicked = {},
            navTo = {}
        )
    }
}
