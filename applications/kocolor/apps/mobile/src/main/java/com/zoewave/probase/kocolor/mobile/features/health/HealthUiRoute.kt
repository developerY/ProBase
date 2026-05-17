package com.zoewave.probase.kocolor.mobile.features.health

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zoewave.probase.features.health.core.ui.HealthEvent
import com.zoewave.probase.features.health.core.ui.HealthSideEffect
import com.zoewave.probase.features.health.core.ui.HealthUiState
import com.zoewave.probase.features.health.core.ui.HealthViewModel
import com.zoewave.probase.features.health.core.ui.components.HealthDashboard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthUiRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HealthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val permissionsLauncher = rememberLauncherForActivityResult(
        viewModel.healthSessionManager.requestPermissionsActivityContract()
    ) {
        viewModel.onEvent(HealthEvent.LoadHealthData)
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(HealthEvent.LoadHealthData)
        viewModel.sideEffect.collect { effect ->
            when (val event = effect) {
                is HealthSideEffect.LaunchPermissions -> {
                    permissionsLauncher.launch(event.permissions)
                }
                HealthSideEffect.OpenHealthConnectSettings -> {
                    val settingsIntent = Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS")
                    context.startActivity(settingsIntent)
                }
                is HealthSideEffect.BikeRideSyncedToHealth -> {
                    // Not used in KoColor probably
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health & Wellness") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is HealthUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HealthUiState.Success -> {
                    StyleHealthDashboard(
                        state = state,
                        onEvent = viewModel::onEvent
                    )
                }
                is HealthUiState.PermissionsRequired -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message)
                        Button(onClick = { viewModel.onEvent(HealthEvent.RequestPermissions) }) {
                            Text("Grant Permissions")
                        }
                    }
                }
                is HealthUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                }
                HealthUiState.Disabled -> {
                    Text("Health Connect is disabled on this device.", modifier = Modifier.align(Alignment.Center))
                }
                HealthUiState.Uninitialized -> {}
            }
        }
    }
}
