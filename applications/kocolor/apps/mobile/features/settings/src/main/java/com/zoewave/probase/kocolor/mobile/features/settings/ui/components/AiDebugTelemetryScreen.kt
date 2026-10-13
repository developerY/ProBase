package com.zoewave.probase.kocolor.mobile.features.settings.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.kocolor.data.telemetry.StyleAuditTrail
import com.zoewave.probase.kocolor.mobile.features.settings.ui.AiDebugUiState
import com.zoewave.probase.kocolor.mobile.features.settings.ui.AiDebugViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDebugTelemetryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AiDebugViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI Telemetry & Diagnostics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Active: ${uiState.activeProviderName}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear Logs")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Active Provider Status Overview
            item {
                Text(
                    "AI ENGINE ROUTING TIER STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // BYOK Card
                    ProviderStatusCard(
                        title = "1. BYOK AI",
                        icon = Icons.Default.Key,
                        isActive = uiState.isByokEnabled && uiState.isByokKeySet,
                        statusText = if (uiState.isByokEnabled && uiState.isByokKeySet) "Key Active" else if (uiState.isByokKeySet) "Disabled" else "No Key",
                        modifier = Modifier.weight(1f)
                    )

                    // Local Nano Card
                    ProviderStatusCard(
                        title = "2. Local Nano",
                        icon = Icons.Default.Memory,
                        isActive = uiState.isLocalAiEnabled,
                        statusText = if (uiState.isLocalAiEnabled) "On-Device" else "Unavailable",
                        modifier = Modifier.weight(1f)
                    )

                    // Firebase Cloud Card
                    ProviderStatusCard(
                        title = "3. Firebase AI",
                        icon = Icons.Default.Cloud,
                        isActive = uiState.isFirebaseEnabled,
                        statusText = if (uiState.isFirebaseEnabled) uiState.currentModelName else "Disabled",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section 2: Live Diagnostic Action Test
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Live Diagnostic Test", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Test AI provider routing and inspect raw response", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }

                            Button(
                                onClick = { viewModel.runTestInference() },
                                enabled = !uiState.isRunningTest,
                                shape = CircleShape
                            ) {
                                if (uiState.isRunningTest) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Run Test")
                                }
                            }
                        }

                        if (uiState.testRunOutput != null) {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E1E1E)
                            ) {
                                Text(
                                    text = uiState.testRunOutput!!,
                                    modifier = Modifier.padding(12.dp),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFF00FF66)
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Audit Trail Execution Console
            item {
                Text(
                    "RECENT EXECUTION AUDIT LOGS (${uiState.recentAuditLogs.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (uiState.recentAuditLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Terminal, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No recent AI audit trails logged.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                                Text("Trigger an outfit generation or run the diagnostic test above.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            } else {
                items(uiState.recentAuditLogs) { logText ->
                    AuditLogCard(logText = logText)
                }
            }
        }
    }
}

@Composable
private fun ProviderStatusCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    statusText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) Color(0xFFBBF7D0) else Color(0xFFFECACA)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) Color(0xFF166534) else Color(0xFF991B1B),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(
                statusText,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) Color(0xFF15803D) else Color(0xFFB91C1C)
            )
        }
    }
}

@Composable
private fun AuditLogCard(logText: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    val headerTitle = logText.lines().firstOrNull { it.contains("[") && it.contains("]") } ?: "KOCOLOR AI AUDIT TRAIL"
                    Text(
                        text = headerTitle.trim(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            if (!expanded) {
                Spacer(Modifier.height(4.dp))
                val snippet = logText.lines().filter { it.isNotBlank() }.take(3).joinToString("\n")
                Text(
                    text = snippet,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    maxLines = 2
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = Color.DarkGray)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = logText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF00FF66),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
