package com.zoewave.probase.photodo.mobile.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.core.R
import java.text.NumberFormat

@Composable
fun BudgetProgressBar(
    projectName: String,
    currentSpend: Double,
    projectBudget: Double,
    modifier: Modifier = Modifier
) {
    // 1. Math Safety: Prevent divide-by-zero and coerce the progress bar between 0% and 100%
    val progress = if (projectBudget > 0) (currentSpend / projectBudget).toFloat() else 0f
    val safeProgress = progress.coerceIn(0f, 1f)

    // 2. Dynamic Color: Turns red if they spend more than the budget
    val progressColor = if (progress >= 1f) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary // Uses your Coral, Forest, or Default primary!
    }

    // 3. Formatting: Automatically converts doubles to "$1,200.00" based on the user's locale
    val currencyFormatter = NumberFormat.getCurrencyInstance(LocalLocale.current.platformLocale)
    val formattedSpend = currencyFormatter.format(currentSpend)
    val formattedBudget = currencyFormatter.format(projectBudget)

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        // --- Header Row: Name & Numbers ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = projectName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$formattedSpend / $formattedBudget",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- The Visual Bar ---
        LinearProgressIndicator(
            progress = { safeProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp) // Slightly thicker than default for better visibility
                .clip(RoundedCornerShape(5.dp)), // Rounds the edges
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )

        // --- Optional Over-Budget Warning ---
        if (progress >= 1f && currentSpend > projectBudget) {
            Text(
                text = stringResource(R.string.applications_photodo_apps_mobile_core_over_budget, currencyFormatter.format(currentSpend - projectBudget)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BudgetProgressBarPreview() {
    PhotoDoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            BudgetProgressBar(
                projectName = "Kitchen Remodel",
                currentSpend = 1200.0,
                projectBudget = 5000.0
            )
            Spacer(modifier = Modifier.height(16.dp))
            BudgetProgressBar(
                projectName = "Office Supplies",
                currentSpend = 150.0,
                projectBudget = 100.0
            )
        }
    }
}
