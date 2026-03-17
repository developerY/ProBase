package com.zoewave.probase.photodo.mobile.features.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.photodo.mobile.features.home.ui.components.HomeOverviewScreen

@Composable
fun HomeUiRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToCategory: (Long, String) -> Unit, // ✅ Add the Nav3 callback
    // navTo: (PhotoTodoRoute) -> Unit = {} // Add this when you need to navigate away from Home
) {

    // collectAsStateWithLifecycle is MAD-recommended over collectAsState
    // as it safely pauses collection when the app is in the background, saving battery!
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Pass the state and events down to our new dashboard screen
    HomeOverviewScreen(
        uiState = uiState,
        modifier = modifier,
        onEvent = { event ->
            // Intercept events coming up from the UI
            when (event) {
                is HomeEvent.OnCategoryClicked -> {
                    // 1. Let the ViewModel know the click happened (Optional: for logging)
                    viewModel.onEvent(event)

                    // 2. Trigger the Nav3 state change to push the new screen!
                    onNavigateToCategory(event.categoryId, event.categoryName)
                }
                // If you add other events to HomeEvent later, pass them to the ViewModel:
                // else -> viewModel.onEvent(event)
                HomeEvent.OnRefresh -> TODO()
                is HomeEvent.OnTaskClicked -> TODO()
                is HomeEvent.OnTaskToggled -> TODO()
            }
        }
    )

    /* collectAsStateWithLifecycle is MAD-recommended over collectAsState
    // as it safely pauses collection when the app is in the background
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )*/

}