package com.zoewave.probase.photodo.mobile.features.home.ui.components.home.components.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.home.R
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel

@Composable
fun BudgetTrendIcon(
    project: ProjectListUiModel,
    modifier: Modifier = Modifier
) {
    if (!project.hasBudget) return

    val statusColor = when {
        project.isOverBudget -> MaterialTheme.colorScheme.error
        project.isNearBudgetLimit -> androidx.compose.ui.graphics.Color(0xFFE5B800)
        else -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
    }

    val trendingIcon = when {
        project.isOverBudget -> Icons.AutoMirrored.Filled.TrendingUp
        project.isNearBudgetLimit -> Icons.AutoMirrored.Filled.TrendingFlat
        else -> Icons.AutoMirrored.Filled.TrendingDown
    }

    // 1. Increased the overall container size to 40.dp
    Box(
        modifier = modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {

        // 2. Expanded the graph to fill the entire 40.dp space
        Icon(
            imageVector = trendingIcon,
            contentDescription = null,
            tint = statusColor.copy(alpha = 0.6f),
            modifier = Modifier.size(40.dp)
        )

        // 3. Shrunk the dollar sign slightly to 14.dp so the graph dominates
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .padding(2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AttachMoney,
                contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_home_budget_status_content_desc),
                tint = statusColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetTrendIconPreview() {
    PhotoDoTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column {
                Row(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    BudgetTrendIcon(
                        project = ProjectListUiModel(
                            projectId = 1,
                            title = "Under Budget",
                            categoryName = "Work",
                            projectBudget = 1000.0,
                            currentSpend = 500.0
                        )
                    )
                    androidx.compose.material3.Text(
                        text = "Under Budget (Green, Trending Down)",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Row(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    BudgetTrendIcon(
                        project = ProjectListUiModel(
                            projectId = 2,
                            title = "Near Budget Limit",
                            categoryName = "Home",
                            projectBudget = 1000.0,
                            currentSpend = 950.0
                        )
                    )
                    androidx.compose.material3.Text(
                        text = "Near Limit (Yellow, Trending Flat)",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    BudgetTrendIcon(
                        project = ProjectListUiModel(
                            projectId = 3,
                            title = "Over Budget",
                            categoryName = "Personal",
                            projectBudget = 1000.0,
                            currentSpend = 1200.0
                        )
                    )
                    androidx.compose.material3.Text(
                        text = "Over Budget (Red, Trending Up)",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
