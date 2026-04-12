package com.zoewave.probase.seaweed.mobile.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.core.ui.R as CoreUiR
import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import com.zoewave.probase.seaweed.mobile.home.ui.components.CategoryQuickJumpCard

@Composable
fun CategoryGridRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    navTo: (SeaweedDestination) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CategoryGridScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is HomeUiEvent.OnBackClicked) {
                onBack()
            } else {
                viewModel.onEvent(event)
            }
        },
        navTo = navTo,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryGridScreen(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreUiR.string.core_ui_all_categories)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(HomeUiEvent.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        when (uiState) {
            HomeUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.categoriesSummary) { category ->
                        CategoryQuickJumpCard(
                            category = category,
                            onClick = { navTo(SeaweedDestination.Transactions(category.name)) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryGridScreenPreview() {
    MaterialTheme {
        CategoryGridScreen(
            uiState = HomeUiState.Success(
                categoriesSummary = listOf(
                    CategoryOverview("Food", 42.0, 1, 100.0),
                    CategoryOverview("Coffee", 15.0, 1, 50.0),
                    CategoryOverview("Transport", 80.0, 1, 150.0),
                    CategoryOverview("Shopping", 250.0, 1, 500.0)
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryGridScreenLoadingPreview() {
    MaterialTheme {
        CategoryGridScreen(
            uiState = HomeUiState.Loading,
            onEvent = {},
            navTo = {}
        )
    }
}
