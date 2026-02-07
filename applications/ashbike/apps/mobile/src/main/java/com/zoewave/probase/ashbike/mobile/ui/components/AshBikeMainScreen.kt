package com.zoewave.probase.ashbike.mobile.ui.components


// --- App Logic Imports ---
// import com.zoewave.ashbike.mobile.ui.MainViewModel
// import com.zoewave.ashbike.mobile.ui.MainUiEvent
//import com.zoewave.probase.ashbike.features.main.ui.AshBikeSharedScreen
//import com.zoewave.probase.ashbike.mobile.ui.MainUiEvent

// --- Feature Screens ---
/**
 * built from current
 */

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.ui.NavDisplay
import com.zoewave.ashbike.mobile.home.ui.HomeViewModel
import com.zoewave.probase.ashbike.features.main.navigation.AshBikeDestination
import com.zoewave.probase.ashbike.mobile.ui.MainUiEvent
import com.zoewave.probase.ashbike.mobile.ui.MainViewModel
import com.zoewave.probase.ashbike.mobile.ui.navigation.ashBikeNavEntryProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AshBikeMainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val homeViewModel : HomeViewModel = hiltViewModel()

    val unsyncedRidesCount by viewModel.unsyncedRidesCount.collectAsState()
    // 1. THE SOURCE OF TRUTH (The Back Stack)
    // In Nav3, the back stack is just a standard Compose MutableList.
    // We initialize it with 'Home' as the first screen.
    val backStack = remember {
        mutableStateListOf<AshBikeDestination>(AshBikeDestination.Home)
    }

    // Helper to push a new screen
    fun navigateTo(destination: AshBikeDestination) {
        backStack.add(destination)
    }

    // Helper to handle "Back"
    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    // Helper for Bottom Bar (Tab Switching)
    fun switchTab(destination: AshBikeDestination) {
        backStack.clear()
        backStack.add(destination)
    }

    // 2. Handle System Back Button
    BackHandler(enabled = backStack.size > 1) {
        navigateBack()
    }
    // Helper to determine what tab is active (last item in the list)
    // val currentDestination = backStack.lastOrNull()

    // ✅ FIX: Use derivedStateOf to strictly observe the list changes
    val currentDestination by remember {
        derivedStateOf { backStack.lastOrNull() ?: AshBikeDestination.Home }
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // ---Permissions Logic ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val isGranted = perms.values.all { it }
        viewModel.onEvent(MainUiEvent.OnPermissionResult(isGranted))
    }

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

    // --- System Back Handler ---
    // If we have history (> 1 item), pop the stack. Otherwise, let Android close the app.
    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    // --- UI Rendering ---
    if (state.hasLocationPermission) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val title = when (currentDestination) {
                            AshBikeDestination.Home -> "AshBike Dashboard"
                            AshBikeDestination.Trips -> "Ride History"
                            is AshBikeDestination.Settings -> "Settings"
                            is AshBikeDestination.RideDetail -> "Detailed Ride"

                        }
                        Text(title)
                    }
                )
            },
            bottomBar = {
                AshBikeBottomBar(
                    currentDestination = currentDestination,
                    showSettingsBadge = state.showSettingsBadge,
                    onNavigate = { newDestination ->
                        // --- Bottom Navigation Logic ---
                        // 1. If we are already on this tab, do nothing (or scroll to top)
                        if (currentDestination != newDestination) {
                            // 2. Clear the stack and set the new tab as the only item.
                            //    (This mimics standard "Top Level Destination" behavior)
                            backStack.clear()
                            backStack.add(newDestination)
                        }
                    },
                    unsyncedRidesCount = unsyncedRidesCount,
                )
            }
        ) { innerPadding ->

            // 2. NAV DISPLAY (The Content Renderer)
            // It takes the list and a provider lambda to map keys -> content.
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.padding(innerPadding),
                onBack = { backStack.removeLastOrNull() }, // Default back action
                entryProvider = { key ->
                    // ✅ DELEGATE: Call the provider function we wrote earlier.
                    // This keeps your MainScreen clean and logic-free.
                    ashBikeNavEntryProvider(
                        key = key,
                        navigateTo = { dest -> navigateTo(dest) },
                        homeViewModel = homeViewModel,
                    )
                }
            )
        }
    } else {
        // --- Permission Denied State ---
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text("Location Permission Required", modifier = Modifier.padding(16.dp))
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