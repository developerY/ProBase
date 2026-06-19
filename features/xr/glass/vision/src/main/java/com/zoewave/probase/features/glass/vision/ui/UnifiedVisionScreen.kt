package com.zoewave.probase.features.glass.vision.ui

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.xr.projected.ProjectedContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.zoewave.probase.features.glass.vision.ui.components.VisionRequirementGate
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class, androidx.xr.projected.experimental.ExperimentalProjectedApi::class)
@Composable
fun UnifiedVisionScreen(
    viewModel: VisionViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val logListState = rememberLazyListState()

    // Sync permission status with ViewModel and trigger camera setup
    LaunchedEffect(cameraPermissionState.status.isGranted) {
        viewModel.updatePermissionStatus(cameraPermissionState.status.isGranted)
        if (cameraPermissionState.status.isGranted && activity != null) {
            viewModel.setupCamera(activity)
        }
    }

    // Diagnostic state: Correct phone-side connectivity check
    var isConnected by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= 36) {
            ProjectedContext.isProjectedDeviceConnected(context, Dispatchers.Main).collect { connected ->
                isConnected = connected
            }
        }
    }

    // Auto-scroll logs to bottom
    LaunchedEffect(uiState.logs.size) {
        if (uiState.logs.isNotEmpty()) {
            logListState.animateScrollToItem(uiState.logs.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vision Diagnostic Hub") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        VisionRequirementGate(
            viewModel = viewModel,
            onNavigateToSettings = onNavigateToSettings
        ) {
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
                    Text("System Status", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    
                    DiagnosticRow(
                        label = "Glasses Connection",
                        isActive = isConnected,
                        activeIcon = Icons.Default.Visibility,
                        inactiveIcon = Icons.Default.VisibilityOff
                    )
                    DiagnosticRow(
                        label = "Camera Permission",
                        isActive = uiState.isPermissionGranted,
                        activeIcon = Icons.Default.Camera,
                        inactiveIcon = Icons.Default.CameraAlt
                    )
                    DiagnosticRow(
                        label = "Camera Source",
                        isActive = uiState.cameraSource == "Glasses",
                        activeIcon = Icons.Default.CheckCircle,
                        inactiveIcon = Icons.Default.Error,
                        statusOverride = uiState.cameraSource,
                        isCritical = true
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

            // --- CAMERA PREVIEW SECTION ---
            Text("Last Captured Image", style = MaterialTheme.typography.labelLarge)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (uiState.capturedImage != null) {
                        Image(
                            bitmap = uiState.capturedImage!!.asImageBitmap(),
                            contentDescription = "Captured Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Image, 
                                contentDescription = null, 
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("No image captured yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    if (uiState.isCapturing) {
                        CircularProgressIndicator()
                    }
                }
            }

            // --- ANALYSIS RESULT ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Gemini Description", style = MaterialTheme.typography.labelMedium)
                    if (uiState.isAnalyzing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Processing...", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        Text(
                            text = if (uiState.imageDescription.isEmpty()) "Ready for capture" else uiState.imageDescription,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- EVENT LOG SECTION ---
            Text("Live Event Log", style = MaterialTheme.typography.labelLarge)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                LazyColumn(
                    state = logListState,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(uiState.logs) { log ->
                        Text(
                            text = log,
                            color = if (log.contains("Error", true)) Color.Red else Color.Green,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }

            // --- CONTROLS SECTION ---
            Button(
                onClick = {
                    if (!uiState.isPermissionGranted) {
                        cameraPermissionState.launchPermissionRequest()
                    } else {
                        viewModel.takePicture()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                enabled = !uiState.isCapturing && !uiState.isAnalyzing
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("TRIGGER GLASSES CAMERA")
            }
            
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
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
    isCritical: Boolean = false,
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
            color = if (isActive) Color(0xFF4CAF50) else (if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}
