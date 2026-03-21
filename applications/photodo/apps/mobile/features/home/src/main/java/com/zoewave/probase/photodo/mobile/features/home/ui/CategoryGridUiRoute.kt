package com.zoewave.probase.photodo.mobile.features.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.photodo.mobile.features.home.ui.components.HomeOverviewScreen

@Composable
fun CategoryGridUiRoute( // ✅ Renamed!
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToCategory: (Long, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeOverviewScreen(
        uiState = uiState,
        modifier = modifier,
        onEvent = { event ->
            if (event is HomeEvent.OnCategoryClicked) {
                // Trigger the Nav3 state change to push the new screen!
                onNavigateToCategory(event.categoryId, event.categoryName)
            } else {
                viewModel.onEvent(event)
            }
        }
    )
}