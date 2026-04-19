package com.zoewave.probase.photodo.features.camera.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Flag
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
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.ui.components.QuickExpenseBar
import com.zoewave.probase.photodo.features.camera.R
import com.zoewave.probase.photodo.features.camera.ui.SavePhotoEvent
import com.zoewave.probase.photodo.features.camera.ui.state.SavePhotoUiState
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavePhotoBottomSheet(
    uiState: SavePhotoUiState,
    onEvent: (SavePhotoEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDatePicker by remember { mutableStateOf(value = false) }

    ModalBottomSheet(
        onDismissRequest = { onEvent(SavePhotoEvent.OnDismiss) },
        sheetState = sheetState,
        modifier = modifier.imePadding()
    ) {
        SavePhotoForm(
            uiState = uiState,
            onEvent = onEvent,
            onShowDatePicker = { showDatePicker = true }
        )

        if (showDatePicker) {
            com.zoewave.probase.photodo.mobile.features.tasks.ui.components.PhotoDoDatePicker(
                onDateSelected = { 
                    onEvent(SavePhotoEvent.OnDueDateChanged(it))
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
    onEvent: (SavePhotoEvent) -> Unit,
    onShowDatePicker: () -> Unit
) {
    val themeColor = if (uiState.isFromAi) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary

    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var isProjectDropdownExpanded by remember { mutableStateOf(false) }

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
                    text = if (uiState.isFromAi) stringResource(R.string.applications_photodo_features_camera_ai_smart_task) else stringResource(R.string.applications_photodo_features_camera_create_task),
                    style = MaterialTheme.typography.headlineSmall,
                    color = themeColor
                )
            }
            IconButton(onClick = { onEvent(SavePhotoEvent.OnDismiss) }) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.applications_photodo_features_camera_close))
            }
        }

        // --- SECTION 1: CATEGORY ---
        val categorySuggestions = listOf(
            stringResource(R.string.applications_photodo_apps_mobile_features_home_suggestion_home) to Icons.Default.Home,
            stringResource(R.string.applications_photodo_apps_mobile_features_home_suggestion_work) to Icons.Default.Business,
            stringResource(R.string.applications_photodo_apps_mobile_features_home_suggestion_personal) to Icons.Default.BeachAccess,
            stringResource(R.string.applications_photodo_apps_mobile_features_home_suggestion_shopping) to Icons.Default.ShoppingCart,
            stringResource(R.string.applications_photodo_apps_mobile_features_home_suggestion_health) to Icons.Default.Favorite
        )

        FormSection(title = stringResource(R.string.applications_photodo_features_camera_category_section), themeColor = themeColor) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownExpanded,
                    onExpandedChange = { isCategoryDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AiEnhancedTextField(
                        value = uiState.categoryName,
                        onValueChange = { onEvent(SavePhotoEvent.OnCategoryNameChanged(it)) },
                        label = stringResource(R.string.applications_photodo_features_camera_category_name_label),
                        isAiGenerated = uiState.aiGeneratedFields.contains("category"),
                        onReportClick = { onEvent(SavePhotoEvent.OnReportIssue) },
                        onClearAiClick = { onEvent(SavePhotoEvent.OnClearAiData) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) 
                        },
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
                                    onEvent(SavePhotoEvent.OnCategoryNameChanged(category.name))
                                    isCategoryDropdownExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
                
                QuickIconRow(
                    items = categorySuggestions,
                    onItemSelected = { onEvent(SavePhotoEvent.OnCategoryNameChanged(it)) },
                    themeColor = themeColor
                )
            }
        }

        // --- SECTION 2: PROJECT ---
        val projectTemplates = listOf(
            stringResource(com.zoewave.probase.photodo.mobile.features.home.R.string.applications_photodo_apps_mobile_features_home_template_kitchen) to Icons.Default.Kitchen,
            stringResource(com.zoewave.probase.photodo.mobile.features.home.R.string.applications_photodo_apps_mobile_features_home_template_garden) to Icons.Default.Yard,
            stringResource(com.zoewave.probase.photodo.mobile.features.home.R.string.applications_photodo_apps_mobile_features_home_template_car) to Icons.Default.DirectionsCar,
            stringResource(com.zoewave.probase.photodo.mobile.features.home.R.string.applications_photodo_apps_mobile_features_home_template_office) to Icons.Default.Business,
            stringResource(com.zoewave.probase.photodo.mobile.features.home.R.string.applications_photodo_apps_mobile_features_home_suggestion_home) to Icons.Default.Home,
            stringResource(com.zoewave.probase.photodo.mobile.features.home.R.string.applications_photodo_apps_mobile_features_home_template_holiday) to Icons.Default.BeachAccess
        )

        FormSection(title = stringResource(R.string.applications_photodo_features_camera_project_details_section), themeColor = themeColor) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = isProjectDropdownExpanded,
                    onExpandedChange = { isProjectDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AiEnhancedTextField(
                        value = uiState.projectName,
                        onValueChange = { onEvent(SavePhotoEvent.OnProjectNameChanged(it)) },
                        label = stringResource(R.string.applications_photodo_features_camera_project_name_label),
                        isAiGenerated = uiState.aiGeneratedFields.contains("project"),
                        onReportClick = { onEvent(SavePhotoEvent.OnReportIssue) },
                        onClearAiClick = { onEvent(SavePhotoEvent.OnClearAiData) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isProjectDropdownExpanded) 
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = isProjectDropdownExpanded,
                        onDismissRequest = { isProjectDropdownExpanded = false }
                    ) {
                        // Show unique project names from current category if possible, or all projects
                        val filteredProjects = uiState.projects
                            .filter { it.name.isNotBlank() }
                            .distinctBy { it.name }
                        
                        filteredProjects.forEach { project ->
                            DropdownMenuItem(
                                text = { Text(project.name) },
                                onClick = {
                                    onEvent(SavePhotoEvent.OnProjectNameChanged(project.name))
                                    isProjectDropdownExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                QuickIconRow(
                    items = projectTemplates,
                    onItemSelected = { onEvent(SavePhotoEvent.OnProjectNameChanged(it)) },
                    themeColor = themeColor
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AiEnhancedTextField(
                        value = uiState.duration,
                        onValueChange = { onEvent(SavePhotoEvent.OnDurationChanged(it)) },
                        label = stringResource(R.string.applications_photodo_features_camera_duration_label),
                        isAiGenerated = uiState.aiGeneratedFields.contains("duration"),
                        onReportClick = { onEvent(SavePhotoEvent.OnReportIssue) },
                        onClearAiClick = { onEvent(SavePhotoEvent.OnClearAiData) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                        placeholder = stringResource(R.string.applications_photodo_features_camera_duration_placeholder)
                    )

                    val dateText = uiState.dueDateMillis?.let {
                        SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it))
                    } ?: stringResource(R.string.applications_photodo_features_camera_set_date)
                    
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
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Text(dateText)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AiEnhancedTextField(
                        value = uiState.budgetInput,
                        onValueChange = { onEvent(SavePhotoEvent.OnBudgetInputChanged(it)) },
                        label = stringResource(R.string.applications_photodo_features_camera_total_budget_label),
                        isAiGenerated = uiState.aiGeneratedFields.contains("budget"),
                        onReportClick = { onEvent(SavePhotoEvent.OnReportIssue) },
                        onClearAiClick = { onEvent(SavePhotoEvent.OnClearAiData) },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text(stringResource(R.string.applications_photodo_features_camera_currency_symbol)) }
                    )
                    QuickExpenseBar(onAdjustAmount = { onEvent(SavePhotoEvent.OnAdjustBudget(it)) })
                }
            }
        }

        // --- SECTION 3: TASK ---
        val taskTemplates = listOf(
            stringResource(com.zoewave.probase.photodo.mobile.features.home.R.string.applications_photodo_apps_mobile_features_home_task_fix) to Icons.Default.Build,
            stringResource(com.zoewave.probase.photodo.mobile.features.home.R.string.applications_photodo_apps_mobile_features_home_task_buy) to Icons.Default.ShoppingCart,
            stringResource(com.zoewave.probase.photodo.mobile.features.home.R.string.applications_photodo_apps_mobile_features_home_task_clean) to Icons.Default.CleaningServices,
            stringResource(com.zoewave.probase.photodo.mobile.features.home.R.string.applications_photodo_apps_mobile_features_home_task_call) to Icons.Default.Call,
            stringResource(com.zoewave.probase.photodo.mobile.features.home.R.string.applications_photodo_apps_mobile_features_home_task_find) to Icons.Default.Search,
            stringResource(com.zoewave.probase.photodo.mobile.features.home.R.string.applications_photodo_apps_mobile_features_home_task_organize) to Icons.Default.Layers
        )

        FormSection(title = stringResource(R.string.applications_photodo_features_camera_task_section), themeColor = themeColor) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AiEnhancedTextField(
                    value = uiState.taskName,
                    onValueChange = { onEvent(SavePhotoEvent.OnTaskNameChanged(it)) },
                    label = stringResource(R.string.applications_photodo_features_camera_main_task_name_label),
                    isAiGenerated = uiState.aiGeneratedFields.contains("task"),
                    onReportClick = { onEvent(SavePhotoEvent.OnReportIssue) },
                    onClearAiClick = { onEvent(SavePhotoEvent.OnClearAiData) },
                    modifier = Modifier.fillMaxWidth()
                )

                QuickIconRow(
                    items = taskTemplates,
                    onItemSelected = { onEvent(SavePhotoEvent.OnTaskNameChanged(it)) },
                    themeColor = themeColor
                )
            }
        }

        // --- SAVE BUTTON ---
        Button(
            onClick = { onEvent(SavePhotoEvent.OnSaveClicked) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !uiState.isSaving && uiState.taskName.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = themeColor)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text(stringResource(R.string.applications_photodo_features_camera_create_view_project))
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AiEnhancedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isAiGenerated: Boolean,
    onReportClick: () -> Unit,
    onClearAiClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: String? = null,
    prefix: @Composable (() -> Unit)? = null,
    colors: androidx.compose.material3.TextFieldColors = androidx.compose.material3.OutlinedTextFieldDefaults.colors()
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.combinedClickable(
        onLongClick = { if (isAiGenerated) showMenu = true },
        onClick = { /* Default focus behavior */ }
    )) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            prefix = prefix,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = if (isAiGenerated) {
                androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)
                )
            } else colors,
            trailingIcon = {
                if (isAiGenerated) {
                    Icon(
                        Icons.Default.AutoAwesome, 
                        stringResource(R.string.applications_photodo_features_camera_ai_generated_content_desc), 
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    trailingIcon?.invoke()
                }
            }
        )

        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.applications_photodo_features_camera_clear_ai_data)) },
                onClick = {
                    showMenu = false
                    onClearAiClick()
                },
                leadingIcon = { Icon(Icons.Default.Close, null) }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.applications_photodo_features_camera_report_bad_ai_output)) },
                onClick = {
                    showMenu = false
                    onReportClick()
                },
                leadingIcon = { Icon(Icons.Default.Flag, null) }
            )
        }
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
