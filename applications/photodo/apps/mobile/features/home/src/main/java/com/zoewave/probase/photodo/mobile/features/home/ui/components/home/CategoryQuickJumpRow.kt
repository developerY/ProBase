package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.mobile.features.home.ui.components.CategoryQuickJumpUiModel

/**
 * 🚀 A horizontal section for "Important Categories Quick Jump".
 * It displays a header and a `LazyRow` of category cards/chips.
 */
@Composable
fun CategoryQuickJumpRow(
    uiState: List<CategoryQuickJumpUiModel>,
    onEvent: (HomeEvent) -> Unit,
    navTo: (PhotoTodoRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 1. Section Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Text(
                "Jump to Category",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // 2. The Horizontal Scrollable Category Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 0.dp), // Screen padding handles edges
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = uiState,
                key = { category -> category.id }
            ) { category ->
                CategoryQuickJumpCard(
                    uiState = category,
                    onEvent = onEvent,
                    navTo = navTo
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryQuickJumpRowPreview() {
    MaterialTheme {
        CategoryQuickJumpRow(
            uiState = listOf(
                CategoryQuickJumpUiModel(
                    id = 1L,
                    name = "Nature",
                    progressText = "5/10 Tasks",
                    progressPercentage = 0.5f,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    icon = Icons.Default.FolderSpecial
                ),
                CategoryQuickJumpUiModel(
                    id = 2L,
                    name = "Urban",
                    progressText = "2/8 Tasks",
                    progressPercentage = 0.25f,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    icon = Icons.Default.FolderSpecial
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}