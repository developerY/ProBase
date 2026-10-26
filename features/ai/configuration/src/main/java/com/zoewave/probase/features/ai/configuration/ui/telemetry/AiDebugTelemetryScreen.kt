package com.zoewave.probase.features.ai.configuration.ui.telemetry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
                        Text("Active: ${uiState.activeProviderName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Engine Routing Status Section
            item {
                SectionHeader("AI ENGINE ROUTING TIER STATUS")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusGridTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Memory,
                        title = "1. Local Nano",
                        subtitle = if (uiState.isLocalAiEnabled) "Active" else "Unavailable",
                        isActive = uiState.isLocalAiEnabled
                    )
                    StatusGridTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Key,
                        title = "2. BYOK AI",
                        subtitle = when {
                            !uiState.isByokEnabled -> "Disabled"
                            uiState.isByokKeySet -> "Key Set"
                            else -> "No Key"
                        },
                        isActive = uiState.isByokEnabled && uiState.isByokKeySet
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusGridTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Cloud,
                        title = "3. Firebase AI",
                        subtitle = if (uiState.isFirebaseEnabled) "Active" else "Disabled",
                        isActive = uiState.isFirebaseEnabled
                    )
                    StatusGridTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AutoAwesome,
                        title = "4. Gemma 4",
                        subtitle = uiState.currentModelName,
                        isActive = uiState.isGemmaEnabled
                    )
                }
            }

            // 2. Select Target LLM
            item {
                SectionHeader("SELECT TARGET LLM TO TEST")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TestTargetProvider.entries.toTypedArray()) { target ->
                        FilterChip(
                            selected = uiState.selectedTarget == target,
                            onClick = { viewModel.selectTarget(target) },
                            label = { Text(target.label) }
                        )
                    }
                }
            }

            // 3. Choose Model Dropdown
            item {
                SectionHeader("CHOOSE MODEL FOR CLOUD AI")
                ModelDropdownMenu(
                    currentModel = uiState.currentModelName,
                    availableModels = uiState.availableByokModels,
                    onModelSelected = { viewModel.selectCloudModel(it) }
                )
            }

            // 4. Diagnostic Card & Live Pipeline Test
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Live Pipeline Diagnostic", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Testing Target: ${uiState.selectedTarget.label}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Button(
                                onClick = { viewModel.runLiveDiagnostic() },
                                enabled = !uiState.isRunningTest,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                if (uiState.isRunningTest) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text("Run Test")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.runFullProviderMatrixTest() },
                                enabled = !uiState.isRunningTest,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Matrix Test")
                            }
                        }

                        // Diagnostic Steps
                        uiState.diagnosticSteps.forEach { step ->
                            DiagnosticStepItem(step = step)
                        }

                        // Payload Output
                        uiState.testRunOutput?.let { output ->
                            HorizontalDivider()
                            Text("Response Payload Output", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = output,
                                    modifier = Modifier.padding(12.dp),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // App-Specific Audit Trail
                        if (uiState.recentAuditLogs.isNotEmpty()) {
                            HorizontalDivider()
                            Text("RECENT EXECUTION AUDIT LOGS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF1E1E1E), // Dark terminal background
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.DarkGray)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    uiState.recentAuditLogs.forEach { log ->
                                        Text(
                                            text = log,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = Color(0xFF4CAF50), // Hacker green text
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun StatusGridTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    isActive: Boolean
) {
    val bgColor = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val contentColor = if (isActive) Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(contentColor))
            }
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = contentColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelDropdownMenu(
    currentModel: String,
    availableModels: List<String>,
    onModelSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = currentModel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Active Cloud Model") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableModels.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model) },
                    onClick = {
                        onModelSelected(model)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DiagnosticStepItem(step: DiagnosticStep) {
    val icon: ImageVector? = when (step.state) {
        DiagnosticStepState.PENDING -> Icons.Default.Power
        DiagnosticStepState.RUNNING -> null
        DiagnosticStepState.SUCCESS -> Icons.Default.CheckCircle
        DiagnosticStepState.FAILED -> Icons.Default.Error
    }
    val color: Color = when (step.state) {
        DiagnosticStepState.PENDING -> Color.Gray
        DiagnosticStepState.RUNNING -> MaterialTheme.colorScheme.primary
        DiagnosticStepState.SUCCESS -> Color(0xFF2E7D32)
        DiagnosticStepState.FAILED -> Color(0xFFC62828)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (step.state == DiagnosticStepState.RUNNING) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(step.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(step.detailMessage ?: step.description, style = MaterialTheme.typography.bodySmall, color = if (step.state == DiagnosticStepState.FAILED) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
