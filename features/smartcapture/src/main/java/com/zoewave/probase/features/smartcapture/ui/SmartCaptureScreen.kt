package com.zoewave.probase.features.smartcapture.ui

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import com.zoewave.probase.features.smartcapture.ui.state.SmartCaptureUiState

@Composable
fun SmartCaptureUiRoute(
    viewModel: SmartCaptureViewModel = hiltViewModel(),
    initialPhotoUri: String? = null,
    onCaptureComplete: (SmartTaskDraft) -> Unit,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 🚀 NEW: Auto-trigger analysis if we already have a URI
    LaunchedEffect(initialPhotoUri) {
        if (initialPhotoUri != null) {
            viewModel.analyzePhoto(initialPhotoUri)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }
            viewModel.analyzePhoto(bitmap)
        }
    }

    SmartCaptureScreen(
        uiState = uiState,
        onUploadClick = {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onConfirmTask = onCaptureComplete,
        onReset = viewModel::reset,
        onDismiss = onDismiss
    )
}

@Composable
internal fun SmartCaptureScreen(
    uiState: SmartCaptureUiState,
    onUploadClick: () -> Unit,
    onConfirmTask: (SmartTaskDraft) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Smart Capture", style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is SmartCaptureUiState.Idle -> {
                    EmptyState(onUploadClick = onUploadClick)
                }

                is SmartCaptureUiState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI is parsing your image...", style = MaterialTheme.typography.bodyMedium)
                        
                        // 🚀 NEW: Diagnostics View
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (uiState.isUsingCloud) Icons.Default.Cloud else Icons.Default.SdStorage,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.padding(start = 8.dp))
                                    Text(
                                        text = if (uiState.isUsingCloud) "Attempting Cloud AI..." else "Local AI (Vision)",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    if (uiState.networkSpeed != null) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(text = uiState.networkSpeed, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                uiState.logs.takeLast(3).forEach { log ->
                                    Text(
                                        text = "> $log",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                is SmartCaptureUiState.Success -> {
                    Column {
                        if (uiState.warnings.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.padding(start = 8.dp))
                                    Text(uiState.warnings.first(), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        TaskReviewPane(
                            draft = uiState.draft,
                            engineUsed = uiState.engineUsed,
                            onConfirm = { onConfirmTask(uiState.draft) },
                            onRetake = onReset
                        )
                    }
                }

                is SmartCaptureUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = onReset, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onUploadClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Text(
            "Extract Tasks with AI",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            "Upload a photo of a note, whiteboard, or screen to automatically extract a structured task.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onUploadClick,
            modifier = Modifier.padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Text("Upload Photo", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun TaskReviewPane(
    draft: SmartTaskDraft,
    engineUsed: String,
    onConfirm: () -> Unit,
    onRetake: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text("Verify Extracted Task", style = MaterialTheme.typography.titleMedium)
                Text("AI: $engineUsed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TaskField("Task Name", draft.taskName ?: "Unknown")
                    TaskField("Category", draft.category ?: "General")
                    TaskField("Project", draft.projectName ?: "None")
                    TaskField("Duration", draft.duration ?: "Not set")
                    TaskField("Due Date", draft.dueDate ?: "Not set")
                    TaskField("Budget", draft.budget?.let { "$$it" } ?: "None")
                    
                    if (draft.subTasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sub-tasks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                        draft.subTasks.forEach { subTask ->
                            Text("• $subTask", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onRetake, modifier = Modifier.weight(1f)) {
                    Text("Retake")
                }
                Button(
                    onClick = onConfirm, 
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Text("Confirm", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun TaskField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
