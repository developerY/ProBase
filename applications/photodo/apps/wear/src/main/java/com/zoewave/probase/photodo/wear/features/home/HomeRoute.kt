package com.zoewave.probase.photodo.wear.features.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onNavigateToCategory: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is HomeEvent.OnCategoryClick -> onNavigateToCategory(event.id, event.name)
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier
    )
}
