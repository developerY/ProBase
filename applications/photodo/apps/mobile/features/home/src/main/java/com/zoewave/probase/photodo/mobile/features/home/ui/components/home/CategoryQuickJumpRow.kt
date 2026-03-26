package components.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.mobile.features.home.ui.components.CategoryQuickJumpUiModel
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.CategoryQuickJumpCard
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.HomeEvent

/**
 * 🚀 A horizontal section for "Important Categories Quick Jump".
 * It displays a header and a `LazyRow` of category cards/chips.
 */
/**
 * 🚀 A horizontal section for "Important Categories Quick Jump".
 * Now upgraded with an AnimatedVisibility accordion toggle!
 */
@Composable
fun CategoryQuickJumpRow(
    importantCategories: List<CategoryOverviewUiModel>,
    onEvent: (HomeEvent) -> Unit, // ✅ Ensure onEvent is passed down
    navTo: (PhotoTodoRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(true) }

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ChevronRotation"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Jump to Category",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse Categories" else "Expand Categories",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.rotate(arrowRotation)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            ) {
                items(
                    items = importantCategories,
                    key = { category -> category.id }
                ) { category ->
                    val index = importantCategories.indexOf(category)

                    // 1. Determine the color based on the index
                    val containerColor = when (index % 4) {
                        0 -> MaterialTheme.colorScheme.primaryContainer
                        1 -> MaterialTheme.colorScheme.secondaryContainer
                        2 -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    // 🚀 2. Map the data to your strict CategoryQuickJumpUiModel
                    val mappedModel = CategoryQuickJumpUiModel(
                        id = category.id,
                        name = category.name,
                        progressText = "${category.completedTasks} / ${category.totalTasks} Tasks",
                        progressPercentage = category.progressPercentage,
                        containerColor = containerColor,
                        icon = Icons.Default.FolderSpecial // Or map a specific icon if you have one!
                    )

                    // 3. Pass the strictly mapped model to your isolated component
                    CategoryQuickJumpCard(
                        model = mappedModel,
                        onEvent = onEvent, // ✅ Passed through
                        navTo = navTo
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true, name = "Quick Jump Row - Animated", backgroundColor = 0xFFF3F4F9)
@Composable
private fun CategoryQuickJumpRowPreview() {
    MaterialTheme {
        val mockData = listOf(
            CategoryOverviewUiModel(
                id = 1L,
                name = "Real Estate",
                totalTasks = 24,
                completedTasks = 18,
                progressPercentage = 0.75f
            ),
            CategoryOverviewUiModel(
                id = 2L,
                name = "Development",
                totalTasks = 50,
                completedTasks = 5,
                progressPercentage = 0.10f
            ),
            CategoryOverviewUiModel(
                id = 3L,
                name = "Business",
                totalTasks = 12,
                completedTasks = 12,
                progressPercentage = 1.0f
            ),
            CategoryOverviewUiModel(
                id = 4L,
                name = "Personal",
                totalTasks = 8,
                completedTasks = 4,
                progressPercentage = 0.50f
            )
        )

        // Wrapping in a Box with padding to simulate the screen edges
        Box(modifier = Modifier.padding(16.dp)) {
            CategoryQuickJumpRow(
                importantCategories = mockData,
                onEvent = {},
                navTo = {}
            )
        }
    }
}