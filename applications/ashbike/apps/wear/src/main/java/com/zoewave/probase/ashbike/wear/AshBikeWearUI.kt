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
    // 1. The Gatekeeper: Check for required hardware permissions
    // Note: We only ask for foreground permissions initially.
    // Background permissions (if needed) must be requested in a separate step later.
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BODY_SENSORS
        )
    )

    // 2. The Permission Wall
    if (!permissionState.allPermissionsGranted) {
        // If permissions are missing, show a dedicated screen to ask for them.
        // This completely blocks access to the rest of the app until granted.
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
        return // Stop executing the rest of the UI until permissions are granted
    }

    // 3. The Source of Truth
    // This code is ONLY reached once the user has approved the permissions above.
    val backStack = remember {
        mutableStateListOf<AshBikeRoute>(AshBikeRoute.Home)
    }

    // 4. The Wear OS Scaffold & Nav3 Display Engine
    AppScaffold {
        NavDisplay(
            backStack = backStack,
            sceneStrategy = SwipeDismissableSceneStrategy(),
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLast()
                }
            },
            entryProvider = { key ->
                ashBikeWearNavEntryProvider(
                    key = key,
                    navigateTo = { dest -> backStack.add(dest) }
                )
            }
        )
    }
}