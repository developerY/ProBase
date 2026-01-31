package com.zoewave.probase.ashbike.mobile.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.zoewave.probase.ashbike.features.main.ui.AshBikeSharedScreen
import com.zoewave.probase.ashbike.features.main.navigation.AshBikeDestination
import kotlinx.serialization.json.Json
import androidx.compose.runtime.saveable.Saver // ✅ Import Saver

// Helper data class for the Tab UI
private data class NavTab(
    val destination: AshBikeDestination,
    val icon: ImageVector,
    val label: String,
    val badgeCount: Int = 0
)

@Composable
fun AshBikeMainScreen() {
    // ✅ FIX: Define a Saver that uses JSON to store the state
    val destinationSaver = Saver<AshBikeDestination, String>(
        save = { Json.encodeToString(it) },
        restore = { Json.decodeFromString(it) }
    )

    // ✅ FIX: Pass 'stateSaver = destinationSaver' to rememberSaveable
    var currentDestination by rememberSaveable(stateSaver = destinationSaver) {
        mutableStateOf(AshBikeDestination.Home)
    }

    // ✅ Nav3: Handle Back Press manually (State Logic)
    // If not at Home, go to Home. If at Home, standard system back (exit).
    BackHandler(enabled = currentDestination != AshBikeDestination.Home) {
        currentDestination = AshBikeDestination.Home
    }

    val tabs = listOf(
        NavTab(AshBikeDestination.Home, Icons.Default.Home, "Home"),
        NavTab(AshBikeDestination.RideHistory, Icons.Default.List, "History", badgeCount = 3),
        NavTab(AshBikeDestination.Settings, Icons.Default.Settings, "Settings")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val isSelected = currentDestination == tab.destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = tab.destination },
                        icon = {
                            if (tab.badgeCount > 0) {
                                BadgedBox(badge = { Badge { Text(tab.badgeCount.toString()) } }) {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.label)
                            }
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // ✅ Nav3: Type-Safe Switching using 'when'
            // No Strings, No Graphs, Just Kotlin.
            when (currentDestination) {
                is AshBikeDestination.Home -> AshBikeSharedScreen("Home Dashboard")
                is AshBikeDestination.RideHistory -> AshBikeSharedScreen("Ride History")
                is AshBikeDestination.Settings -> AshBikeSharedScreen("Settings")
            }
        }
    }
}