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
    project: ProjectListUiModel,      // ✅ The strict UiState
    onEvent: (TasksEvent) -> Unit,    // ✅ The single event channel
    navTo: (PhotoTodoRoute?) -> Unit, // ✅ The single navigation channel
    modifier: Modifier = Modifier
) {
    // 1. Math Safety
    val currentSpend = project.currentSpend
    val budget = project.projectBudget
    val progress = if (budget > 0) (currentSpend / budget).toFloat() else 0f
    val safeProgress = progress.coerceIn(0f, 1f)
    val isOverBudget = progress >= 1f

    // 2. The Color-Coded Dollar Icon Logic (Green < 80%, Yellow 80-99%, Red 100%+)
    val financialStatusColor = when {
        progress >= 1f -> MaterialTheme.colorScheme.error // Red
        progress >= 0.8f -> androidx.compose.ui.graphics.Color(0xFFFFD54F) // Warning Yellow
        else -> androidx.compose.ui.graphics.Color(0xFF81C784) // Safe Green
    }

    val currencyFormatter = java.text.NumberFormat.getCurrencyInstance(LocalLocale.current.platformLocale)

    Card(
        modifier = modifier
            .fillMaxWidth()
            // ✅ Send the click directly through the navigation channel!
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
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(28.dp)
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
                    // ✅ Urgent Toggle mapped to onEvent
                    IconToggleButton(
                        checked = project.isUrgent,
                        onCheckedChange = { isUrgent ->
                            onEvent(TasksEvent.OnToggleProjectUrgent(project.projectId, isUrgent))
                        }
                    ) {
                        Icon(
                            imageVector = if (project.isUrgent) Icons.Filled.Error else Icons.Outlined.ErrorOutline,
                            contentDescription = "Urgent",
                            tint = if (project.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // ✅ Favorite Toggle mapped to onEvent
                    IconToggleButton(
                        checked = project.isFavorite,
                        onCheckedChange = { isFav ->
                            onEvent(TasksEvent.OnToggleProjectFavorite(project.projectId, isFav))
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

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTTOM SECTION: The Financial Progress Bar ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The new color-coded Dollar Icon + Label
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
                    text = "${currencyFormatter.format(currentSpend)} / ${currencyFormatter.format(budget)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { safeProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                // The bar itself turns red if over budget, otherwise uses primary theme color
                color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
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
                title = "Kitchen Remodel",
                categoryName = "Interior Design",
                currentSpend = 4500.0,
                projectBudget = 6000.0,
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
fun ProjectCardUrgentFavoritePreview() {
    PhotoDoTheme {
        ProjectCard(
            project = ProjectListUiModel(
                projectId = 2,
                title = "Client Website Redesign",
                categoryName = "Web Development",
                currentSpend = 1500.0,
                projectBudget = 2000.0,
                isFavorite = true,
                isUrgent = true
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectCardOverBudgetPreview() {
    PhotoDoTheme {
        ProjectCard(
            project = ProjectListUiModel(
                projectId = 3,
                title = "Backyard Landscaping",
                categoryName = "Exterior Design",
                currentSpend = 7200.0,
                projectBudget = 6500.0,
                isFavorite = false,
                isUrgent = true
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
