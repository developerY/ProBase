package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel

/**
 * Data model for a single slice of the progress wheel.
 */
data class OverviewWheelSlice(
    val name: String,
    val value: Int, // The absolute number of tasks
    val sweepAngle: Float, // The calculated angle (0-360)
    val color: Color
)

/**
 * Maps the Success category list into sorted, colored slices for the donut chart.
 */
@Composable
fun mapCategoriesToWheelSlices(
    categories: List<CategoryOverviewUiModel>
): List<OverviewWheelSlice> {

    // Sort by largest task count so the wheel is balanced
    val sortedCategories = categories.sortedByDescending { it.totalTasks }
    val totalTasks = sortedCategories.sumOf { it.totalTasks }

    // Return empty if no tasks exist
    if (totalTasks == 0) return emptyList()

    return sortedCategories.mapIndexed { index, category ->
        // 1. Assign color using the exact same cycle as the Category Cards
        val sliceColor = when (index % 4) {
            0 -> MaterialTheme.colorScheme.primary
            1 -> MaterialTheme.colorScheme.secondary
            2 -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.surfaceVariant
        }

        // 2. Calculate the angular size of the slice
        val angle = (category.totalTasks.toFloat() / totalTasks.toFloat()) * 360f

        OverviewWheelSlice(
            name = category.name,
            value = category.totalTasks,
            sweepAngle = angle,
            color = sliceColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MapCategoriesToWheelSlicesPreview() {
    val sampleCategories = listOf(
        CategoryOverviewUiModel(1, "Work", 10, 5, 0.5f),
        CategoryOverviewUiModel(2, "Personal", 5, 2, 0.4f),
        CategoryOverviewUiModel(3, "Shopping", 3, 3, 1.0f),
        CategoryOverviewUiModel(4, "Health", 2, 0, 0f)
    )

    PhotoDoTheme {
        val slices = mapCategoriesToWheelSlices(categories = sampleCategories)
        Surface {
            Column {
                slices.forEach { slice ->
                    Text(
                        text = "${slice.name}: ${slice.value} tasks, ${slice.sweepAngle} degrees",
                        color = slice.color
                    )
                }
            }
        }
    }
}
