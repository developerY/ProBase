package com.zoewave.probase.features.health.ui

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.zoewave.probase.features.health.ui.components.ErrorScreen
import com.zoewave.probase.features.health.ui.components.HealthConnectionStatus
import com.zoewave.probase.features.health.ui.components.HealthDashboardTabs

@Composable
fun HealthRoute(
    modifier: Modifier = Modifier,
    statusOnly : Boolean = false,
    viewModel: HealthViewModel = hiltViewModel()
) {
    // 1. Collect State & Context
    val state by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // 2. Setup Launchers
    // Launcher for opening Health Connect Settings
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            // When user returns from settings, reload data to check if permissions changed
            viewModel.onEvent(HealthEvent.LoadHealthData)
        }
    )

    // Launcher for requesting permissions
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
        onResult = {
            // When returning from permission screen, force reload
            viewModel.onEvent(HealthEvent.LoadHealthData)
        }
    )

    // 3. Handle Side Effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is HealthSideEffect.LaunchPermissions -> {
                    Log.d("HealthRoute", "Launching Permissions Contract")
                    permissionsLauncher.launch(effect.permissions)
                }
                is HealthSideEffect.OpenHealthConnectSettings -> {
                    // Create the intent to open Health Connect settings
                    val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
                    settingsLauncher.launch(intent)
                }
                // Handle other effects if necessary (e.g. Sync success toast)
                else -> { /* No-op for now */ }
            }
        }
    }

    // 4. Lifecycle Observer (Auto-reload on Resume)
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.onEvent(HealthEvent.LoadHealthData)
        }
    }

    // 5. Render UI
    Box(
        modifier = modifier
            .then(if (statusOnly) Modifier.wrapContentHeight() else Modifier.fillMaxSize()) // <--- Optimization
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val uiState = state) {
            is HealthUiState.Success -> {
                if (statusOnly) {
                    HealthConnectionStatus(
                        onEvent = viewModel::onEvent,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    HealthDashboardTabs(
                        state = uiState,
                        onEvent = viewModel::onEvent
                    )
                }
            }

            is HealthUiState.PermissionsRequired -> {
                PermissionsNotGrantedScreen(
                    onEnableClick = { viewModel.onEvent(HealthEvent.RequestPermissions) }
                )
            }

            is HealthUiState.Error -> {
                ErrorScreen(
                    message = uiState.message, // Ensure ErrorScreen parameter matches "errorMessage"
                    onRetry = { viewModel.onEvent(HealthEvent.Retry) }
                )
            }

            // Loading or Initial state
            else -> {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun PermissionsNotGrantedScreen(onEnableClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Health Connect Permissions Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please grant access to show your weekly activity.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onEnableClick) {
            Text("Grant Permissions")
        }
    }
}