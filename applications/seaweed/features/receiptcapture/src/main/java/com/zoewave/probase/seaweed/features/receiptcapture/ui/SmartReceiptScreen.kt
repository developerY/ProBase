package com.zoewave.probase.seaweed.features.receiptcapture.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.zoewave.probase.seaweed.features.receiptcapture.domain.SmartReceiptDraft
import com.zoewave.probase.seaweed.features.receiptcapture.ui.components.ReceiptSaveForm
import com.zoewave.probase.seaweed.features.receiptcapture.ui.state.SmartReceiptUiState

@Composable
fun SmartReceiptUiRoute(
    viewModel: SmartReceiptViewModel = hiltViewModel(),
    initialPhotoUri: String? = null,
    onComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(initialPhotoUri) {
        if (initialPhotoUri != null && uiState is SmartReceiptUiState.Idle && (uiState as SmartReceiptUiState.Idle).capturedUri == null) {
            viewModel.setCapturedUri(initialPhotoUri)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.setCapturedUri(it.toString()) }
    }

    SmartReceiptScreen(
        uiState = uiState,
        onUploadClick = {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onCommentChanged = viewModel::onUserCommentChanged,
        onAnalyzeClick = { uri, comment ->
            viewModel.analyzeReceipt(uri, comment.ifBlank { null })
        },
        onConfirmDraft = {
            viewModel.saveTransaction(it)
            onComplete()
        },
        onReset = viewModel::reset,
        onDismiss = onDismiss
    )
}

@Composable
internal fun SmartReceiptScreen(
    uiState: SmartReceiptUiState,
    onUploadClick: () -> Unit,
    onCommentChanged: (String) -> Unit,
    onAnalyzeClick: (String, String) -> Unit,
    onConfirmDraft: (SmartReceiptDraft) -> Unit,
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
                Text("Receipt Capture", style = MaterialTheme.typography.headlineSmall)
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
                is SmartReceiptUiState.Idle -> {
                    if (uiState.capturedUri == null) {
                        EmptyState(onUploadClick = onUploadClick)
                    } else {
                        ContextInputState(
                            uri = uiState.capturedUri,
                            comment = uiState.userComment,
                            onCommentChanged = onCommentChanged,
                            onAnalyzeClick = { onAnalyzeClick(uiState.capturedUri, uiState.userComment) },
                            onRetake = onReset
                        )
                    }
                }

                is SmartReceiptUiState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI is parsing your receipt...", style = MaterialTheme.typography.bodyMedium)
                        
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
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (uiState.isUsingCloud) "Cloud AI (Gemini)" else "Local AI (Vision)",
                                        style = MaterialTheme.typography.labelSmall
                                    )
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

                is SmartReceiptUiState.Success -> {
                    ReceiptSaveForm(
                        draft = uiState.draft,
                        onConfirm = onConfirmDraft,
                        onRetake = onReset
                    )
                }

                is SmartReceiptUiState.Error -> {
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
private fun ContextInputState(
    uri: String,
    comment: String,
    onCommentChanged: (String) -> Unit,
    onAnalyzeClick: () -> Unit,
    onRetake: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            AsyncImage(
                model = uri,
                contentDescription = "Captured receipt",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Add context (Optional)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChanged,
                placeholder = { Text("e.g. Lunch with clients...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = MaterialTheme.shapes.medium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onRetake,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Text("Retake")
            }
            Button(
                onClick = onAnalyzeClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyze")
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
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            "Scan Receipt with AI",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            "Take a photo of a receipt to automatically extract merchant, amount, and date.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onUploadClick,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Text("Upload Photo", modifier = Modifier.padding(start = 8.dp))
        }
    }
}
