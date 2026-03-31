package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.tasks.ui.SavePhotoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavePhotoBottomSheet(
    uiState: SavePhotoUiState,
    onCategorySelected: (Long) -> Unit,
    onProjectSelected: (Long) -> Unit,
    onSaveClicked: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Save to which project?",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (uiState.selectedCategoryId == null) {
                // Show Categories
                Text(text = "Select Category", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(uiState.categories) { category ->
                        ListItem(
                            headlineContent = { Text(category.name) },
                            leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                            modifier = Modifier.clickable { onCategorySelected(category.categoryId) }
                        )
                    }
                }
            } else {
                // Show Projects
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { /* Could implement back to categories if needed */ }) {
                        Text("Categories")
                    }
                    Text(">")
                    Text(
                        uiState.categories.find { it.categoryId == uiState.selectedCategoryId }?.name ?: "",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Text(
                    text = "Select Project",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(uiState.projects) { project ->
                        ListItem(
                            headlineContent = { Text(project.name) },
                            leadingContent = { Icon(Icons.Default.List, contentDescription = null) },
                            trailingContent = {
                                if (uiState.selectedProjectId == project.projectId) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            },
                            modifier = Modifier.clickable { onProjectSelected(project.projectId) }
                        )
                    }
                }

                Button(
                    onClick = onSaveClicked,
                    enabled = uiState.selectedProjectId != null && !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Save Photo")
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
