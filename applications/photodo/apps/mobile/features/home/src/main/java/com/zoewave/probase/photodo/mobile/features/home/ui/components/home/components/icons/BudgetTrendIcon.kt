package com.zoewave.probase.photodo.mobile.features.home.ui.components.home.components.icons

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel

@Composable
fun BudgetTrendIcon(
    project: ProjectListUiModel,
    modifier: Modifier = Modifier
) {
    // 1. Safety check: Don't draw anything if there's no budget.
    if (!project.hasBudget) return

    // 2. The Color Logic (Red > Yellow > Green)
    val financialStatusColor = when {
        project.isOverBudget -> MaterialTheme.colorScheme.error // 🔴 Red
        project.isNearBudgetLimit -> androidx.compose.ui.graphics.Color(0xFFFFD54F) // 🟡 Warning Yellow
        else -> androidx.compose.ui.graphics.Color(0xFF81C784) // 🟢 Safe Green
    }

    // 3. The Trending Arrow Logic (Up > Flat > Down)
    val trendingIcon = when {
        project.isOverBudget -> Icons.AutoMirrored.Filled.TrendingUp // Spending High (Bad trend)
        project.isNearBudgetLimit -> Icons.AutoMirrored.Filled.TrendingFlat // Nearing Limit (Warning)
        else -> Icons.AutoMirrored.Filled.TrendingDown // Spending Low (Good trend)
    }

    // 4. The Composition Box
    Box(
        modifier = modifier.size(32.dp), // Set total area for the combi-icon
        contentAlignment = androidx.compose.ui.Alignment.Center // Base align everything to center
    ) {

        // 🚀 LAYER 1 (BACKGROUND): The Dynamic Trending Graph/Arrow
        Icon(
            imageVector = trendingIcon,
            contentDescription = null, // decorative
            tint = financialStatusColor, // Carries the color coding!
            modifier = Modifier.size(28.dp) // The larger background element
        )

        // 🚀 LAYER 2 (FOREGROUND): The Dollar Sign
        // Make it smaller and offset it slightly so both icons are readable
        Icon(
            imageVector = Icons.Default.AttachMoney,
            contentDescription = "Budget Status", // Screen reader info
            // Use a dark color (like onSurfaceVariant) so it pops against the status color
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(16.dp) // Smaller foreground element
                .align(androidx.compose.ui.Alignment.BottomEnd) // Pin to bottom-right
                .padding(bottom = 2.dp, end = 2.dp) // Tiny padding for visual separation
        )
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
