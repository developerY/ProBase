package com.zoewave.probase.seaweed.mobile.transaction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.seaweed.mobile.transaction.R
import com.zoewave.probase.core.ui.R as CoreUiR

@Composable
fun AiDebugScreen(
    rawResponse: String,
    logs: List<String>,
    engineUsed: String,
    whatIsThis: String? = null,
    onBack: () -> Unit
) {
    AiDebugScreen(
        uiState = AiDebugUiState(rawResponse, logs, engineUsed, whatIsThis),
        onEvent = { onBack() }
    )
}

data class AiDebugUiState(
    val rawResponse: String,
    val logs: List<String>,
    val engineUsed: String,
    val whatIsThis: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDebugScreen(
    uiState: AiDebugUiState,
    onEvent: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_debug_ai_title)) },
                navigationIcon = {
                    IconButton(onClick = onEvent) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(CoreUiR.string.cd_navigate_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                EngineUsedCard(engineUsed = uiState.engineUsed)
            }

            uiState.whatIsThis?.let {
                item {
                    AiDescriptionCard(description = it)
                }
            }

            item {
                Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_debug_process_logs), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }

            items(uiState.logs) { log ->
                LogItem(log = log)
            }

            item {
                RawJsonResponseHeader()
            }

            item {
                RawJsonResponseContent(rawResponse = uiState.rawResponse)
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun EngineUsedCard(engineUsed: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.BugReport, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_debug_engine_used), style = MaterialTheme.typography.labelSmall)
                Text(engineUsed, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AiDescriptionCard(description: String) {
    Column {
        Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_debug_ai_description), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
        ) {
            Text(
                text = description,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun LogItem(log: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            text = "> $log",
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun RawJsonResponseHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_debug_raw_json), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun RawJsonResponseContent(rawResponse: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = if (rawResponse.isBlank()) stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_debug_no_data) else rawResponse,
            color = Color(0xFF00FF00), // Matrix Green
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AiDebugScreenPreview() {
    MaterialTheme {
        AiDebugScreen(
            uiState = AiDebugUiState(
                rawResponse = "{\"merchant\": \"Starbucks\", \"total\": 5.50}",
                logs = listOf("Starting AI analysis", "Image captured", "Response received"),
                engineUsed = "Gemini 1.5 Flash",
                whatIsThis = "A coffee receipt from Starbucks"
            ),
            onEvent = {}
        )
    }
}
