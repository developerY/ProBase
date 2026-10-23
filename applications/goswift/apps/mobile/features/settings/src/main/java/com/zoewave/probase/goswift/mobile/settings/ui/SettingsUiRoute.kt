package com.zoewave.probase.goswift.mobile.settings.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord

@Composable
fun SettingsUiRoute(
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val healthConnectPermissions = remember {
        setOf(
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getWritePermission(NutritionRecord::class),
            HealthPermission.getReadPermission(NutritionRecord::class)
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.containsAll(healthConnectPermissions)) {
            Toast.makeText(context, "Health Connect Permissions Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Health Connect Permissions Updated", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestHealthPermissions() {
        when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_UNAVAILABLE -> {
                Toast.makeText(context, "Health Connect is unavailable on this device", Toast.LENGTH_SHORT).show()
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                val uriString = "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(context, "Health Connect update required", Toast.LENGTH_SHORT).show()
                }
            }
            HealthConnectClient.SDK_AVAILABLE -> {
                try {
                    permissionLauncher.launch(healthConnectPermissions)
                } catch (_: Exception) {
                    val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Unable to open Health Connect settings", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    SettingsUiScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRequestHealthPermissions = { requestHealthPermissions() },
        onUpdateMicrodoseQuantum = { viewModel.updateMicrodoseQuantum(it) },
        onUpdateCaffeineCeiling = { viewModel.updateCaffeineCeiling(it) },
        onToggleCurfew = { viewModel.toggleCurfew(it) },
        onToggleOnDeviceInference = { viewModel.toggleOnDeviceInference(it) },
        onUpdateHrvSensitivity = { viewModel.updateHrvSensitivity(it) },
        onTogglePostPrandial = { viewModel.togglePostPrandialResponse(it) },
        onToggleCalendarAnticipation = { viewModel.toggleCalendarAnticipation(it) },
        onUpdateHapticPattern = { viewModel.updateHapticPattern(it) },
        onToggleBioPrompts = { viewModel.toggleBioPrompts(it) },
        onToggleSlumpInterventions = { viewModel.toggleSlumpInterventions(it) },
        onPurgeCache = { viewModel.purgeCache() },
        onExportLog = { Toast.makeText(context, "Exporting Clinical PK Log (PDF)...", Toast.LENGTH_SHORT).show() },
        onCalibrateSensor = { Toast.makeText(context, "Sensor Latency Calibrated (Zero-drift)", Toast.LENGTH_SHORT).show() }
    )
}

@Composable
fun SettingsUiScreen(
    uiState: SettingsUiState,
    onNavigateBack: () -> Unit,
    onRequestHealthPermissions: () -> Unit,
    onUpdateMicrodoseQuantum: (MicrodoseQuantum) -> Unit,
    onUpdateCaffeineCeiling: (Float) -> Unit,
    onToggleCurfew: (Boolean) -> Unit,
    onToggleOnDeviceInference: (Boolean) -> Unit,
    onUpdateHrvSensitivity: (HrvSensitivity) -> Unit,
    onTogglePostPrandial: (Boolean) -> Unit,
    onToggleCalendarAnticipation: (Boolean) -> Unit,
    onUpdateHapticPattern: (HapticPattern) -> Unit,
    onToggleBioPrompts: (Boolean) -> Unit,
    onToggleSlumpInterventions: (Boolean) -> Unit,
    onPurgeCache: () -> Unit,
    onExportLog: () -> Unit,
    onCalibrateSensor: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101418))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header with Back Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = Color(0xFFE1E2E9)
                    )
                }
                Column {
                    Text(
                        text = "HUMMINGBIRD FUEL",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFB4AA),
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFFE1E2E9),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Surface(
                color = Color(0xFF1C2228),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF41474D))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFFF6F61), CircleShape)
                    )
                    Text(
                        text = if (uiState.onDeviceInferenceEnabled) "AI Active" else "AI Standby",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFE1E2E9)
                    )
                }
            }
        }

        // Profile Section
        Surface(
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color(0xFFFF6F61),
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("AR", style = MaterialTheme.typography.titleMedium, color = Color(0xFF101418), fontWeight = FontWeight.Bold)
                            }
                        }
                        Column {
                            Text("Alex Rivera", style = MaterialTheme.typography.titleMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                            Text("MD-PhD Candidate • Pro Endur...", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B9198))
                        }
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color(0xFF8B9198))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = Color(0xFF693C00),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "PRO METABOLIC TIER • ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFDCBE),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = Color(0xFF242C34),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "99.4% Adherence",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFEF08A),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFF242C34))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Daily Ceiling", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                        Text("${uiState.caffeineCeilingMg.toInt()} mg/day limit", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Receptor Sensitivity", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                        Text("High (Adaptive)", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFDBA74), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 1: DOSING & PHARMACOKINETICS
        SectionHeader("DOSING & PHARMACOKINETICS", "PK Model v3.2")
        Surface(
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Metabolic Profile (Clearance Rate)", style = MaterialTheme.typography.labelMedium, color = Color(0xFF8B9198))
                    Surface(
                        color = Color(0xFF161B20),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242C34)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Fast Metabolizer (A/A Allele)", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                            Text("4.8h Elimination Half-Life • Rapid Hepatic Flow", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Default Microdose Quantum", style = MaterialTheme.typography.labelMedium, color = Color(0xFF8B9198))
                        Text("${uiState.microdoseQuantum.mgText} Target Dose", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF6F61), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MicrodoseQuantum.entries.forEach { quantum ->
                            QuantumChip(quantum.label, uiState.microdoseQuantum == quantum) {
                                onUpdateMicrodoseQuantum(quantum)
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Daily Caffeine Ceiling", style = MaterialTheme.typography.labelMedium, color = Color(0xFF8B9198))
                        Text("${uiState.caffeineCeilingMg.toInt()} mg / 150 Safe Max", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFDBA74), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = uiState.caffeineCeilingMg,
                        onValueChange = { onUpdateCaffeineCeiling(it) },
                        valueRange = 40f..250f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF6F61),
                            activeTrackColor = Color(0xFFFF6F61),
                            inactiveTrackColor = Color(0xFF242C34)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smart Curfew Buffer", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                        Text("Lockout at 7:00 PM • 4.5h pre-bed adenosine shield", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                    }
                    Switch(
                        checked = uiState.curfewEnabled,
                        onCheckedChange = { onToggleCurfew(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF101418),
                            checkedTrackColor = Color(0xFFFF6F61)
                        )
                    )
                }
            }
        }

        // Section 2: ADAPTIVE NEURAL ENGINE
        SectionHeader("ADAPTIVE NEURAL ENGINE", "Edge TF-Lite 2.4")
        Surface(
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("On-Device Inference", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(14.dp))
                        }
                        Text("Zero telemetry leaves phone. Sub-8ms latency.", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                    }
                    Switch(
                        checked = uiState.onDeviceInferenceEnabled,
                        onCheckedChange = { onToggleOnDeviceInference(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF101418), checkedTrackColor = Color(0xFFFF6F61))
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("HRV Dip Sensitivity", style = MaterialTheme.typography.labelMedium, color = Color(0xFF8B9198))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HrvSensitivity.entries.forEach { sensitivity ->
                            SensitivityChip(sensitivity.label, uiState.hrvSensitivity == sensitivity) {
                                onUpdateHrvSensitivity(sensitivity)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Post-Prandial Response", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                        Text("Prompts hydration + microdose during glucose dips", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                    }
                    Switch(
                        checked = uiState.postPrandialResponseEnabled,
                        onCheckedChange = { onTogglePostPrandial(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF101418), checkedTrackColor = Color(0xFFFF6F61))
                    )
                }
            }
        }

        // Section 3: HARDWARE & HEALTH CONNECT
        SectionHeader("HARDWARE & HEALTH CONNECT", "3 Streams Active")
        Surface(
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Connected Telemetry Nodes", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                    Text(
                        "Manage Permissions",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF6F61),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onRequestHealthPermissions() }
                    )
                }

                NodeItem(Icons.Default.Watch, "Pixel Watch 2", "Continuous x84 & PPG • 100Hz", "Live", Color(0xFFFEF08A))
                NodeItem(Icons.Default.Sensors, "Dexcom G7 CGM", "Interstitial Glucose • Every 5m", "94 mg/dL", Color(0xFFFDBA74))
                NodeItem(Icons.Default.Watch, "Whoop 4.0", "Recovery Index 88% • Strain Sync", "Optimal", Color(0xFFFFB4AA))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Calendar Strain Anticipation", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                        Text("Schedules focus primes 20m before deep-work blocks", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                    }
                    Switch(
                        checked = uiState.calendarAnticipationEnabled,
                        onCheckedChange = { onToggleCalendarAnticipation(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF101418), checkedTrackColor = Color(0xFFFF6F61))
                    )
                }

                Surface(
                    color = Color(0xFF161B20),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242C34)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(20.dp))
                        Column {
                            Text("Zero Biometric Egress", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                            Text("Hardware Keystore AES-256 GCM encrypted", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                        }
                    }
                }
            }
        }

        // Section 4: TACTILE & BIO-PROMPTS
        SectionHeader("TACTILE & BIO-PROMPTS", "Haptics Engine")
        Surface(
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Wrist Haptic Pattern (Double Tap)", style = MaterialTheme.typography.labelMedium, color = Color(0xFF8B9198))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HapticPattern.entries.forEach { pattern ->
                            HapticChip(pattern.label, uiState.hapticPattern == pattern) {
                                onUpdateHapticPattern(pattern)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ambient Always-On Bio-Prompts", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                        Text("Glanceable fuel status on Android lock screen", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                    }
                    Switch(
                        checked = uiState.bioPromptsEnabled,
                        onCheckedChange = { onToggleBioPrompts(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF101418), checkedTrackColor = Color(0xFFFF6F61))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Critical Slump Interventions", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                        Text("Bypasses Do Not Disturb for critical tasks", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                    }
                    Switch(
                        checked = uiState.slumpInterventionsEnabled,
                        onCheckedChange = { onToggleSlumpInterventions(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF101418), checkedTrackColor = Color(0xFFFF6F61))
                    )
                }
            }
        }

        // Section 5: DIAGNOSTICS & DATA
        SectionHeader("DIAGNOSTICS & DATA", "")
        Surface(
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onExportLog() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242C34), contentColor = Color(0xFFFF6F61)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("EXPORT CLINICAL PK LOG (JSON / PDF)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }

                DiagnosticRow("Calibrate Sensor Latency", "Zero-drift clock sync with Bluetooth nodes") { onCalibrateSensor() }
                HorizontalDivider(color = Color(0xFF242C34))
                DiagnosticRow("Purge Local Inference-Cache", "${"%.1f".format(uiState.cacheSizeMb)}MB stored on-device") { onPurgeCache() }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Hummingbird OS v2.4.1 (Build 8904 - Edge AI Core)\nValidated with Clinical Chrono-metabolic Standards",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8B9198),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFFFB4AA),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B9198),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun RowScope.QuantumChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Color(0xFFFF6F61) else Color(0xFF161B20),
        shape = RoundedCornerShape(12.dp),
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242C34)) else null,
        modifier = Modifier.weight(1f).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color(0xFF101418) else Color(0xFFE1E2E9),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun RowScope.SensitivityChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Color(0xFF693C00) else Color(0xFF161B20),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Color(0xFFFDBA74) else Color(0xFF242C34)),
        modifier = Modifier.weight(1f).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color(0xFFFDBA74) else Color(0xFF8B9198),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun RowScope.HapticChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Color(0xFFFF6F61) else Color(0xFF161B20),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Color(0xFFFF6F61) else Color(0xFF242C34)),
        modifier = Modifier.weight(1f).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color(0xFF101418) else Color(0xFF8B9198),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun NodeItem(icon: ImageVector, title: String, subtitle: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = Color(0xFFE1E2E9), modifier = Modifier.size(20.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
            }
        }
        Text(status, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DiagnosticRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF8B9198))
    }
}
