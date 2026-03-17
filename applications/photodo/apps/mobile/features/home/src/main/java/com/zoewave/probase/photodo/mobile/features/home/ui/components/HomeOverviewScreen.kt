package com.zoewave.probase.photodo.mobile.features.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.home.ui.CategoryOverviewUiModel
import com.zoewave.probase.photodo.mobile.features.home.ui.HomeEvent
import com.zoewave.probase.photodo.mobile.features.home.ui.HomeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeOverviewScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Overview",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* Optional global action like settings or profile */ }) {
                        Icon(Icons.Default.Analytics, contentDescription = "Analytics")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HomeUiState.Empty -> {
                    EmptyHomeState(modifier = Modifier.align(Alignment.Center))
                }
                is HomeUiState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2), // 2-column dashboard layout
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = uiState.categories,
                            key = { _, category -> category.id }
                        ) { index, category ->
                            CategoryDashboardCard(
                                category = category,
                                index = index,
                                onClick = {
                                    onEvent(HomeEvent.OnCategoryClicked(category.id, category.name))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDashboardCard(
    category: CategoryOverviewUiModel,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamically cycle through Material 3 expressive container colors!
    val (containerColor, contentColor) = when (index % 4) {
        0 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        2 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), // Expressive, large rounded corners
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row: Icon and Percentage Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FolderSpecial,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = category.progressText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Middle: Category Name & Task Count
            Column {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${category.completedTasks}/${category.totalTasks} Tasks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f) // Slightly dim the subtitle
                )
            }

            // Bottom: Thicker, expressive progress bar
            LinearProgressIndicator(
                progress = { category.progressPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)), // Rounded ends for the track
                color = contentColor, // Use the high-contrast text color for the bar
                trackColor = contentColor.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
fun EmptyHomeState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.FolderSpecial,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Welcome to PhotoDo!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Head over to the Tasks tab to create your first category and start building projects.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true, name = "1. Dashboard Populated")
@Composable
private fun HomeOverviewScreenPopulatedPreview() {
    MaterialTheme {
        val mockData = listOf(
            CategoryOverviewUiModel(
                id = 1L,
                name = "Real Estate",
                totalTasks = 24,
                completedTasks = 18,
                progressPercentage = 0.75f
            ),
            CategoryOverviewUiModel(
                id = 2L,
                name = "Development",
                totalTasks = 50,
                completedTasks = 5,
                progressPercentage = 0.10f
            ),
            CategoryOverviewUiModel(
                id = 3L,
                name = "Business",
                totalTasks = 12,
                completedTasks = 12,
                progressPercentage = 1.0f
            ),
            CategoryOverviewUiModel(
                id = 4L,
                name = "Personal",
                totalTasks = 8,
                completedTasks = 4,
                progressPercentage = 0.50f
            ),
            CategoryOverviewUiModel(
                id = 5L,
                name = "Hobbies",
                totalTasks = 0,
                completedTasks = 0,
                progressPercentage = 0.0f
            )
        )

        Surface {
            HomeOverviewScreen(
                uiState = HomeUiState.Success(mockData),
                onEvent = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "2. Dashboard Empty")
@Composable
private fun HomeOverviewScreenEmptyPreview() {
    MaterialTheme {
        Surface {
            HomeOverviewScreen(
                uiState = HomeUiState.Empty,
                onEvent = {}
            )
        }
    }
}