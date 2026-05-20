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
    sideEffects: Flow<HealthSideEffect> = emptyFlow()
) {
    val context = LocalContext.current
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = androidx.health.connect.client.PermissionController.createRequestPermissionResultContract()
    ) {
        onEvent(HealthEvent.LoadHealthData)
    }

    LaunchedEffect(Unit) {
        onEvent(HealthEvent.LoadHealthData)
        sideEffects.collect { effect ->
            when (effect) {
                is HealthSideEffect.LaunchPermissions -> {
                    permissionsLauncher.launch(effect.permissions)
                }
                HealthSideEffect.OpenHealthConnectSettings -> {
                    val settingsIntent = Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS")
                    context.startActivity(settingsIntent)
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
                StyleHealthDashboard(
                    uiState = uiState,
                    onEvent = onEvent,
                    navTo = navTo,
                    modifier = Modifier.fillMaxWidth()
                )
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
