package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.home.R
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel

/**
 * 🚀 High-Density Data Visualization Card.
 * Replaces the blue metrics card with a compact "Color Wheel of Events" (Donut Chart).
 */
@Composable
fun OverviewSummaryCard(
    categories: List<CategoryOverviewUiModel>,
    modifier: Modifier = Modifier
) {
    // Transform the categories into colored slices
    val slices = mapCategoriesToWheelSlices(categories)
    val totalCategories = categories.size

    // Expressive, compact card (Height reduced to 160dp)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp), // Compact height
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant, // Using neutral color
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(20.dp) // express round corners
    ) {
        // Zero internal padding inside the card!
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Start, // Legend pushed to the left
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- 🚀 1. The Compact Legend (Left Side) ---
            Column(
                modifier = Modifier
                    .weight(1f) // Legends get space
                    .fillMaxHeight()
                    // Minimum padding to group the text nicely
                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Main Header (Bold, Primary Color)
                Text(
                    text = stringResource(R.string.applications_photodo_apps_mobile_features_home_total_categories),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Bold Category Count
                Text(
                    text = "$totalCategories",
                    style = MaterialTheme.typography.displayMedium, // Compact but Display font
                    fontWeight = FontWeight.Black
                )

                // Tiny Legend Items (Derivative from slices)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Show top 3 in compact legend
                    slices.take(3).forEach { slice ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Tiny Color Dot
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).size(8.dp).background(slice.color))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "${slice.name} (${slice.value})", // I'll keep this as is for now as it's a mix of dynamic data and symbols, or I could use a template
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // --- 🚀 2. The Color Wheel Donut Chart (Right Side) ---
            Box(
                modifier = Modifier
                    .size(140.dp) // Large wheel, packed into compact container
                    .padding(end = 16.dp), // Standard side padding
                contentAlignment = Alignment.Center
            ) {

                // --- Part A: The Drawing Canvas ---
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var currentStartAngle = -90f // Start from the top
                    val strokeWidth = 24.dp.toPx() // Expressive, thick stroke

                    if (slices.isEmpty()) {
                        // Empty state placeholder arc
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    } else {
                        slices.forEach { slice ->
                            // Draw the colored segment
                            drawArc(
                                color = slice.color,
                                startAngle = currentStartAngle,
                                sweepAngle = slice.sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round) // Rounded expressive ends
                            )
                            currentStartAngle += slice.sweepAngle // Increment the angle
                        }
                    }
                }

                // --- Part B: The Center Label (Derivative State) ---
                // Shows overall progress percentage
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val totalTasks = slices.sumOf { it.value }
                    val totalCompleted = categories.sumOf { it.completedTasks }
                    val progressPercentage = if (totalTasks > 0) (totalCompleted.toFloat() / totalTasks * 100).toInt() else 0

                    Text(
                        text = "$progressPercentage%",
                        style = MaterialTheme.typography.titleLarge, // Big bold center number
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.applications_photodo_apps_mobile_features_home_done),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true)
@Composable
private fun OverviewSummaryCardPreview() {
    val mockData = listOf(
        CategoryOverviewUiModel(
            id = 1L,
            name = "Real Estate",
            totalProjects = 3,
            totalTasks = 24,
            completedTasks = 18,
            progressPercentage = 0.75f
        ),
        CategoryOverviewUiModel(
            id = 2L,
            name = "Development",
            totalProjects = 5,
            totalTasks = 50,
            completedTasks = 5,
            progressPercentage = 0.10f
        ),
        CategoryOverviewUiModel(
            id = 3L,
            name = "Business",
            totalProjects = 2,
            totalTasks = 12,
            completedTasks = 12,
            progressPercentage = 1.0f
        ),
        CategoryOverviewUiModel(
            id = 4L,
            name = "Personal",
            totalProjects = 4,
            totalTasks = 8,
            completedTasks = 4,
            progressPercentage = 0.50f
        )
    )

    PhotoDoTheme {
        OverviewSummaryCard(
            categories = mockData,
            modifier = Modifier.padding(16.dp)
        )
    }
}
