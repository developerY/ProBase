package com.zoewave.probase.ashbike.mobile.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.ashbike.features.main.navigation.AshBikeDestination
import com.zoewave.probase.ashbike.features.main.ui.AshBikeSharedScreen
import com.zoewave.probase.ashbike.mobile.ui.MainUiEvent
import com.zoewave.probase.ashbike.mobile.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AshBikeMainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // --- Platform Side Effects (Permissions) ---
    // Note: The launcher must live in the composable, but the RESULT goes to the VM.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val isGranted = perms.values.all { it }
        viewModel.onEvent(MainUiEvent.OnPermissionResult(isGranted))
    }

    // Check permission on Start
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.onEvent(MainUiEvent.OnPermissionResult(hasPermission))

        if (!hasPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // --- Back Handler ---
    // We delegate the decision of "what happens on back" to the VM
    BackHandler(enabled = state.currentDestination != AshBikeDestination.Home) {
        viewModel.onEvent(MainUiEvent.OnBackPressed)
    }

    // --- UI Rendering ---
    if (state.hasLocationPermission) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val title = when (state.currentDestination) {
                            AshBikeDestination.Home -> "AshBike Dashboard"
                            AshBikeDestination.RideHistory -> "Your Rides"
                            AshBikeDestination.Settings -> "Settings"
                        }
                        Text(title)
                    }
                )
            },
            bottomBar = {
                AshBikeBottomBar(
                    currentDestination = state.currentDestination,
                    showSettingsBadge = state.showSettingsBadge,
                    onNavigate = { dest -> viewModel.onEvent(MainUiEvent.OnTabSelected(dest)) }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                when (state.currentDestination) {
                    AshBikeDestination.Home -> AshBikeSharedScreen("Dashboard")
                    AshBikeDestination.RideHistory -> AshBikeSharedScreen("History")
                    AshBikeDestination.Settings -> AshBikeSharedScreen("Settings")
                }
            }
        }
    } else {
        // Permission Denied UI
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text("Location Needed", modifier = Modifier.padding(16.dp))
                Button(onClick = {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                }) {
                    Text("Grant Permissions")
                }
            }
        }
    }
}