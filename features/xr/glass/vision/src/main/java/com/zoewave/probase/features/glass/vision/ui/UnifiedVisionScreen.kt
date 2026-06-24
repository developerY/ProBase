package com.zoewave.probase.features.glass.vision.ui

import android.Manifest
import android.util.Log
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
import androidx.compose.ui.tooling.preview.Preview
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
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    val isGranted = cameraPermissionState.status.isGranted

    // Cleanup singleton state when leaving the diagnostic hub
    DisposableEffect(Unit) {
        onDispose {
            Log.d("VisionUI", "Leaving Diagnostic Hub. Tearing down camera manager.")
            viewModel.cameraManager.teardown()
        }
    }

    // 1. Establish the Phone instance as the Commander
    LaunchedEffect(Unit) {
        Log.d("VisionUI", "Establishing Phone as Host Commander...")
        viewModel.initializeAsHostCommander()
    }

    // 2. Bulletproof Initialization: Key on both Permission and Activity presence
    LaunchedEffect(isGranted, activity) {
        viewModel.onEvent(VisionUiEvent.CheckPermissions(context))
        if (isGranted && activity != null) {
            Log.d("VisionUI", "Permission is GRANTED and Activity is ready. FIRING INITIALIZE!")
            viewModel.cameraManager.initialize(activity)
        } else {
            Log.d("VisionUI", "Initialization pending: isGranted=$isGranted, activity=${activity?.let { "Ready" } ?: "Null"}")
        }
    }

    // Refresh status on Resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            Log.d("VisionUI", "UnifiedVisionRoute Lifecycle: $event")
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(VisionUiEvent.CheckPermissions(context))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    UnifiedVisionScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToSettings = onNavigateToSettings,
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
    onBack: () -> Unit,
    requestPhonePermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
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
            onRequestPhonePermission = requestPhonePermission
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
                            label = "Camera Access",
                            isActive = uiState.isPermissionGranted,
                            activeIcon = Icons.Default.Camera,
                            inactiveIcon = Icons.Default.CameraAlt,
                            statusOverride = if (uiState.isPermissionGranted) "ALLOWED" else "DENIED"
                        )
                        DiagnosticRow(
                            label = "Camera Source",
                            isActive = uiState.cameraSource.contains("Hardware"),
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            Log.d("VisionUI", "CAPTURE_BUTTON_CLICKED (Phone UI)")
                            if (!uiState.isPermissionGranted) {
                                requestPhonePermission()
                            } else {
                                onEvent(VisionUiEvent.TriggerCapture)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CircleShape,
                        enabled = !uiState.isCapturing && !uiState.isAnalyzing
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text("CAPTURE IMAGE")
                    }

                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            activity?.let {
                                onEvent(VisionUiEvent.RunDiagnostic(it))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape
                    ) {
                        Text("RUN HARDWARE DIAGNOSTIC")
                    }

                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            activity?.let {
                                onEvent(VisionUiEvent.RunOfficialTest(it))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape
                    ) {
                        Text("RUN OFFICIAL SDK TEST")
                    }
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

@ExperimentalLensFacing
@ExperimentalCamera2Interop
@Preview(showBackground = true)
@Composable
private fun UnifiedVisionScreenPreview() {
    MaterialTheme {
        UnifiedVisionScreen(
            uiState = VisionUiState(
                cameraSource = "Hardware Camera (Projected)",
                isPermissionGranted = true,
                isApiKeySet = true,
                imageDescription = "A scenic view of a mountain path."
            ),
            onEvent = {},
            onNavigateToSettings = {},
            onBack = {},
            requestPhonePermission = {}
        )
    }
}
