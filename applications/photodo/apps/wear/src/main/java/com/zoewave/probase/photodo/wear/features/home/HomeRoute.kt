package com.zoewave.probase.photodo.wear.features.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onCategoryClick: (Long, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        onCategoryClick = onCategoryClick
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onCategoryClick: (Long, String) -> Unit
) {
    val listState = rememberScalingLazyListState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            HomeUiState.Loading -> {
                CircularProgressIndicator()
            }
            HomeUiState.Empty -> {
                Text("No Categories Yet")
            }
            is HomeUiState.Success -> {
                ScalingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
                ) {
                    item {
                        ListHeader {
                            Text("Categories")
                        }
                    }
                    items(uiState.categories) { category ->
                        CategoryItem(
                            category = category,
                            onClick = { onCategoryClick(category.id, category.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: CategoryWearUiModel,
    onClick: () -> Unit
) {
    TitleCard(
        onClick = onClick,
        title = { Text(category.name) },
        subtitle = {
            Text("${category.completedTasks}/${category.totalTasks} tasks")
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        // You could add a progress bar here if you want
    }
}
