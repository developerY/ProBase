package com.zoewave.probase.photodo.mobile.features.tasks.ui.financial

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.tasks.R
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetProgressBarSummary(
    currentSpend: Double,
    projectBudget: Double?, // Nullable, because not all projects have a strict budget
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val formattedSpend = currencyFormatter.format(currentSpend)

    Column(modifier = modifier.fillMaxWidth()) {
        // SCENARIO A: No Budget Set (Just show what we've spent)
        if (projectBudget == null || projectBudget <= 0.0) {
            Text(
                text = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_total_spend_label, formattedSpend),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        // SCENARIO B: Budget is Set (Show the progress bar)
        val formattedBudget = currencyFormatter.format(projectBudget)

        // Calculate raw percentage (capped at 1.0 so the bar doesn't break if over budget)
        val rawPercentage = (currentSpend / projectBudget).toFloat()
        val displayPercentage = rawPercentage.coerceIn(0f, 1f)

        // 1. Smoothly animate the progress bar filling up
        var animatedProgress by remember { mutableFloatStateOf(0f) }
        LaunchedEffect(displayPercentage) {
            animatedProgress = displayPercentage
        }
        val progress by animateFloatAsState(
            targetValue = animatedProgress,
            animationSpec = tween(durationMillis = 1000),
            label = "progressAnimation"
        )

        // 2. Smart Color Logic (Green -> Orange -> Red)
        val targetColor = when {
            rawPercentage >= 1.0f -> MaterialTheme.colorScheme.error // Over Budget!
            rawPercentage >= 0.8f -> Color(0xFFE65100) // Warning (Orange)
            else -> MaterialTheme.colorScheme.primary // Looking good
        }

        val progressColor by animateColorAsState(
            targetValue = targetColor,
            animationSpec = tween(durationMillis = 800),
            label = "colorAnimation"
        )

        // --- The UI ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_budget_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$formattedSpend / $formattedBudget",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (rawPercentage >= 1.0f) FontWeight.Bold else FontWeight.Normal,
                color = if (rawPercentage >= 1.0f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)), // Perfectly rounded edges
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetProgressBarUnderBudgetPreviewSummary() {
    MaterialTheme {
        BudgetProgressBarSummary(
            currentSpend = 450.0,
            projectBudget = 1000.0,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetProgressBarSummaryWarningPreview() {
    MaterialTheme {
        BudgetProgressBarSummary(
            currentSpend = 850.0,
            projectBudget = 1000.0,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetProgressBarOverBudgetPreviewSummary() {
    MaterialTheme {
        BudgetProgressBarSummary(
            currentSpend = 1200.0,
            projectBudget = 1000.0,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetProgressBarNoBudgetPreviewSummary() {
    MaterialTheme {
        BudgetProgressBarSummary(
            currentSpend = 500.0,
            projectBudget = null,
            modifier = Modifier.padding(16.dp)
        )
    }
}
