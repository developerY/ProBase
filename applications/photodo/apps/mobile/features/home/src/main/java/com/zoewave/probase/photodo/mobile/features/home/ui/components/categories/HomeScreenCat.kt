package com.zoewave.probase.photodo.mobile.features.home.ui.components.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.HomeEvent
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.HomeUiState

@Composable
fun HomeScreenCat(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit, // ✅ Standardized Navigation Channel
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is HomeUiState.Empty -> {
                Text(
                    text = "No categories found. Start by adding one!",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is HomeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(uiState.categories, key = { it.id }) { category ->
                        CategoryCard(
                            category = category,
                            onEvent = onEvent,
                            navTo = navTo
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: CategoryOverviewUiModel,
    onEvent: (HomeEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            navTo(PhotoTodoRoute.TasksList(categoryId = category.id, categoryName = category.name))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${category.completedTasks} / ${category.totalTasks} tasks completed",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = category.progressText,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenCatPreview_Loading() {
    HomeScreenCat(
        uiState = HomeUiState.Loading,
        onEvent = {},
        navTo = {}
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenCatPreview_Success() {
    HomeScreenCat(
        uiState = HomeUiState.Success(
            categories = listOf(
                CategoryOverviewUiModel(
                    1, "Work", 10, 5, 0.5f
                ),
                CategoryOverviewUiModel(
                    2, "Personal", 5, 5, 1.0f
                )
            ),
            urgentProjects = emptyList()
        ),
        onEvent = {},
        navTo = {}
    )
}
