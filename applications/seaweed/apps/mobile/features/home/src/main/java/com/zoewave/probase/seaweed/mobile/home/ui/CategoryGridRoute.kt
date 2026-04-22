package com.zoewave.probase.seaweed.mobile.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.seaweed.mobile.core.ui.components.CategoryQuickJumpCard
import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import com.zoewave.probase.core.ui.R as CoreUiR

@Composable
fun CategoryGridRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    navTo: (SeaweedDestination) -> Unit,
    onBack: () -> Unit,
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
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }

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
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Actions")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(CoreUiR.string.core_ui_add_random_data)) },
                        onClick = {
                            onEvent(HomeUiEvent.AddRandomTransaction)
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Refresh") },
                        onClick = {
                            onEvent(HomeUiEvent.Refresh)
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                    )
                }
            }
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
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.categoriesSummary) { category ->
                        CategoryQuickJumpCard(
                            category = category,
                            onClick = { navTo(SeaweedDestination.Transactions(category.name)) },
                            onDelete = { categoryToDelete = category.name }
                        )
                    }
                }
            }
        }
    }

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete '$categoryToDelete' and all its transactions? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(HomeUiEvent.DeleteCategory(categoryToDelete!!))
                        categoryToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
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
