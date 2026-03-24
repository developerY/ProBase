package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel
import components.home.CategoryQuickJumpRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    navTo: (PhotoTodoRoute) -> Unit, // ✅ Restrictive navigation channel enforced
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        // 🚀 Upgrade: Large Top AppBar for an expressive title feel
        topBar = { LargeTopAppBar(title = { Text("Overview") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- 🚀 NEW: Graphic/AI Overview Section (Derivative State) ---
            if (uiState is HomeUiState.Success) {
                // Compute the models on recomposition
                val importantCategories = remember(uiState.categories) {
                    // Logic: Categories with most pending tasks
                    uiState.categories
                        .sortedByDescending { it.totalTasks - it.completedTasks }
                        .take(5) // Show top 5
                }

                // 🚀 1. The main "High-Density Wheel" summary card
                OverviewSummaryCard(categories = uiState.categories) // ✅ Pass list directly

                Spacer(modifier = Modifier.height(24.dp)) // Vertical spacer reduced (Compactness)

                // 2. The horizontal quick-jump section (Preserved)
                CategoryQuickJumpRow(
                    importantCategories = importantCategories,
                    onEvent = onEvent,
                    navTo = navTo
                )

                Spacer(modifier = Modifier.height(8.dp)) // reduced compactness spacer
            }
            // --- End New Sections ---

            // --- 3. View All Categories Button (Preserved) ---
            Button(
                onClick = { navTo(PhotoTodoRoute.CategoryGrid) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View All Categories")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. Jump Back In Section (Preserved Urgent/Fav List) ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Text("Jump Back In", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))

            when (uiState) {
                is HomeUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                is HomeUiState.Empty -> Text("No data yet. Seed the DB!", modifier = Modifier.padding(top = 16.dp))
                is HomeUiState.Success -> {
                    if (uiState.urgentProjects.isEmpty()) {
                        Text("No urgent or favorite projects yet.", modifier = Modifier.padding(top = 16.dp))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.urgentProjects, key = { it.id }) { project ->
                                HomeProjectRow(
                                    project = project,
                                    onEvent = onEvent,
                                    navTo = navTo
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            uiState = HomeUiState.Success(
                categories = listOf(
                    CategoryOverviewUiModel(1L, "Nature", 10, 5, 0.5f),
                    CategoryOverviewUiModel(2L, "Urban", 8, 2, 0.25f)
                ),
                urgentProjects = listOf(
                    com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel(
                        id = 1L,
                        title = "Sunset shoot",
                        categoryName = "Nature",
                        isFavorite = true,
                        isUrgent = true
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    MaterialTheme {
        HomeScreen(
            uiState = HomeUiState.Loading,
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    MaterialTheme {
        HomeScreen(
            uiState = HomeUiState.Empty,
            onEvent = {},
            navTo = {}
        )
    }
}