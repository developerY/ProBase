package com.zoewave.probase.ashbike.mobile.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.zoewave.probase.ashbike.features.main.navigation.AshBikeDestination

// Keeping the helper pure and stateless
@Composable
fun AshBikeBottomBar(
    currentDestination: AshBikeDestination,
    onNavigate: (AshBikeDestination) -> Unit,
    unsyncedRidesCount: Int,
    showSettingsBadge: Boolean,
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentDestination is AshBikeDestination.Home,
            onClick = { onNavigate(AshBikeDestination.Home) },
            icon = { Icon(Icons.Default.Home, "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentDestination is AshBikeDestination.Trips,
            onClick = { onNavigate(AshBikeDestination.Trips) },
            icon = {
                if (unsyncedRidesCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge {
                                Text(text = unsyncedRidesCount.toString())
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, "History")
                    }
                } else {
                    Icon(Icons.AutoMirrored.Filled.List, "History")
                }
            },
            label = { Text("History") }
        )
        NavigationBarItem(
            selected = currentDestination is AshBikeDestination.Settings,
            onClick = { onNavigate(AshBikeDestination.Settings) },
            icon = {
                if (showSettingsBadge) {
                    BadgedBox(badge = { Badge() }) { Icon(Icons.Default.Settings, "Settings") }
                } else {
                    Icon(Icons.Default.Settings, "Settings")
                }
            },
            label = { Text("Settings") }
        )
    }
}