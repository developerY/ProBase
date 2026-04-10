package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
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
        SavePhotoBottomSheetContent(
            uiState = uiState,
            onCategorySelected = onCategorySelected,
            onProjectSelected = onProjectSelected,
            onNewCategoryNameChanged = onNewCategoryNameChanged,
            onNewProjectNameChanged = onNewProjectNameChanged,
            onAddCategoryClicked = onAddCategoryClicked,
            onAddProjectClicked = onAddProjectClicked,
            onSaveClicked = onSaveClicked,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun SavePhotoBottomSheetContent(
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
    Column(
        modifier = modifier
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

@Preview(showBackground = true)
@Composable
fun SavePhotoBottomSheetCategoryPreview() {
    PhotoDoTheme {
        Surface {
            SavePhotoBottomSheetContent(
                uiState = SavePhotoUiState(
                    photoUri = "",
                    categories = listOf(
                        CategoryEntity(categoryId = 1, name = "Home"),
                        CategoryEntity(categoryId = 2, name = "Work"),
                        CategoryEntity(categoryId = 3, name = "Personal")
                    )
                ),
                onCategorySelected = {},
                onProjectSelected = {},
                onNewCategoryNameChanged = {},
                onNewProjectNameChanged = {},
                onAddCategoryClicked = {},
                onAddProjectClicked = {},
                onSaveClicked = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavePhotoBottomSheetProjectPreview() {
    PhotoDoTheme {
        Surface {
            SavePhotoBottomSheetContent(
                uiState = SavePhotoUiState(
                    photoUri = "",
                    categories = listOf(
                        CategoryEntity(categoryId = 1, name = "Home"),
                        CategoryEntity(categoryId = 2, name = "Work"),
                        CategoryEntity(categoryId = 3, name = "Personal")
                    ),
                    selectedCategoryId = 1,
                    projects = listOf(
                        ProjectEntity(projectId = 1, categoryId = 1, name = "Kitchen Remodel"),
                        ProjectEntity(projectId = 2, categoryId = 1, name = "Garden Maintenance")
                    ),
                    selectedProjectId = 1
                ),
                onCategorySelected = {},
                onProjectSelected = {},
                onNewCategoryNameChanged = {},
                onNewProjectNameChanged = {},
                onAddCategoryClicked = {},
                onAddProjectClicked = {},
                onSaveClicked = {},
                onDismiss = {}
            )
        }
    }
}
