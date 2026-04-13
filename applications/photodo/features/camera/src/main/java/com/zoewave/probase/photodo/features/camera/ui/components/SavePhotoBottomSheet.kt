package com.zoewave.probase.photodo.features.camera.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.ui.components.QuickExpenseBar
import com.zoewave.probase.photodo.features.camera.ui.state.SavePhotoUiState
import com.zoewave.probase.photodo.mobile.features.tasks.ui.components.PhotoDoDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavePhotoBottomSheet(
    uiState: SavePhotoUiState,
    onCategoryNameChanged: (String) -> Unit,
    onProjectNameChanged: (String) -> Unit,
    onTaskNameChanged: (String) -> Unit,
    onDurationChanged: (String) -> Unit,
    onBudgetInputChanged: (String) -> Unit,
    onAdjustBudget: (Double) -> Unit,
    onDueDateChanged: (Long?) -> Unit,
    onAddSubTask: (String) -> Unit,
    onRemoveSubTask: (Int) -> Unit,
    onSaveClicked: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.imePadding()
    ) {
        SavePhotoForm(
            uiState = uiState,
            onCategoryNameChanged = onCategoryNameChanged,
            onProjectNameChanged = onProjectNameChanged,
            onTaskNameChanged = onTaskNameChanged,
            onDurationChanged = onDurationChanged,
            onBudgetInputChanged = onBudgetInputChanged,
            onAdjustBudget = onAdjustBudget,
            onShowDatePicker = { showDatePicker = true },
            onAddSubTask = onAddSubTask,
            onRemoveSubTask = onRemoveSubTask,
            onSaveClicked = onSaveClicked,
            onDismiss = onDismiss
        )

        if (showDatePicker) {
            PhotoDoDatePicker(
                onDateSelected = { 
                    onDueDateChanged(it)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SavePhotoForm(
    uiState: SavePhotoUiState,
    onCategoryNameChanged: (String) -> Unit,
    onProjectNameChanged: (String) -> Unit,
    onTaskNameChanged: (String) -> Unit,
    onDurationChanged: (String) -> Unit,
    onBudgetInputChanged: (String) -> Unit,
    onAdjustBudget: (Double) -> Unit,
    onShowDatePicker: () -> Unit,
    onAddSubTask: (String) -> Unit,
    onRemoveSubTask: (Int) -> Unit,
    onSaveClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    val themeColor = if (uiState.isFromAi) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val containerColor = if (uiState.isFromAi) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f) else Color.Transparent

    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (uiState.isFromAi) {
                    Icon(
                        Icons.Default.AutoAwesome, 
                        contentDescription = null, 
                        tint = themeColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (uiState.isFromAi) "AI Smart Task" else "Create Task",
                    style = MaterialTheme.typography.headlineSmall,
                    color = themeColor
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        // --- SECTION 1: CATEGORY ---
        val categorySuggestions = listOf(
            "Home" to Icons.Default.Home,
            "Work" to Icons.Default.Business,
            "Personal" to Icons.Default.BeachAccess,
            "Shopping" to Icons.Default.ShoppingCart,
            "Health" to Icons.Default.Favorite
        )

        FormSection(title = "Category", themeColor = themeColor) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownExpanded,
                    onExpandedChange = { isCategoryDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.categoryName,
                        onValueChange = onCategoryNameChanged,
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false }
                    ) {
                        uiState.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    onCategoryNameChanged(category.name)
                                    isCategoryDropdownExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
                
                QuickIconRow(
                    items = categorySuggestions,
                    onItemSelected = onCategoryNameChanged,
                    themeColor = themeColor
                )
            }
        }

        // --- SECTION 2: PROJECT ---
        val projectTemplates = listOf(
            "Kitchen" to Icons.Default.Kitchen,
            "Garden" to Icons.Default.Yard,
            "Car" to Icons.Default.DirectionsCar,
            "Office" to Icons.Default.Business,
            "Home" to Icons.Default.Home,
            "Holiday" to Icons.Default.BeachAccess
        )

        FormSection(title = "Project Details", themeColor = themeColor) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.projectName,
                    onValueChange = onProjectNameChanged,
                    label = { Text("Project Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                QuickIconRow(
                    items = projectTemplates,
                    onItemSelected = onProjectNameChanged,
                    themeColor = themeColor
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.duration,
                        onValueChange = onDurationChanged,
                        label = { Text("Duration") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                        placeholder = { Text("e.g. 2h") },
                        singleLine = true
                    )

                    val dateText = uiState.dueDateMillis?.let {
                        SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it))
                    } ?: "Set Date"
                    
                    Button(
                        onClick = onShowDatePicker,
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(dateText)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.budgetInput,
                        onValueChange = onBudgetInputChanged,
                        label = { Text("Total Budget") },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("$") },
                        singleLine = true
                    )
                    QuickExpenseBar(onAdjustAmount = onAdjustBudget)
                }
            }
        }

        // --- SECTION 3: TASK ---
        val taskTemplates = listOf(
            "Fix" to Icons.Default.Build,
            "Buy" to Icons.Default.ShoppingCart,
            "Clean" to Icons.Default.CleaningServices,
            "Call" to Icons.Default.Call,
            "Find" to Icons.Default.Search,
            "Organize" to Icons.Default.Layers
        )

        FormSection(title = "Task", themeColor = themeColor) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.taskName,
                    onValueChange = onTaskNameChanged,
                    label = { Text("Main Task Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                QuickIconRow(
                    items = taskTemplates,
                    onItemSelected = onTaskNameChanged,
                    themeColor = themeColor
                )
            }
        }

        // --- SAVE BUTTON ---
        Button(
            onClick = onSaveClicked,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !uiState.isSaving && uiState.taskName.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = themeColor)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Create & View Project")
            }
        }
    }
}

@Composable
private fun FormSection(title: String, themeColor: Color, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelLarge, color = themeColor)
        content()
    }
}

@Composable
private fun QuickIconRow(
    items: List<Pair<String, androidx.compose.ui.graphics.vector.ImageVector>>,
    onItemSelected: (String) -> Unit,
    themeColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items.forEach { (name, icon) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { onItemSelected(name) }
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = themeColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = name, modifier = Modifier.size(20.dp))
                    }
                }
                Text(text = name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
