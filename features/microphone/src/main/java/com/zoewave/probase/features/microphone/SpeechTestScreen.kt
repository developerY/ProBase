package com.zoewave.probase.features.microphone

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpeechTestScreen() {
    val context = LocalContext.current
    val engine = remember { SpeechEngine(context) }
    val transcribedText by engine.textState.collectAsState()
    val logs by engine.logs.collectAsState()
    val rmsLevel by engine.rmsLevel.collectAsState()
    val isServiceReady by engine.isServiceReady.collectAsState()
    val hasDetectedSignal by engine.hasDetectedSignal.collectAsState()
    
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasPermission = isGranted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    DisposableEffect(Unit) {
        onDispose { engine.stopListening() }
    }

    val logListState = rememberLazyListState()
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            logListState.animateScrollToItem(logs.size - 1)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Audio Isolation Test",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DiagnosticPanel(
                hasPermission = hasPermission,
                isAvailable = engine.isRecognitionAvailable,
                isServiceReady = isServiceReady,
                hasDetectedSignal = hasDetectedSignal
            )

            Spacer(modifier = Modifier.height(16.dp))

            VolumeIndicator(rmsLevel = rmsLevel)

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = transcribedText,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = if (transcribedText.startsWith("ERROR")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { engine.startListening() },
                    enabled = hasPermission
                ) {
                    Text("Start")
                }
                
                OutlinedButton(
                    onClick = { engine.stopListening() }
                ) {
                    Text("Stop")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Detailed Logs",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp)
            )

            LazyColumn(
                state = logListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.05f))
                    .padding(4.dp)
            ) {
                items(logs) { log ->
                    Text(
                        text = "> $log",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DiagnosticPanel(
    hasPermission: Boolean,
    isAvailable: Boolean,
    isServiceReady: Boolean,
    hasDetectedSignal: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DiagnosticItem("Microphone Permission", hasPermission)
        DiagnosticItem("Engine Available", isAvailable)
        DiagnosticItem("Service Ready", isServiceReady)
        DiagnosticItem("Signal Detected", hasDetectedSignal)
    }
}

@Composable
fun DiagnosticItem(label: String, isOk: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (isOk) Color(0xFF4CAF50) else Color(0xFFF44336),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isOk) FontWeight.Normal else FontWeight.Bold
        )
    }
}

@Composable
fun VolumeIndicator(rmsLevel: Float) {
    // RMS typically goes from -2 to 10 or more. Normalize for progress bar (0 to 1)
    val normalizedLevel = ((rmsLevel + 2f) / 12f).coerceIn(0f, 1f)
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Input Level", style = MaterialTheme.typography.labelSmall)
            Text("${rmsLevel.toInt()} dB", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { normalizedLevel },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = when {
                normalizedLevel > 0.8f -> Color.Red
                normalizedLevel > 0.5f -> Color.Yellow
                else -> Color.Green
            },
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
