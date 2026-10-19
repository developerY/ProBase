package com.zoewave.probase.features.ai.configuration.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zoewave.probase.features.ai.configuration.R
import com.zoewave.probase.core.ui.R as CoreUiR


@Composable
private fun PipelineStageIndicator(
    isActive: Boolean,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isActive) Color(0xFF4CAF50) else Color(0xFFE53935), // Green vs Red
                    shape = CircleShape
                )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) Color.Black else Color.Gray
        )
    }
}

@Composable
fun AiConfigurationCard(
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    onDebugClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.features_ai_configuration_title),
    description: String = stringResource(R.string.features_ai_configuration_desc),
    viewModel: AiConfigurationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AiConfigurationCardContent(
        expanded = expanded,
        onExpandToggle = onExpandToggle,
        onDebugClick = onDebugClick,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        title = title,
        description = description,
        modifier = modifier
    )
}


/**
 * Configure AI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiConfigurationCardContent(
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    uiState: AiConfigurationUiState,
    onEvent: (AiConfigurationEvent) -> Unit,
    title: String,
    description: String,
    onDebugClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var editingKey by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var isModelDropdownExpanded by remember { mutableStateOf(false) }
    var isPrimaryEngineDropdownExpanded by remember { mutableStateOf(false) }

    val selectedPrimaryAi = when {
        uiState.useByokKey -> "Bring Your Own Key (BYOK)"
        uiState.useFirebaseVertexAi -> "Firebase AI Logic (Cloud)"
        else -> "Gemma 4 Cloud (Free Tier) - Default"
    }

    val primaryAiOptions = listOf(
        "Gemma 4 Cloud (Free Tier) - Default",
        "Firebase AI Logic (Cloud)",
        "Bring Your Own Key (BYOK)"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFFE8E0FD)) // Soft lavender
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable { onExpandToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37) // Gold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                    val isStage1Active = uiState.isApiKeySet && uiState.useByokKey
                    val isStage2Active = uiState.isLocalAiAvailable
                    val isStage3Active = uiState.useFirebaseVertexAi
                    val isStage4Active = true // Authenticated automatically via Firebase project key

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // Stage 1: BYOK
                        PipelineStageIndicator(isActive = isStage1Active, label = "BYOK")

                        Text("→", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                        // Stage 2: Local AI
                        PipelineStageIndicator(isActive = isStage2Active, label = "Local AI")

                        Text("→", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                        // Stage 3: Firebase
                        PipelineStageIndicator(isActive = isStage3Active, label = "Firebase")

                        Text("→", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                        // Stage 4: Gemma
                        PipelineStageIndicator(isActive = isStage4Active, label = "Gemma")
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = "Primary AI Engine",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    ExposedDropdownMenuBox(
                        expanded = isPrimaryEngineDropdownExpanded,
                        onExpandedChange = { isPrimaryEngineDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedPrimaryAi,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPrimaryEngineDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = isPrimaryEngineDropdownExpanded,
                            onDismissRequest = { isPrimaryEngineDropdownExpanded = false }
                        ) {
                            primaryAiOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option,
                                            fontWeight = if (option == selectedPrimaryAi) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        when (option) {
                                            "Gemma 4 Cloud (Free Tier) - Default" -> {
                                                onEvent(AiConfigurationEvent.OnUseByokKeyToggled(false))
                                                onEvent(AiConfigurationEvent.OnUseFirebaseVertexAiToggled(false))
                                            }
                                            "Firebase AI Logic (Cloud)" -> {
                                                onEvent(AiConfigurationEvent.OnUseFirebaseVertexAiToggled(true))
                                            }
                                            "Bring Your Own Key (BYOK)" -> {
                                                onEvent(AiConfigurationEvent.OnUseByokKeyToggled(true))
                                            }
                                        }
                                        isPrimaryEngineDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = "Select your preferred cloud AI provider. (Fallback routing rules remain unchanged).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    AnimatedVisibility(visible = uiState.useByokKey) {
                        Column {
                            if (uiState.isApiKeySet && editingKey.isBlank()) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = stringResource(R.string.features_ai_configuration_key_configured),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        TextButton(onClick = { onEvent(AiConfigurationEvent.OnGeminiApiKeyChanged(null)) }) {
                                            Text(stringResource(CoreUiR.string.action_delete), color = MaterialTheme.colorScheme.error)
                                        }
                                    }

                                    val keyColor = when {
                                        uiState.keyTestResult == null -> MaterialTheme.colorScheme.primary
                                        uiState.keyTestResult.startsWith("Key Valid") || uiState.keyTestResult.startsWith("Valid") -> Color(0xFF4CAF50)
                                        else -> MaterialTheme.colorScheme.error
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Button(
                                            onClick = { onEvent(AiConfigurationEvent.OnTestApiKeyClicked) },
                                            enabled = !uiState.isTestingKey,
                                            modifier = Modifier.padding(bottom = 8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (uiState.keyTestResult == null) MaterialTheme.colorScheme.primary else keyColor.copy(alpha = 0.1f),
                                                contentColor = if (uiState.keyTestResult == null) MaterialTheme.colorScheme.onPrimary else keyColor
                                            )
                                        ) {
                                            if (uiState.isTestingKey) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = keyColor)
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Text(stringResource(R.string.features_ai_configuration_test_connection))
                                        }
                                        if (uiState.keyTestResult != null) {
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = uiState.keyTestResult,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = keyColor
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = editingKey,
                                onValueChange = { editingKey = it },
                                label = {
                                    Text(
                                        if (uiState.isApiKeySet)
                                            stringResource(R.string.features_ai_configuration_update_key)
                                        else
                                            stringResource(R.string.features_ai_configuration_enter_key)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showKey = !showKey }) {
                                        Icon(
                                            imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true
                            )

                            if (editingKey.isNotBlank()) {
                                Button(
                                    onClick = {
                                        onEvent(AiConfigurationEvent.OnGeminiApiKeyChanged(editingKey))
                                        editingKey = ""
                                    },
                                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                                ) {
                                    Text(stringResource(R.string.features_ai_configuration_save_key))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(R.string.features_ai_configuration_preferred_model),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            ExposedDropdownMenuBox(
                                expanded = isModelDropdownExpanded && uiState.availableModels.isNotEmpty(),
                                onExpandedChange = { if (uiState.availableModels.isNotEmpty()) isModelDropdownExpanded = it },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                OutlinedTextField(
                                    value = if (uiState.availableModels.isEmpty())
                                        stringResource(R.string.features_ai_configuration_test_connection_hint)
                                    else
                                        uiState.currentAiModel,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(stringResource(R.string.features_ai_configuration_model_placeholder)) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                                    trailingIcon = {
                                        if (uiState.availableModels.isNotEmpty()) {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isModelDropdownExpanded)
                                        }
                                    },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                )

                                ExposedDropdownMenu(
                                    expanded = isModelDropdownExpanded,
                                    onDismissRequest = { isModelDropdownExpanded = false }
                                ) {
                                    uiState.availableModels.forEach { model ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = model,
                                                    fontWeight = if (model == uiState.currentAiModel) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                onEvent(AiConfigurationEvent.OnAiModelSelected(model))
                                                isModelDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (uiState.availableModels.isEmpty())
                                    stringResource(R.string.features_ai_configuration_unlock_models_hint)
                                else
                                    stringResource(R.string.features_ai_configuration_choose_model_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            if (uiState.availableModels.isNotEmpty()) {
                                val statusColor = when {
                                    uiState.modelTestResult == null -> MaterialTheme.colorScheme.outline
                                    uiState.modelTestResult.contains("Error") ||
                                    uiState.modelTestResult.contains("Connection") ||
                                    uiState.modelTestResult.contains("not enabled") ||
                                    uiState.modelTestResult.contains("failed") ||
                                    uiState.modelTestResult.contains("404") ||
                                    uiState.modelTestResult.contains("403") -> MaterialTheme.colorScheme.error
                                    else -> Color(0xFF4CAF50) // Material Green
                                }

                                Column(modifier = Modifier.padding(top = 16.dp)) {
                                    Text(
                                        text = stringResource(R.string.features_ai_configuration_verify_model_title),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = stringResource(R.string.features_ai_configuration_verify_model_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Button(
                                            onClick = { onEvent(AiConfigurationEvent.OnTestModelClicked) },
                                            enabled = !uiState.isTestingModel,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (uiState.modelTestResult == null) MaterialTheme.colorScheme.secondaryContainer else statusColor.copy(alpha = 0.1f),
                                                contentColor = if (uiState.modelTestResult == null) MaterialTheme.colorScheme.onSecondaryContainer else statusColor
                                            )
                                        ) {
                                            if (uiState.isTestingModel) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = statusColor
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Text(stringResource(R.string.features_ai_configuration_ping_model))
                                        }
                                        if (uiState.modelTestResult != null) {
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = uiState.modelTestResult,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = statusColor,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // On-Device Local AI (Hardware Capability Badge)
                    ListItem(
                        colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = Color(0xFFFBF8F5)),
                        headlineContent = { Text(stringResource(R.string.features_ai_configuration_stage_2_title), fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Gemini Nano executes deterministically before reaching out to cloud providers if hardware is available.") },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = if (uiState.isLocalAiAvailable) Color(0xFF4CAF50) else Color(0xFFE53935),
                                            shape = CircleShape
                                        )
                                )
                                Text(
                                    text = if (uiState.isLocalAiAvailable) stringResource(R.string.features_ai_configuration_npu_active) else stringResource(R.string.features_ai_configuration_npu_unsupported),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isLocalAiAvailable) Color(0xFF4CAF50) else Color(0xFFE53935)
                                )
                            }
                        }
                    )

                    // Debug AI entry point
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onDebugClick) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "AI Diagnostics",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "AI Config Card - Expanded")
@Composable
private fun AiConfigurationCardExpandedPreview() {
    MaterialTheme {
        AiConfigurationCardContent(
            expanded = true,
            onExpandToggle = {},
            uiState = AiConfigurationUiState(
                isApiKeySet = false,
                useByokKey = false,
                isLocalAiAvailable = true,
                useFirebaseVertexAi = true,
                availableModels = listOf("gemini-3.8-flash", "gemini-2.5-flash")
            ),
            onEvent = {},
            title = "AI Configuration",
            description = "Configure your generative AI settings."
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "AI Config Card - Collapsed")
@Composable
private fun AiConfigurationCardCollapsedPreview() {
    MaterialTheme {
        AiConfigurationCardContent(
            expanded = false,
            onExpandToggle = {},
            uiState = AiConfigurationUiState(
                isAiEnabled = true,
                isLocalAiAvailable = true,
                useFirebaseVertexAi = true
            ),
            onEvent = {},
            title = "AI Configuration",
            description = "Configure your generative AI settings."
        )
    }
}
