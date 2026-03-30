package com.zoewave.probase.photodo.mobile.features.tasks.ui.detail

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.zoewave.probase.applications.photodo.db.entity.ExpenseEntity
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectDetails
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.photodo.mobile.core.ui.components.BudgetProgressBar
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.tasks.R
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    onEvent: (TaskDetailEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit,
    modifier: Modifier = Modifier
) {
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    // Task Dialog State
    var showAddTaskDialog by rememberSaveable { mutableStateOf(false) }
    var newTaskText by rememberSaveable { mutableStateOf("") }

    // 🚀 Expense Dialog State
    var showAddExpenseDialog by rememberSaveable { mutableStateOf(false) }
    var newExpenseAmount by rememberSaveable { mutableStateOf("") }
    var newExpenseDesc by rememberSaveable { mutableStateOf("") }

    // 🚀 Confirmation Dialog States
    var showDeleteProjectConfirmation by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val state = uiState.loadState
            if (state is DetailLoadState.Success) {
                navTo(PhotoTodoRoute.Camera(projectId = state.projectDetails.project.projectId))
            }
        } else {
            Toast.makeText(context, context.getString(R.string.applications_photodo_apps_mobile_features_tasks_detail_camera_permission_required), Toast.LENGTH_LONG).show()
        }
    }

    BackHandler {
        if (fabMenuExpanded) fabMenuExpanded = false else navTo(null)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    val title = when (val state = uiState.loadState) {
                        is DetailLoadState.Success -> state.projectDetails.project.name
                        else -> stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_loading)
                    }
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(null) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_back_content_desc)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteProjectConfirmation = true }) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_delete_project_content_desc)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButtonMenu(
                expanded = fabMenuExpanded,
                button = {
                    ToggleFloatingActionButton(
                        checked = fabMenuExpanded,
                        onCheckedChange = { fabMenuExpanded = !fabMenuExpanded }
                    ) {
                        val imageVector by remember {
                            derivedStateOf { if (checkedProgress > 0.5f) Icons.Default.Close else Icons.Default.Add }
                        }
                        Icon(
                            painter = rememberVectorPainter(imageVector),
                            contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_add_menu_content_desc),
                            modifier = Modifier.animateIcon({ checkedProgress })
                        )
                    }
                }
            ) {
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        val isGranted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (isGranted) {
                            val state = uiState.loadState
                            if (state is DetailLoadState.Success) {
                                navTo(PhotoTodoRoute.Camera(projectId = state.projectDetails.project.projectId))
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                    text = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_take_photo)) }
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        showAddTaskDialog = true
                    },
                    icon = { Icon(Icons.Default.CheckBox, contentDescription = null) },
                    text = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_add_task)) }
                )
                // 🚀 NEW: Add Expense FAB Item
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        showAddExpenseDialog = true
                    },
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    text = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_add_expense)) }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState.loadState) {
                is DetailLoadState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailLoadState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DetailLoadState.Success -> {
                    val data = state.projectDetails
                    val dueDateMillis = data.project.dueDate // Assuming it's in your ProjectEntity!

                    // 🚀 Extract budget data safely from the Project entity
                    val totalBudget = data.project.projectBudget ?: 0.0
                    val currentSpend = data.project.currentSpend ?: 0.0
                    val hasBudget = totalBudget > 0

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {

                        // 🚀 1. THE DUE DATE DISPLAY
                        if (dueDateMillis != null) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 16.dp, bottom = 8.dp), // Spacing to separate from top bar and budget
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_due_date_content_desc),
                                        tint = MaterialTheme.colorScheme.primary, // Matches your theme
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

                                    val formatter = remember { java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault()) }
                                    val dateStr = formatter.format(java.util.Date(dueDateMillis))

                                    Text(
                                        text = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_due_date, dateStr),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // 1. THE BUDGET PROGRESS BAR (Always at the top if it exists)
                        if (hasBudget) {
                            item {
                                BudgetProgressBar(
                                    projectName = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_total_budget),
                                    currentSpend = currentSpend,
                                    projectBudget = totalBudget,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                                )
                            }
                            item {
                                QuickExpenseBar(
                                    onAdjustSpend = { adjustmentAmount ->
                                        // Automatically generate a generic description based on + or -
                                        val description = if (adjustmentAmount > 0) {
                                            context.getString(R.string.applications_photodo_apps_mobile_features_tasks_detail_quick_edit_plus)
                                        } else {
                                            context.getString(R.string.applications_photodo_apps_mobile_features_tasks_detail_quick_edit_minus)
                                        }

                                        // Fire the exact same event the full dialog uses!
                                        onEvent(
                                            TaskDetailEvent.OnAddExpenseClicked(
                                                description = description,
                                                amount = adjustmentAmount
                                            )
                                        )
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
                                )
                            }
                        }

                        // 2. EMPTY STATE (Moved inside the list so the budget bar doesn't vanish!)
                        if (data.photos.isEmpty() && data.tasks.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_empty_state),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 64.dp), // Push it down a bit
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {

                            // 3. PHOTOS
                            if (data.photos.isNotEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_context_photos),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                                    )
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(data.photos, key = { it.photoId }) { photo ->
                                            PhotoThumbnailCard(
                                                photo = photo,
                                                onEvent = onEvent,
                                                navTo = navTo
                                            )
                                        }
                                    }
                                }
                            }

                            // 4. TASKS
                            if (data.tasks.isNotEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_checklist),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                                    )
                                }
                                items(data.tasks, key = { it.taskId }) { task ->
                                    TaskItemRow(
                                        task = task,
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

    // --- DIALOGS ---

    // 1. Add Task Dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = {
                newTaskText = ""
                showAddTaskDialog = false
            },
            title = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_new_task_title)) },
            text = {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    placeholder = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_new_task_placeholder)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTaskText.isNotBlank()) {
                            onEvent(TaskDetailEvent.OnAddItemClicked(newTaskText.trim()))
                            newTaskText = ""
                            showAddTaskDialog = false
                        }
                    }
                ) { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_add_button)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    newTaskText = ""
                    showAddTaskDialog = false
                }) { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_cancel_button)) }
            }
        )
    }

    // 🚀 2. NEW: Add Expense Dialog
    if (showAddExpenseDialog) {
        AlertDialog(
            onDismissRequest = {
                newExpenseAmount = ""
                newExpenseDesc = ""
                showAddExpenseDialog = false
            },
            title = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_add_expense)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newExpenseDesc,
                        onValueChange = { newExpenseDesc = it },
                        label = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_expense_description_label)) },
                        placeholder = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_expense_description_placeholder)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newExpenseAmount,
                        onValueChange = { newExpenseAmount = it },
                        label = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_expense_amount_label)) },
                        placeholder = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_expense_amount_placeholder)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = newExpenseAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0 && newExpenseDesc.isNotBlank()) {
                            // 👇 MAKE SURE TO ADD THIS EVENT TO YOUR SEALED CLASS
                            onEvent(TaskDetailEvent.OnAddExpenseClicked(description = newExpenseDesc.trim(), amount = amount))
                            newExpenseAmount = ""
                            newExpenseDesc = ""
                            showAddExpenseDialog = false
                        }
                    }
                ) { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_add_button)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    newExpenseAmount = ""
                    newExpenseDesc = ""
                    showAddExpenseDialog = false
                }) { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_cancel_button)) }
            }
        )
    }

    if (showDeleteProjectConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteProjectConfirmation = false },
            title = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_delete_project_title)) },
            text = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_delete_project_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteProjectConfirmation = false
                        onEvent(TaskDetailEvent.OnDeleteTaskListClicked)
                    }
                ) {
                    Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_delete_button), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteProjectConfirmation = false }) {
                    Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_cancel_button))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskDetailScreenPreview() {
    val sampleProject = ProjectEntity(
        projectId = 1,
        categoryId = 1,
        name = "Kitchen Renovation",
        projectBudget = 500.0,
        currentSpend = 150.0
    )
    val sampleTasks = listOf(
        TaskEntity(taskId = 1, projectId = 1, text = "Buy white paint", isChecked = false),
        TaskEntity(taskId = 2, projectId = 1, text = "Measure cabinets", isChecked = true)
    )
    val samplePhotos = listOf(
        PhotoEntity(photoId = 1, projectId = 1, photoUri = "content://media/external/images/media/1")
    )
    val sampleExpenses = listOf(
        ExpenseEntity(expenseId = 1, projectId = 1, description = "Paint", amount = 50.0)
    )
    val sampleProjectDetails = ProjectDetails(
        project = sampleProject,
        tasks = sampleTasks,
        photos = samplePhotos,
        expenses = sampleExpenses
    )
    val sampleUiState = TaskDetailUiState(
        loadState = DetailLoadState.Success(sampleProjectDetails)
    )

    PhotoDoTheme {
        TaskDetailScreen(
            uiState = sampleUiState,
            onEvent = {},
            navTo = {}
        )
    }
}
