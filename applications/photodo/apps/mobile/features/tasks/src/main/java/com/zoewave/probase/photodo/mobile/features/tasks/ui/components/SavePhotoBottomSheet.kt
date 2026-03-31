package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.tasks.ui.SavePhotoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavePhotoBottomSheet(
    uiState: SavePhotoUiState,
    onCategorySelected: (Long) -> Unit,
    onProjectSelected: (Long) -> Unit,
    onNewCategoryNameChanged: (String) -> Unit,
    onNewProjectNameChanged: (String) -> Unit,
    onAddCategoryClicked: () -> Unit,
    onAddProjectClicked: () -> Unit,
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
                // Show Category Selection
                Text(text = "Select Category", style = MaterialTheme.typography.titleMedium)
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.newCategoryName,
                        onValueChange = onNewCategoryNameChanged,
                        label = { Text("New Category Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (uiState.newCategoryName.isNotBlank()) onAddCategoryClicked()
                        })
                    )
                    Button(
                        onClick = onAddCategoryClicked,
                        enabled = uiState.newCategoryName.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(uiState.categories) { category ->
                        ListItem(
                            headlineContent = { Text(category.name) },
                            leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                            modifier = Modifier.clickable { onCategorySelected(category.categoryId) }
                        )
                    }
                }
            } else {
                // Show Project Selection
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onCategorySelected(-1L) /* Use a marker to go back */ }) {
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

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.newProjectName,
                        onValueChange = onNewProjectNameChanged,
                        label = { Text("New Project Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (uiState.newProjectName.isNotBlank()) onAddProjectClicked()
                        })
                    )
                    Button(
                        onClick = onAddProjectClicked,
                        enabled = uiState.newProjectName.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }

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
                    onClick = if (uiState.isSaved) onDismiss else onSaveClicked,
                    enabled = (uiState.isSaved || uiState.selectedProjectId != null || uiState.newProjectName.isNotBlank()) && !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = if (uiState.isSaved) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) else ButtonDefaults.buttonColors()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(if (uiState.isSaved) "Saved! Close" else "Save Photo")
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
