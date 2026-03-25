package com.zoewave.probase.photodo.mobile.features.tasks.ui.detail

// import coil.compose.AsyncImage // Uncomment when Coil is added

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.zoewave.photodo.model.navigation.PhotoTodoRoute

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    onEvent: (TaskDetailEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit, // ✅ Standardized Navigation Channel
    modifier: Modifier = Modifier
) {
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var showAddTaskDialog by rememberSaveable { mutableStateOf(false) }
    var newTaskText by rememberSaveable { mutableStateOf("") }


    val context = LocalContext.current

    // 1. Check the current permission status on load
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 2. The native Compose launcher that handles the system dialog
    // 1. Just the launcher. No `hasCameraPermission` state variable needed!
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            if (uiState.loadState is DetailLoadState.Success) {
                navTo(PhotoTodoRoute.Camera(listId = uiState.loadState.taskListWithPhotos.taskList.listId))
            }
        } else {
            Toast.makeText(context, "Camera permission is required.", Toast.LENGTH_LONG).show()
        }
    }

    // Handle back presses appropriately
    BackHandler {
        if (fabMenuExpanded) fabMenuExpanded = false else navTo(null)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    val title = when (val state = uiState.loadState) {
                        is DetailLoadState.Success -> state.taskListWithPhotos.taskList.name
                        else -> "Loading..."
                    }
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(TaskDetailEvent.OnDeleteTaskListClicked) }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Delete Project")
                    }
                }
            )
        },
        floatingActionButton = {
            // Contextual FAB Menu (Only Photo and Task actions)
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
                            contentDescription = "Add Menu",
                            modifier = Modifier.animateIcon({ checkedProgress })
                        )
                    }
                }
            ) {
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false

                        // 2. Check the OS directly at the moment of the click
                        val isGranted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (isGranted) {
                            if (uiState.loadState is DetailLoadState.Success) {
                                navTo(PhotoTodoRoute.Camera(listId = uiState.loadState.taskListWithPhotos.taskList.listId))
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                    text = { Text("Take Photo") }
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        showAddTaskDialog = true
                    },
                    icon = { Icon(Icons.Default.CheckBox, contentDescription = null) },
                    text = { Text("Add Task") }
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
                    val data = state.taskListWithPhotos

                    if (data.photos.isEmpty() && data.taskItems.isEmpty()) {
                        Text(
                            text = "This project is empty.\nTap + to add tasks or photos!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 88.dp) // Space for FAB
                        ) {
                            // --- 1. PHOTOS SECTION ---
                            if (data.photos.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Context Photos",
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

                            // --- 2. CHECKLIST SECTION ---
                            if (data.taskItems.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Checklist",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                                    )
                                }
                                items(data.taskItems, key = { it.itemId }) { item ->
                                    TaskItemRow(
                                        item = item,
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

    // --- ADD TASK DIALOG ---
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = {
                newTaskText = ""
                showAddTaskDialog = false
            },
            title = { Text("New Task") },
            text = {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    placeholder = { Text("What needs to be done?") },
                    singleLine = true,
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
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = {
                    newTaskText = ""
                    showAddTaskDialog = false
                }) { Text("Cancel") }
            }
        )
    }
}