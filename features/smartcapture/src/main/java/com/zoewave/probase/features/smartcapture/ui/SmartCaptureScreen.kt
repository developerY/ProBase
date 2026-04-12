package com.zoewave.probase.features.smartcapture.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.zoewave.probase.features.camera.ui.CameraUIRoute
import com.zoewave.probase.features.smartcapture.domain.SmartTask
import com.zoewave.probase.features.smartcapture.ui.state.SmartCaptureUiState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SmartCaptureUiRoute(
    viewModel: SmartCaptureViewModel = hiltViewModel(),
    onTaskConfirmed: (SmartTask) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    if (uiState.showCamera) {
        if (cameraPermissionState.status.isGranted) {
            CameraUIRoute(
                navTo = { result ->
                    if (result.startsWith("result_ok:")) {
                        viewModel.onImageCaptured(result.removePrefix("result_ok:"))
                    } else {
                        viewModel.setCameraVisible(false)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
            // Temporarily hide camera if permission is being requested or denied
            viewModel.setCameraVisible(false)
        }
    } else {
        SmartCaptureScreen(
            uiState = uiState,
            onCaptureClick = { viewModel.setCameraVisible(true) },
            onConfirmTask = onTaskConfirmed,
            onReset = viewModel::reset,
            onDismiss = onDismiss
        )
    }
}

@Composable
internal fun SmartCaptureScreen(
    uiState: SmartCaptureUiState,
    onCaptureClick: () -> Unit,
    onConfirmTask: (SmartTask) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isProcessing) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text("AI is analyzing text...", modifier = Modifier.padding(top = 16.dp))
                }
            } else if (uiState.capturedTask != null) {
                TaskReviewPane(
                    task = uiState.capturedTask,
                    onConfirm = { onConfirmTask(uiState.capturedTask) },
                    onRetake = onReset
                )
            } else {
                EmptyState(onCaptureClick = onCaptureClick)
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onCaptureClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            "Extract Tasks with AI",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            "Point your camera at a note, screen, or whiteboard to automatically extract a structured task.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Button(
            onClick = onCaptureClick,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Text("Open Camera", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun TaskReviewPane(
    task: SmartTask,
    onConfirm: () -> Unit,
    onRetake: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Verify Extracted Task", style = MaterialTheme.typography.titleMedium)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TaskField("Title", task.title)
                    TaskField("Description", task.description ?: "None")
                    TaskField("Due Date", task.dueDate ?: "Not set")
                    TaskField("Budget", task.estimatedBudget?.let { "$$it" } ?: "None")
                    TaskField("Category", task.suggestedCategory ?: "General")
                }
            }
        }

        item {
            HorizontalDivider()
            Text("Raw OCR Text", style = MaterialTheme.typography.labelSmall)
            Text(task.rawText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onRetake, modifier = Modifier.weight(1f)) {
                    Text("Retake")
                }
                Button(onClick = onConfirm, modifier = Modifier.weight(1f)) {
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
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
