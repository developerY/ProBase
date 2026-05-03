package com.zoewave.probase.features.ai.capture.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import com.zoewave.probase.core.ui.theme.AshBikeTheme
import com.zoewave.probase.features.ai.capture.R
import com.zoewave.probase.features.ai.capture.ui.state.SmartCaptureUiState

@Composable
fun SmartCaptureUiRoute(
    viewModel: SmartCaptureViewModel = hiltViewModel(),
    initialPhotoUri: String? = null,
    onCaptureComplete: (SmartTaskDraft) -> Unit,
    onRetakeRequest: () -> Unit,
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    // 🚀 Reset the ViewModel state whenever a new initialPhotoUri arrives from navigation.
    // This ensures we clear any "cached" results from previous analyses.
    LaunchedEffect(initialPhotoUri) {
        if (initialPhotoUri != null) {
            viewModel.setCapturedUri(initialPhotoUri)
        } else {
            viewModel.reset()
        }
    }

    SmartCaptureScreen(
        uiState = uiState,
        onCommentChanged = viewModel::onUserCommentChanged,
        onAnalyzeClick = { uri, comment ->
            viewModel.analyzePhoto(uri, comment.ifBlank { null })
        },
        onConfirmTask = onCaptureComplete,
        onRetake = onRetakeRequest,
        onReset = viewModel::reset,
        onDismiss = onDismiss
    )
}

@Composable
internal fun SmartCaptureScreen(
    uiState: SmartCaptureUiState,
    onCommentChanged: (String) -> Unit,
    onAnalyzeClick: (String?, String) -> Unit,
    onConfirmTask: (SmartTaskDraft) -> Unit,
    onRetake: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showDiagnostics by remember { mutableStateOf(value = false) }

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
                Text(stringResource(R.string.features_ai_capture_smart_capture_title), style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.features_ai_capture_smart_capture_close_content_desc))
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
                    if (uiState.capturedUri == null) {
                        EmptyState(
                            comment = uiState.userComment,
                            onCommentChanged = onCommentChanged,
                            onAnalyzeClick = { onAnalyzeClick(null, uiState.userComment) }
                        )
                    } else {
                        ContextInputState(
                            uri = uiState.capturedUri,
                            comment = uiState.userComment,
                            onCommentChanged = onCommentChanged,
                            onAnalyzeClick = { onAnalyzeClick(uiState.capturedUri, uiState.userComment) },
                            onRetake = onRetake
                        )
                    }
                }

                is SmartCaptureUiState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.features_ai_capture_smart_capture_loading_description), style = MaterialTheme.typography.bodyMedium)
                        
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
                                        text = if (uiState.isUsingCloud) stringResource(R.string.features_ai_capture_smart_capture_cloud_ai_label) else stringResource(R.string.features_ai_capture_smart_capture_local_ai_label),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    if (uiState.networkSpeed != null) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(text = uiState.networkSpeed, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                uiState.logs.takeLast(5).forEach { log ->
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.padding(start = 8.dp))
                                    Text(
                                        uiState.warnings.first(),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { showDiagnostics = true }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.HelpOutline,
                                            contentDescription = stringResource(R.string.features_ai_capture_smart_capture_ai_warning_content_desc),
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                        TaskReviewPane(
                            draft = uiState.draft,
                            engineUsed = uiState.engineUsed,
                            onConfirm = { onConfirmTask(uiState.draft) },
                            onRetake = onRetake
                        )

                        if (showDiagnostics) {
                            DiagnosticsDialog(
                                logs = uiState.diagnostics,
                                onDismiss = { showDiagnostics = false }
                            )
                        }
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
                            Text(stringResource(R.string.features_ai_capture_smart_capture_try_again))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticsDialog(
    logs: List<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.features_ai_capture_smart_capture_ai_diagnostics_title)) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs.size) { index ->
                    Text(
                        text = "> ${logs[index]}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.features_ai_capture_smart_capture_ai_diagnostics_close))
            }
        }
    )
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
                contentDescription = stringResource(R.string.features_ai_capture_smart_capture_cd_captured_image),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.features_ai_capture_smart_capture_context_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChanged,
                placeholder = { Text(stringResource(R.string.features_ai_capture_smart_capture_context_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = MaterialTheme.shapes.medium
            )
            Text(
                stringResource(R.string.features_ai_capture_smart_capture_context_description),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                Text(stringResource(R.string.features_ai_capture_smart_capture_retake_button))
            }
            Button(
                onClick = onAnalyzeClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.features_ai_capture_smart_capture_analyze_button))
            }
        }
    }
}

@Composable
private fun EmptyState(
    comment: String,
    onCommentChanged: (String) -> Unit,
    onAnalyzeClick: () -> Unit
) {
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
            stringResource(R.string.features_ai_capture_smart_capture_empty_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            stringResource(R.string.features_ai_capture_smart_capture_empty_description),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChanged,
            placeholder = { Text(stringResource(R.string.features_ai_capture_smart_capture_empty_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = MaterialTheme.shapes.medium
        )
        
        if (comment.isNotBlank()) {
            Button(
                onClick = onAnalyzeClick,
                modifier = Modifier.padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Text(stringResource(R.string.features_ai_capture_smart_capture_analyze_text_button), modifier = Modifier.padding(start = 8.dp))
            }
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
                Text(stringResource(R.string.features_ai_capture_smart_capture_review_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.features_ai_capture_smart_capture_review_ai_label, engineUsed), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    TaskField(stringResource(R.string.features_ai_capture_smart_capture_field_task_name), draft.taskName ?: stringResource(R.string.features_ai_capture_smart_capture_value_unknown))
                    TaskField(stringResource(R.string.features_ai_capture_smart_capture_field_category), draft.category ?: stringResource(R.string.features_ai_capture_smart_capture_value_general))
                    TaskField(stringResource(R.string.features_ai_capture_smart_capture_field_project), draft.projectName ?: stringResource(R.string.features_ai_capture_smart_capture_value_none))
                    TaskField(stringResource(R.string.features_ai_capture_smart_capture_field_duration), draft.duration ?: stringResource(R.string.features_ai_capture_smart_capture_value_not_set))
                    TaskField(stringResource(R.string.features_ai_capture_smart_capture_field_due_date), draft.dueDate ?: stringResource(R.string.features_ai_capture_smart_capture_value_not_set))
                    TaskField(stringResource(R.string.features_ai_capture_smart_capture_field_budget), draft.budget?.let { stringResource(R.string.features_ai_capture_smart_capture_value_budget_format, it) } ?: stringResource(R.string.features_ai_capture_smart_capture_value_none))
                    
                    if (draft.subTasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.features_ai_capture_smart_capture_review_subtasks), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                        draft.subTasks.forEach { subTask ->
                            Text(
                                text = stringResource(R.string.features_ai_capture_smart_capture_bullet_format, subTask), 
                                style = MaterialTheme.typography.bodyMedium
                            )
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
                    Text(stringResource(R.string.features_ai_capture_smart_capture_retake_button))
                }
                Button(
                    onClick = onConfirm, 
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Text(stringResource(R.string.features_ai_capture_smart_capture_review_confirm), modifier = Modifier.padding(start = 8.dp))
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

@Preview(showBackground = true, name = "Empty State")
@Composable
fun SmartCaptureScreenEmptyPreview() {
    AshBikeTheme {
        SmartCaptureScreen(
            uiState = SmartCaptureUiState.Idle(
                capturedUri = null,
                userComment = ""
            ),
            onCommentChanged = {},
            onAnalyzeClick = { _, _ -> },
            onConfirmTask = {},
            onRetake = {},
            onReset = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Context Input State")
@Composable
fun SmartCaptureScreenContextInputPreview() {
    AshBikeTheme {
        SmartCaptureScreen(
            uiState = SmartCaptureUiState.Idle(
                capturedUri = "https://example.com/photo.jpg",
                userComment = "Fixing the kitchen sink"
            ),
            onCommentChanged = {},
            onAnalyzeClick = { _, _ -> },
            onConfirmTask = {},
            onRetake = {},
            onReset = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun SmartCaptureScreenLoadingPreview() {
    AshBikeTheme {
        SmartCaptureScreen(
            uiState = SmartCaptureUiState.Loading(
                logs = listOf("Analyzing image...", "OCR successful"),
                isUsingCloud = true,
                networkSpeed = "1.2 MB/s"
            ),
            onCommentChanged = {},
            onAnalyzeClick = { _, _ -> },
            onConfirmTask = {},
            onRetake = {},
            onReset = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Success State")
@Composable
fun SmartCaptureScreenSuccessPreview() {
    AshBikeTheme {
        SmartCaptureScreen(
            uiState = SmartCaptureUiState.Success(
                draft = SmartTaskDraft(
                    taskName = "Replace Sink Faucet",
                    category = "Plumbing",
                    projectName = "Kitchen Renovation",
                    duration = "2 hours",
                    dueDate = "2023-12-31",
                    budget = 150.0,
                    subTasks = listOf("Buy new faucet", "Remove old faucet", "Install new faucet")
                ),
                engineUsed = "Cloud AI (Gemini)"
            ),
            onCommentChanged = {},
            onAnalyzeClick = { _, _ -> },
            onConfirmTask = {},
            onRetake = {},
            onReset = {},
            onDismiss = {}
        )
    }
}
