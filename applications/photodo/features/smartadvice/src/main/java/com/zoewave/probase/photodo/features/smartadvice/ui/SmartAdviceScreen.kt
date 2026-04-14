package com.zoewave.probase.photodo.features.smartadvice.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zoewave.probase.photodo.features.smartadvice.domain.ProjectAdvice

@Composable
fun SmartAdviceUiRoute(
    projectId: Long,
    viewModel: SmartAdviceViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.fetchAdvice(projectId)
    }

    SmartAdviceScreen(
        uiState = uiState,
        onRetry = { viewModel.fetchAdvice(projectId) },
        onDismiss = onDismiss
    )
}

@Composable
internal fun SmartAdviceScreen(
    uiState: SmartAdviceUiState,
    onRetry: () -> Unit,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text("AI Project Advisor", style = MaterialTheme.typography.titleLarge)
                }
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
                is SmartAdviceUiState.Idle,
                is SmartAdviceUiState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Gemini is analyzing your project...", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                is SmartAdviceUiState.Success -> {
                    AdviceContent(advice = uiState.advice)
                }

                is SmartAdviceUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Text(
                            text = uiState.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Button(onClick = onRetry, modifier = Modifier.padding(top = 24.dp)) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdviceContent(advice: ProjectAdvice) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Summary", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                    Text(advice.summary, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        if (advice.tips.isNotEmpty()) {
            item { AdviceSection(title = "Expert Tips", items = advice.tips) }
        }

        if (advice.potentialRisks.isNotEmpty()) {
            item { AdviceSection(title = "Potential Risks", items = advice.potentialRisks, color = MaterialTheme.colorScheme.error) }
        }

        if (advice.budgetAdvice != null || advice.timeAdvice != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Resource Optimization", style = MaterialTheme.typography.titleMedium)
                    advice.budgetAdvice?.let { OptimizationCard(title = "Budget", text = it) }
                    advice.timeAdvice?.let { OptimizationCard(title = "Time", text = it) }
                }
            }
        }

        if (advice.suggestedChecklistItems.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Suggested Tasks", style = MaterialTheme.typography.titleMedium)
                    advice.suggestedChecklistItems.forEach { item ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "• $item", 
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdviceSection(title: String, items: List<String>, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.tertiary) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = color)
        items.forEach { item ->
            Text(text = "• $item", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun OptimizationCard(title: String, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(text, style = MaterialTheme.typography.bodySmall)
        }
    }
}
