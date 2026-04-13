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
    onAiModelSelected: (String) -> Unit,
    isTestingKey: Boolean,
    keyTestResult: String?,
    onTestKeyClicked: () -> Unit,
    onGeminiApiKeyChanged: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingKey by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var isModelDropdownExpanded by remember { mutableStateOf(false) }

    val models = listOf(
        "gemini-1.5-flash",
        "gemini-1.5-pro",
        "gemini-3.1-flash-lite-preview",
        "gemini-3.1-pro-preview",
        "gemini-3-flash-preview"
    )

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = onTestKeyClicked,
                                    enabled = !isTestingKey,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    if (isTestingKey) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text("Test Connection")
                                }
                                if (keyTestResult != null) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = keyTestResult,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (keyTestResult.startsWith("Valid")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
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
                        expanded = isModelDropdownExpanded,
                        onExpandedChange = { isModelDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = currentAiModel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gemini Model") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isModelDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = isModelDropdownExpanded,
                            onDismissRequest = { isModelDropdownExpanded = false }
                        ) {
                            models.forEach { model ->
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
            onAiModelSelected = {},
            isTestingKey = false,
            keyTestResult = "Valid! Connection successful.",
            onTestKeyClicked = {},
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
            onAiModelSelected = {},
            isTestingKey = false,
            keyTestResult = null,
            onTestKeyClicked = {},
            onGeminiApiKeyChanged = {}
        )
    }
}
