package com.zoewave.probase.applications.journal.features.main.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditJournalScreen(
    viewModel: JournalViewModel,
    onBack: () -> Unit
) {
    val currentEntry by viewModel.currentEntry.collectAsState()
    
    var title by remember { mutableStateOf(currentEntry?.title ?: "") }
    var content by remember { mutableStateOf(currentEntry?.content ?: "") }
    var selectedImages by remember { mutableStateOf(currentEntry?.images ?: emptyList<Uri>()) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            selectedImages = selectedImages + uris
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (currentEntry == null) "New Entry" else "Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(onClick = {
                        viewModel.saveEntry(title, content, selectedImages)
                        onBack()
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Write your thoughts...") },
                modifier = Modifier.fillMaxWidth().weight(1f),
                minLines = 5
            )

            Text("Images", style = MaterialTheme.typography.titleSmall)
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    IconButton(
                        onClick = {
                            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Add Photo")
                    }
                }
                items(selectedImages) { uri ->
                    // Placeholder for image preview
                    Surface(
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {}
                }
            }
        }
    }
}
