package com.zoewave.probase.photodo.mobile.features.settings.ui.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsCard(
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    isAiEnabled: Boolean,
    onAiEnabledToggled: (Boolean) -> Unit,
    isApiKeySet: Boolean,
    currentAiModel: String,
    availableModels: List<String>,
    onAiModelSelected: (String) -> Unit,
    isTestingKey: Boolean,
    keyTestResult: String?,
    onTestKeyClicked: () -> Unit,
    isTestingModel: Boolean,
    modelTestResult: String?,
    onTestModelClicked: () -> Unit,
    onGeminiApiKeyChanged: (String?) -> Unit,
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
                    Text(text = "Smart Capture AI", style = MaterialTheme.typography.titleMedium)
                    val statusText = when {
                        !isAiEnabled -> "Off (No AI)"
                        !isApiKeySet -> "On (Local AI)"
                        else -> "On (Cloud-Enhanced)"
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
                        headlineContent = { Text("Enable AI Auto-fill") },
                        supportingContent = { Text("Use Gemini to automatically fill task details from photos.") },
                        trailingContent = {
                            Switch(
                                checked = isAiEnabled,
                                onCheckedChange = onAiEnabledToggled
                            )
                        }
                    )

                    Spacer(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "BYOK (Bring Your Own Key)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Get your free API key from Google AI Studio to enable high-fidelity multimodal parsing. Keys are stored in hardware-backed encrypted storage. Level 3 Cloud features include mandatory reporting tools; flagging an output will redirect you to Google’s external feedback portal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    if (isApiKeySet && editingKey.isBlank()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "✅ Gemini API Key is configured",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                TextButton(onClick = { onGeminiApiKeyChanged(null) }) {
                                    Text("Remove", color = MaterialTheme.colorScheme.error)
                                }
                            }
                            
                            // TEST KEY BUTTON
                            val keyColor = when {
                                keyTestResult == null -> MaterialTheme.colorScheme.primary
                                keyTestResult.startsWith("Key Valid") || keyTestResult.startsWith("Valid") -> Color(0xFF4CAF50)
                                else -> MaterialTheme.colorScheme.error
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = onTestKeyClicked,
                                    enabled = !isTestingKey,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (keyTestResult == null) MaterialTheme.colorScheme.primary else keyColor.copy(alpha = 0.1f),
                                        contentColor = if (keyTestResult == null) MaterialTheme.colorScheme.onPrimary else keyColor
                                    )
                                ) {
                                    if (isTestingKey) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = keyColor)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text("Test Connection & Discover Models")
                                }
                                if (keyTestResult != null) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = keyTestResult,
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
                        label = { Text(if (isApiKeySet) "Update API Key" else "Enter Gemini API Key") },
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
                                onGeminiApiKeyChanged(editingKey)
                                editingKey = ""
                            },
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        ) {
                            Text("Save Key")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // MODEL SELECTION
                    Text(
                        text = "Preferred AI Model",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = isModelDropdownExpanded && availableModels.isNotEmpty(),
                        onExpandedChange = { if (availableModels.isNotEmpty()) isModelDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = if (availableModels.isEmpty()) "Test connection to see models" else currentAiModel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gemini Model") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                            trailingIcon = { 
                                if (availableModels.isNotEmpty()) {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isModelDropdownExpanded) 
                                }
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = isModelDropdownExpanded,
                            onDismissRequest = { isModelDropdownExpanded = false }
                        ) {
                            availableModels.forEach { model ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = model,
                                            fontWeight = if (model == currentAiModel) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    onClick = {
                                        onAiModelSelected(model)
                                        isModelDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Text(
                        text = if (availableModels.isEmpty()) "Test connection to unlock available models." else "Choose your preferred Gemini model.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    if (availableModels.isNotEmpty()) {
                        val statusColor = when {
                            modelTestResult == null -> MaterialTheme.colorScheme.outline
                            modelTestResult.contains("Error") || 
                            modelTestResult.contains("Connection") || 
                            modelTestResult.contains("not enabled") ||
                            modelTestResult.contains("failed") ||
                            modelTestResult.contains("404") ||
                            modelTestResult.contains("403") -> MaterialTheme.colorScheme.error
                            else -> Color(0xFF4CAF50) // Material Green
                        }

                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = "Step 2: Verify Model Functionality",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Some models in the list might be restricted by your billing tier. Ping the selected model to confirm it responds.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Button(
                                    onClick = onTestModelClicked,
                                    enabled = !isTestingModel,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (modelTestResult == null) MaterialTheme.colorScheme.secondaryContainer else statusColor.copy(alpha = 0.1f),
                                        contentColor = if (modelTestResult == null) MaterialTheme.colorScheme.onSecondaryContainer else statusColor
                                    )
                                ) {
                                    if (isTestingModel) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = statusColor
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text("Ping Selected Model")
                                }
                                if (modelTestResult != null) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = modelTestResult,
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

@Preview(showBackground = true)
@Composable
fun AiSettingsCardCollapsedPreview() {
    PhotoDoTheme {
        AiSettingsCard(
            expanded = false,
            onExpandToggle = {},
            isAiEnabled = true,
            onAiEnabledToggled = {},
            isApiKeySet = true,
            currentAiModel = "gemini-1.5-flash",
            availableModels = listOf("gemini-1.5-flash", "gemini-1.5-pro"),
            onAiModelSelected = {},
            isTestingKey = false,
            keyTestResult = "Valid! Connection successful.",
            onTestKeyClicked = {},
            isTestingModel = false,
            modelTestResult = "Gemini 1.5 Flash v1",
            onTestModelClicked = {},
            onGeminiApiKeyChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AiSettingsCardExpandedPreview() {
    PhotoDoTheme {
        AiSettingsCard(
            expanded = true,
            onExpandToggle = {},
            isAiEnabled = true,
            onAiEnabledToggled = {},
            isApiKeySet = false,
            currentAiModel = "gemini-3.1-flash-lite-preview",
            availableModels = listOf("gemini-3.1-flash-lite-preview"),
            onAiModelSelected = {},
            isTestingKey = false,
            keyTestResult = null,
            onTestKeyClicked = {},
            isTestingModel = false,
            modelTestResult = null,
            onTestModelClicked = {},
            onGeminiApiKeyChanged = {}
        )
    }
}
