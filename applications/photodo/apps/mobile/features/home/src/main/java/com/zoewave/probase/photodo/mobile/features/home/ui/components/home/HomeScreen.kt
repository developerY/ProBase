package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
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
        topBar = { TopAppBar(title = { Text("Overview") }) }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp), // Only pad the sides, let the list handle vertical padding
            contentPadding = PaddingValues(vertical = 16.dp), // Padding at top and bottom of scroll
            verticalArrangement = Arrangement.spacedBy(24.dp), // 🚀 MAGIC: Replaces all your Spacers!
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- 🚀 NEW: Graphic/AI Overview Section (Derivative State) ---
            if (uiState is HomeUiState.Success) {
                // Compute the models on recomposition
                item {
                    // 🚀 1. The main "High-Density Wheel" summary card
                    OverviewSummaryCard(categories = uiState.categories) // ✅ Pass list directly
                }

                item {
                    val importantCategories = remember(uiState.categories) {
                        // Logic: Categories with most pending tasks
                        uiState.categories
                            .sortedByDescending { it.totalTasks - it.completedTasks }
                            .take(5) // Show top 5
                    }
                    // 2. The horizontal quick-jump section (Preserved)
                    CategoryQuickJumpRow(
                        importantCategories = importantCategories,
                        onEvent = onEvent,
                        navTo = navTo
                    )
                }
            }
            // --- End New Sections ---

            item {            // --- 3. View All Categories Button (Preserved) ---
                Button(
                    onClick = { navTo(PhotoTodoRoute.CategoryGrid) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View All Categories")
                }
            }

            item {
                // --- 4. Jump Back In Section (Preserved Urgent/Fav List) ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Text(
                        "Jump Back In",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            when (uiState) {
                is HomeUiState.Loading -> {
                    item { CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp)) }
                }

                is HomeUiState.Empty -> {
                    item {
                        Text(
                            "No data yet. Seed the DB!",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                is HomeUiState.Success -> {
                    if (uiState.urgentProjects.isEmpty()) {
                        item {
                            Text(
                                "No urgent or favorite projects yet.",
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    } else {
                        // Use `items` for the dynamic data! It scrolls seamlessly with the `item` blocks above.
                        items(items = uiState.urgentProjects, key = { it.projectId }) { project ->
                            HomeProjectRow(
                                project = project,
                                onEvent = onEvent, // Pass the channel down
                                navTo = navTo      // Pass the channel down
                            )
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
    PhotoDoTheme {
        HomeScreen(
            uiState = HomeUiState.Success(
                categories = listOf(
                    CategoryOverviewUiModel(1L, "Nature", 10, 5, 0.5f),
                    CategoryOverviewUiModel(2L, "Urban", 8, 2, 0.25f)
                ),
                urgentProjects = listOf(
                    com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel(
                        projectId = 1L,
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
    PhotoDoTheme {
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
    PhotoDoTheme {
        HomeScreen(
            uiState = HomeUiState.Empty,
            onEvent = {},
            navTo = {}
        )
    }
}