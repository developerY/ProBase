package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TasksUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TasksListScreen(
    uiState: TasksUiState,
    onEvent: (TasksEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track whether the Speed Dial is open
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    // Ensure the system back button closes the menu if it's open, rather than closing the app
    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            /* ... keep the TopAppBar we built earlier ... */
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
                            derivedStateOf {
                                if (checkedProgress > 0.5f) Icons.Default.Close else Icons.Default.Add
                            }
                        }
                        Icon(
                            painter = rememberVectorPainter(imageVector),
                            contentDescription = "Toggle Add Menu",
                            modifier = Modifier.animateIcon({ checkedProgress })
                        )
                    }
                }
            ) {
                // 1. Add Photo
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onEvent(TasksEvent.OnAddPhotoClicked)
                    },
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                    text = { Text("Take Photo") }
                )
                // 2. Add Task Item
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onEvent(TasksEvent.OnAddTaskItemClicked)
                    },
                    icon = { Icon(Icons.Default.CheckBox, contentDescription = null) },
                    text = { Text("Add Task Item") }
                )
                // 3. Add Task List
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onEvent(TasksEvent.OnAddListClicked)
                    },
                    icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = null) },
                    text = { Text("New List") }
                )
                // 4. Add Category
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onEvent(TasksEvent.OnAddCategoryClicked)
                    },
                    icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                    text = { Text("New Category") }
                )
            }
        }
    ) { localPadding ->
        // ... Keep your Box and LazyColumn here, exactly as they were! ...
        Box(modifier = Modifier.fillMaxSize().padding(localPadding)) {
            // ...
        }
    }
}