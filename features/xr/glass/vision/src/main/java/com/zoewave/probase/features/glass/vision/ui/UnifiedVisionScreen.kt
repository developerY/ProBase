package com.zoewave.probase.features.glass.vision.ui

import android.Manifest
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.ExperimentalLensFacing
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NoEncryption
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.zoewave.probase.features.glass.vision.ui.components.VisionRequirementGate
import kotlinx.coroutines.Dispatchers

@ExperimentalLensFacing
@ExperimentalCamera2Interop
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalProjectedApi::class)
@Composable
fun UnifiedVisionRoute(
    viewModel: VisionViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onRequestGlassesPermission: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initial sync
    LaunchedEffect(cameraPermissionState.status.isGranted) {
        viewModel.onEvent(VisionUiEvent.CheckPermissions(context))
    }

    // Refresh permission and camera status on Resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(VisionUiEvent.CheckPermissions(context))
                // Always try to initialize if glasses granted, to re-discover hardware
                if (activity != null) {
                    val glassesGranted = viewModel.cameraManager.checkGlassesPermission(activity)
                    if (glassesGranted) {
                        viewModel.cameraManager.initialize(activity)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Initial sync
    LaunchedEffect(cameraPermissionState.status.isGranted) {
        viewModel.onEvent(VisionUiEvent.CheckPermissions(context))
    }

    UnifiedVisionScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToSettings = onNavigateToSettings,
        onRequestGlassesPermission = onRequestGlassesPermission,
        onBack = onBack,
        requestPhonePermission = { cameraPermissionState.launchPermissionRequest() }
    )
}

/**
 * A stateless Glimmer-optimized vision screen for AI Glasses.
 */
@ExperimentalLensFacing
@ExperimentalCamera2Interop
@OptIn(ExperimentalMaterial3Api::class, ExperimentalProjectedApi::class)
@Composable
fun UnifiedVisionScreen(
    uiState: VisionUiState,
    onEvent: (VisionUiEvent) -> Unit,
    onNavigateToSettings: () -> Unit,
    onRequestGlassesPermission: () -> Unit,
    onBack: () -> Unit,
    requestPhonePermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val logListState = rememberLazyListState()

    // Diagnostic state: Connectivity check
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
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Vision Diagnostic Hub") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        VisionRequirementGate(
            uiState = uiState,
            onNavigateToSettings = onNavigateToSettings,
            onRequestPhonePermission = requestPhonePermission,
            onRequestGlassesPermission = onRequestGlassesPermission
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
                            inactiveIcon = Icons.Default.VisibilityOff,
                            statusOverride = if (isConnected) "CONNECTED" else "DISCONNECTED"
                        )
                        DiagnosticRow(
                            label = "Phone Camera Access",
                            isActive = uiState.isPermissionGranted,
                            activeIcon = Icons.Default.Camera,
                            inactiveIcon = Icons.Default.CameraAlt,
                            statusOverride = if (uiState.isPermissionGranted) "ALLOWED" else "DENIED"
                        )
                        DiagnosticRow(
                            label = "Glasses Camera Access",
                            isActive = uiState.isGlassesPermissionGranted,
                            activeIcon = Icons.Default.Camera,
                            inactiveIcon = Icons.Default.CameraAlt,
                            statusOverride = if (uiState.isGlassesPermissionGranted) "ALLOWED" else "DENIED"
                        )
                        DiagnosticRow(
                            label = "Camera Source",
                            isActive = uiState.cameraSource.contains("Glasses"),
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

                // --- DISCOVERED CAMERAS SECTION ---
                if (uiState.discoveredCameras.isNotEmpty()) {
                    Text("Discovered Cameras", style = MaterialTheme.typography.labelLarge)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            uiState.discoveredCameras.forEach { (device, info) ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(device, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text(info, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
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
                                bitmap = uiState.capturedImage.asImageBitmap(),
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
                            requestPhonePermission()
                        } else {
                            onEvent(VisionUiEvent.TriggerCapture)
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
                        text = uiState.error,
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
