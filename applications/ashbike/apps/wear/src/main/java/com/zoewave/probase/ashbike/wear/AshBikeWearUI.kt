package com.zoewave.probase.ashbike.wear

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import androidx.wear.compose.navigation3.SwipeDismissableSceneStrategy
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.zoewave.probase.ashbike.wear.ui.navigation.AshBikeRoute
import com.zoewave.probase.ashbike.wear.ui.navigation.ashBikeWearNavEntryProvider

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AshBikeWearUI() {
    // 1. The Gatekeeper: Define required hardware sensors
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BODY_SENSORS
        )
    )

    // 2. The Permission Wall
    // If permissions are missing, show a dedicated screen to ask for them.
    // This completely blocks access to the Pager and ViewModels until granted.
    if (!permissionState.allPermissionsGranted) {
        AppScaffold {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                    Text("Grant Sensor Access")
                }
            }
        }
        return // Halt UI composition here until the user taps 'Allow'
    }

    // 3. The Source of Truth for Nav3
    // We explicitly set the starting destination to your new Pager
    val backStack = remember {
        mutableStateListOf<AshBikeRoute>(AshBikeRoute.Core.HomePager)
    }

    // 4. The Main App Scaffold & Navigation Engine
    AppScaffold {
        NavDisplay(
            backStack = backStack,
            // Enables native Wear OS swipe-to-dismiss behavior for drill-down screens
            sceneStrategy = SwipeDismissableSceneStrategy(),
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLast()
                }
            },
            entryProvider = { key ->
                ashBikeWearNavEntryProvider(
                    key = key,
                    // When the Pager requests a drill-down (like RideDetail),
                    // add it to the top of the Navigation 3 backStack
                    navigateTo = { dest -> backStack.add(dest) }
                )
            }
        )
    }
}