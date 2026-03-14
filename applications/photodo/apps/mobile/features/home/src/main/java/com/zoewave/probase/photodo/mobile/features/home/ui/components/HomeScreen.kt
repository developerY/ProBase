package com.zoewave.probase.photodo.mobile.features.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.photodo.model.PhotoTask
import com.zoewave.probase.photodo.mobile.features.home.ui.HomeEvent
import com.zoewave.probase.photodo.mobile.features.home.ui.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Recent Tasks",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(uiState.recentPhotoTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = { isChecked ->
                            onEvent(HomeEvent.OnTaskToggled(task.id, isChecked))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: PhotoTask,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onToggle
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview_Loading() {
    HomeScreen(
        uiState = HomeUiState(isLoading = true),
        onEvent = {}
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview_Success() {
    HomeScreen(
        uiState = HomeUiState(
            isLoading = false,
            recentPhotoTasks = listOf(
                PhotoTask("1", "Preview Task 1", false),
                PhotoTask("2", "Preview Completed Task", true)
            )
        ),
        onEvent = {}
    )
}