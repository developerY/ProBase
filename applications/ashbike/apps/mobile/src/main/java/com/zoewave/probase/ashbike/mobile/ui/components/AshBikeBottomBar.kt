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
    showSettingsBadge: Boolean,
    onNavigate: (AshBikeDestination) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentDestination == AshBikeDestination.Home,
            onClick = { onNavigate(AshBikeDestination.Home) },
            icon = { Icon(Icons.Default.Home, "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentDestination == AshBikeDestination.RideHistory,
            onClick = { onNavigate(AshBikeDestination.RideHistory) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, "History") },
            label = { Text("History") }
        )
        NavigationBarItem(
            selected = currentDestination == AshBikeDestination.Settings,
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