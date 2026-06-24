package com.zoewave.probase.features.glass.translation.ui

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NoEncryption
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.xr.projected.ProjectedContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class, androidx.xr.projected.experimental.ExperimentalProjectedApi::class)
@Composable
fun UnifiedTranslationScreen(
    viewModel: TranslationViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val context = LocalContext.current

    // Sync permission status with ViewModel
    LaunchedEffect(micPermissionState.status.isGranted) {
        viewModel.updatePermissionStatus(micPermissionState.status.isGranted)
    }
    
    // Diagnostic state: Correct phone-side connectivity check
    var isConnected by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= 36) {
            ProjectedContext.isProjectedDeviceConnected(context, Dispatchers.Main).collect { connected ->
                isConnected = connected
            }
        } else {
            // Simple check if any display name matches PROJECTED_DISPLAY_NAME
            // or just rely on alphabetic version of ProjectedContext if available
            isConnected = false 
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Translation Hub") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- DIAGNOSTICS SECTION ---
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("System Diagnostics", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    
                    DiagnosticRow(
                        label = "Glasses Connection",
                        isActive = isConnected,
                        activeIcon = Icons.Default.Visibility,
                        inactiveIcon = Icons.Default.VisibilityOff
                    )
                    DiagnosticRow(
                        label = "Speech Engine (ASR)",
                        isActive = uiState.isEngineAvailable,
                        activeIcon = Icons.Default.CheckCircle,
                        inactiveIcon = Icons.Default.Error
                    )
                    DiagnosticRow(
                        label = "Microphone Permission",
                        isActive = uiState.isPermissionGranted,
                        activeIcon = Icons.Default.Mic,
                        inactiveIcon = Icons.Default.MicOff
                    )
                    DiagnosticRow(
                        label = "Microphone Source",
                        isActive = uiState.micSource == "Glasses",
                        activeIcon = Icons.Default.CheckCircle,
                        inactiveIcon = Icons.Default.Error,
                        statusOverride = uiState.micSource
                    )
                    DiagnosticRow(
                        label = "Gemini API Key",
                        isActive = uiState.isApiKeySet,
                        activeIcon = Icons.Default.VpnKey,
                        inactiveIcon = Icons.Default.NoEncryption,
                        onClick = onNavigateToSettings
                    )
                }
            }

            // --- LIVE FEED SECTION ---
            Card(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Live Transcription", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = if (uiState.transcribedText.isEmpty()) "Waiting for speech..." else uiState.transcribedText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.transcribedText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp))
                    Spacer(Modifier.height(32.dp))

                    Text("Spanish Translation", style = MaterialTheme.typography.labelMedium)
                    if (uiState.isTranslating) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    } else {
                        Text(
                            text = if (uiState.translatedText.isEmpty()) "---" else uiState.translatedText,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // --- CONTROLS SECTION ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!uiState.isPermissionGranted) {
                    val rationale = if (micPermissionState.status.shouldShowRationale) {
                        "The app needs microphone access to transcribe your speech. Tap below to grant."
                    } else {
                        "Microphone access is blocked. Please enable it in System Settings."
                    }
                    Text(
                        text = rationale,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    if (micPermissionState.status.shouldShowRationale) {
                        Button(
                            onClick = { micPermissionState.launchPermissionRequest() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("REQUEST MICROPHONE ACCESS")
                        }
                    } else {
                        // This logic usually triggers when "Don't ask again" is set
                        TextButton(
                            onClick = { 
                                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = android.net.Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Text("OPEN SYSTEM SETTINGS")
                        }
                    }
                } else if (uiState.error != null) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                if (uiState.isPermissionGranted) {
                    Button(
                        onClick = {
                            if (uiState.isListening) viewModel.stopListening() else viewModel.startListening()
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = CircleShape,
                        colors = if (uiState.isListening) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors()
                    ) {
                        Icon(
                            imageVector = if (uiState.isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (uiState.isListening) "STOP MICROPHONE" else "START TRANSLATING",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    androidx.compose.material3.OutlinedButton(
                        onClick = { viewModel.runMicDiagnostic(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape
                    ) {
                        Text("RUN OFFICIAL MIC TEST")
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticRow(
    label: String, 
    isActive: Boolean, 
    activeIcon: ImageVector, 
    inactiveIcon: ImageVector,
    statusOverride: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isActive) activeIcon else inactiveIcon,
                contentDescription = null,
                tint = if (isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = statusOverride ?: (if (isActive) "OK" else "FAIL"),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
        )
    }
}
