package com.zoewave.probase.kocolor.mobile.core.ui.health

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.features.health.core.ui.HealthEvent
import com.zoewave.probase.features.health.core.ui.HealthSideEffect
import com.zoewave.probase.features.health.core.ui.HealthUiState
import com.zoewave.probase.features.health.core.ui.settings.HealthConnectionStatus
import com.zoewave.probase.kocolor.mobile.core.ui.theme.KoColorTheme
import com.zoewave.probase.kocolor.model.KoColorRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthUiRoute(
    uiState: HealthUiState,
    onEvent: (HealthEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier,
    sideEffects: Flow<HealthSideEffect> = emptyFlow()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health & Wellness") },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        HealthContent(
            uiState = uiState,
            onEvent = onEvent,
            navTo = navTo,
            sideEffects = sideEffects,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        )
    }
}

@Composable
fun HealthContent(
    uiState: HealthUiState,
    onEvent: (HealthEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier,
    sideEffects: Flow<HealthSideEffect> = emptyFlow(),
    statusOnly: Boolean = false
) {
    val context = LocalContext.current
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = androidx.health.connect.client.PermissionController.createRequestPermissionResultContract()
    ) {
        onEvent(HealthEvent.LoadHealthData)
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) {
        onEvent(HealthEvent.LoadHealthData)
    }

    LaunchedEffect(Unit) {
        onEvent(HealthEvent.LoadHealthData)
        sideEffects.collect { effect ->
            when (effect) {
                is HealthSideEffect.LaunchPermissions -> {
                    try {
                        permissionsLauncher.launch(effect.permissions)
                    } catch (e: Exception) {
                        android.util.Log.e("HealthContent", "Failed to launch permissions", e)
                    }
                }
                HealthSideEffect.OpenHealthConnectSettings -> {
                    val packageName = context.packageName
                    val intents = listOf(
                        // 1. App-specific permissions (Android 14+)
                        Intent("android.health.connect.action.MANAGE_HEALTH_PERMISSIONS")
                            .putExtra(Intent.EXTRA_PACKAGE_NAME, packageName),
                        // 2. General Health Connect Settings (System Integrated)
                        Intent("android.settings.HEALTH_CONNECT_SETTINGS"),
                        // 3. Legacy Health Connect App Settings
                        Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS")
                    )

                    var launched = false
                    for (intent in intents) {
                        try {
                            settingsLauncher.launch(intent)
                            launched = true
                            break
                        } catch (ignore: Exception) {
                            android.util.Log.w("HealthContent", "Could not launch intent: ${intent.action}")
                        }
                    }

                    if (!launched) {
                        // Final fallback: Play Store
                        try {
                            val playStoreIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=com.google.android.apps.healthdata"))
                            playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(playStoreIntent)
                        } catch (ignore: Exception) {}
                    }
                }
                else -> {}
            }
        }
    }

    Box(modifier = modifier) {
        when (uiState) {
            is HealthUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HealthUiState.Success -> {
                if (statusOnly) {
                    HealthConnectionStatus(
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                } else {
                    StyleHealthDashboard(
                        uiState = uiState,
                        onEvent = onEvent,
                        navTo = navTo,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            is HealthUiState.PermissionsRequired -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(uiState.message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { onEvent(HealthEvent.RequestPermissions) }) {
                        Text("Grant Permissions")
                    }
                }
            }
            is HealthUiState.Error -> {
                Text(
                    text = uiState.message,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            HealthUiState.Disabled -> {
                Text(
                    text = "Health Connect is disabled on this device.",
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    textAlign = TextAlign.Center
                )
            }
            HealthUiState.Uninitialized -> {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HealthUiRoutePreview() {
    KoColorTheme {
        HealthUiRoute(
            uiState = HealthUiState.Loading,
            onEvent = {},
            navTo = {}
        )
    }
}
