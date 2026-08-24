package com.zoewave.probase.features.ai.configuration.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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
fun AiConfigurationCard(
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.features_ai_configuration_title),
    description: String = stringResource(R.string.features_ai_configuration_desc),
    viewModel: AiConfigurationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    AiConfigurationCardContent(
        expanded = expanded,
        onExpandToggle = onExpandToggle,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        title = title,
        description = description,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiConfigurationCardContent(
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    uiState: AiConfigurationUiState,
    onEvent: (AiConfigurationEvent) -> Unit,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    var editingKey by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var isModelDropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
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
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    val statusText = when {
                        !uiState.isAiEnabled -> stringResource(R.string.features_ai_configuration_status_off)
                        !uiState.isApiKeySet -> stringResource(R.string.features_ai_configuration_status_local)
                        else -> stringResource(R.string.features_ai_configuration_status_cloud)
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.features_ai_configuration_enable_features)) },
                        supportingContent = { Text(description) },
                        trailingContent = {
                            Switch(
                                checked = uiState.isAiEnabled,
                                onCheckedChange = { onEvent(AiConfigurationEvent.OnAiEnabledToggled(it)) }
                            )
                        }
                    )

                    Spacer(modifier = Modifier.padding(vertical = 8.dp))

                    ListItem(
                        headlineContent = { Text(stringResource(R.string.features_ai_configuration_use_firebase_title)) },
                        supportingContent = { Text(stringResource(R.string.features_ai_configuration_use_firebase_desc)) },
                        trailingContent = {
                            Switch(
                                checked = uiState.useFirebaseVertexAi,
                                onCheckedChange = { onEvent(AiConfigurationEvent.OnUseFirebaseVertexAiToggled(it)) }
                            )
                        }
                    )

                    Spacer(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = stringResource(R.string.features_ai_configuration_byok_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.features_ai_configuration_byok_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

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
                }
            }
        }
    }
}
