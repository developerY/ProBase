package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    navTo: (PhotoTodoRoute) -> Unit, // ✅ Your strict navigation rule applied!
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Overview") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("📈 Graphic & AI Agent Placeholder", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                // ✅ Using the unified navTo channel
                onClick = { navTo(PhotoTodoRoute.CategoryGrid) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View All Categories")
            }

            Spacer(modifier = Modifier.height(32.dp))

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
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        // ✅ Using the unified navTo channel for the deep link
                                        navTo(PhotoTodoRoute.TaskDetail(listId = project.id, listTitle = project.title))
                                    },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (project.isUrgent) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val icon = when {
                                                project.isUrgent -> Icons.Default.Error
                                                project.isFavorite -> Icons.Default.Favorite
                                                else -> Icons.Default.Star
                                            }
                                            val tint = when {
                                                project.isUrgent -> MaterialTheme.colorScheme.error
                                                project.isFavorite -> MaterialTheme.colorScheme.tertiary
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                            Icon(imageVector = icon, contentDescription = null, tint = tint)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(project.title, style = MaterialTheme.typography.bodyLarge)
                                                Text(project.categoryName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Icon(Icons.Default.ChevronRight, contentDescription = "Go")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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
                    ProjectListUiModel(
                        id = 1L,
                        title = "Sunset shoot",
                        categoryName = "Nature",
                        isFavorite = true,
                        isUrgent = true
                    ),
                    ProjectListUiModel(
                        id = 2L,
                        title = "Street photography",
                        categoryName = "Urban",
                        isFavorite = false,
                        isUrgent = true
                    ),
                    ProjectListUiModel(
                        id = 3L,
                        title = "Night sky",
                        categoryName = "Nature",
                        isFavorite = true,
                        isUrgent = false
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