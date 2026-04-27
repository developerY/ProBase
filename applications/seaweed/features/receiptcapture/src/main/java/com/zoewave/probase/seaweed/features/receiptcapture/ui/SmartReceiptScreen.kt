package com.zoewave.probase.seaweed.features.receiptcapture.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.zoewave.probase.seaweed.features.receiptcapture.domain.SmartReceiptDraft
import com.zoewave.probase.seaweed.features.receiptcapture.ui.components.ReceiptSaveForm
import com.zoewave.probase.seaweed.features.receiptcapture.ui.state.SmartReceiptUiState
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination

@Composable
fun SmartReceiptUiRoute(
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SmartReceiptViewModel = hiltViewModel(),
    initialPhotoUri: String? = null
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
        onEvent = { event ->
            when (event) {
                SmartReceiptUiEvent.UploadPhoto -> launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                is SmartReceiptUiEvent.CommentChanged -> viewModel.onUserCommentChanged(event.comment)
                is SmartReceiptUiEvent.AnalyzeReceipt -> viewModel.analyzeReceipt(event.uri, event.comment.ifBlank { null })
                is SmartReceiptUiEvent.ConfirmDraft -> {
                    viewModel.saveTransaction(event.draft)
                    onComplete()
                }
                SmartReceiptUiEvent.Reset -> viewModel.reset()
                SmartReceiptUiEvent.Dismiss -> onDismiss()
            }
        },
        navTo = {},
        modifier = modifier
    )
}

sealed interface SmartReceiptUiEvent {
    object UploadPhoto : SmartReceiptUiEvent
    data class CommentChanged(val comment: String) : SmartReceiptUiEvent
    data class AnalyzeReceipt(val uri: String, val comment: String) : SmartReceiptUiEvent
    data class ConfirmDraft(val draft: SmartReceiptDraft) : SmartReceiptUiEvent
    object Reset : SmartReceiptUiEvent
    object Dismiss : SmartReceiptUiEvent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartReceiptScreen(
    uiState: SmartReceiptUiState,
    onEvent: (SmartReceiptUiEvent) -> Unit,
    @Suppress("UnusedParameter") navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Receipt Capture", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { onEvent(SmartReceiptUiEvent.Dismiss) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
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
                        EmptyState(onUploadClick = { onEvent(SmartReceiptUiEvent.UploadPhoto) })
                    } else {
                        ContextInputState(
                            uri = uiState.capturedUri,
                            comment = uiState.userComment,
                            onCommentChanged = { onEvent(SmartReceiptUiEvent.CommentChanged(it)) },
                            onAnalyzeClick = { onEvent(SmartReceiptUiEvent.AnalyzeReceipt(uiState.capturedUri, uiState.userComment)) },
                            onRetake = { onEvent(SmartReceiptUiEvent.Reset) }
                        )
                    }
                }

                is SmartReceiptUiState.Loading -> {
                    LoadingState(uiState = uiState)
                }

                is SmartReceiptUiState.Success -> {
                    ReceiptSaveForm(
                        draft = uiState.draft,
                        onConfirm = { onEvent(SmartReceiptUiEvent.ConfirmDraft(it)) },
                        onRetake = { onEvent(SmartReceiptUiEvent.Reset) }
                    )
                }

                is SmartReceiptUiState.Error -> {
                    ErrorState(message = uiState.message, onRetry = { onEvent(SmartReceiptUiEvent.Reset) })
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

@Composable
private fun LoadingState(uiState: SmartReceiptUiState.Loading) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
            Text("Try Again")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SmartReceiptScreenIdlePreview() {
    MaterialTheme {
        SmartReceiptScreen(
            uiState = SmartReceiptUiState.Idle(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SmartReceiptScreenLoadingPreview() {
    MaterialTheme {
        SmartReceiptScreen(
            uiState = SmartReceiptUiState.Loading(isUsingCloud = true, logs = listOf("Connecting to cloud...")),
            onEvent = {},
            navTo = {}
        )
    }
}
