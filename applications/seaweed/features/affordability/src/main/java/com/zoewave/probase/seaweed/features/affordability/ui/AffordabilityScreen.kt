package com.zoewave.probase.seaweed.features.affordability.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.tasks.SmartTaskDraft

@Composable
fun AffordabilityUiRoute(
    viewModel: AffordabilityViewModel = hiltViewModel(),
    initialPhotoUri: String? = null,
    onComplete: (SmartTaskDraft) -> Unit,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(initialPhotoUri) {
        if (initialPhotoUri != null) {
            viewModel.setCapturedUri(initialPhotoUri)
        }
    }

    AffordabilityScreen(
        uiState = uiState,
        onCommentChanged = viewModel::onUserCommentChanged,
        onAnalyzeClick = { uri, comment -> viewModel.analyze(uri, comment) },
        onConfirm = onComplete,
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AffordabilityScreen(
    uiState: AffordabilityUiState,
    onCommentChanged: (String) -> Unit,
    onAnalyzeClick: (String?, String) -> Unit,
    onConfirm: (SmartTaskDraft) -> Unit,
    onDismiss: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Seaweed Affordability Check") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                is AffordabilityUiState.Idle -> {
                    AffordabilityIdleState(
                        uri = uiState.capturedUri,
                        comment = uiState.userComment,
                        onCommentChanged = onCommentChanged,
                        onAnalyzeClick = { onAnalyzeClick(uiState.capturedUri, uiState.userComment) }
                    )
                }
                is AffordabilityUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text(uiState.message, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            uiState.logs.lastOrNull() ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is AffordabilityUiState.Success -> {
                    AffordabilityResultState(
                        draft = uiState.draft,
                        advice = uiState.affordabilityAdvice,
                        onConfirm = { onConfirm(uiState.draft) }
                    )
                }
                is AffordabilityUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(uiState.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onDismiss) { Text("Go Back") }
                    }
                }
            }
        }
    }
}

@Composable
private fun AffordabilityIdleState(
    uri: String?,
    comment: String,
    onCommentChanged: (String) -> Unit,
    onAnalyzeClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uri != null) {
            Card(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No image provided. Analyzing text only.")
            }
        }

        Spacer(Modifier.height(24.dp))
        
        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChanged,
            label = { Text("What are you buying? (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onAnalyzeClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Check Affordability with Gemini")
        }
    }
}

@Composable
private fun AffordabilityResultState(
    draft: SmartTaskDraft,
    advice: String,
    onConfirm: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Gemini Analysis", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Product: ${draft.taskName ?: "Unknown"}", style = MaterialTheme.typography.titleMedium)
                    Text("Estimated Price: ${draft.budget?.let { "$$it" } ?: "Not found"}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Financial Advice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(advice, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save as Pending Transaction")
            }
        }
    }
}
