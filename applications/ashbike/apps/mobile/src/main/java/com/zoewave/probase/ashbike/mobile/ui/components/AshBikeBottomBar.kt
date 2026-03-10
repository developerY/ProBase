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
import androidx.compose.ui.res.stringResource
import com.zoewave.probase.ashbike.features.main.navigation.AshBikeDestination
import com.zoewave.probase.ashbike.mobile.R

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
            icon = { Icon(Icons.Default.Home, stringResource(R.string.applications_ashbike_apps_mobile_bottom_nav_home)) },
            label = { Text(stringResource(R.string.applications_ashbike_apps_mobile_bottom_nav_home)) }
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
                        Icon(Icons.AutoMirrored.Filled.List, stringResource(R.string.applications_ashbike_apps_mobile_bottom_nav_history))
                    }
                } else {
                    Icon(Icons.AutoMirrored.Filled.List, stringResource(R.string.applications_ashbike_apps_mobile_bottom_nav_history))
                }
            },
            label = { Text(stringResource(R.string.applications_ashbike_apps_mobile_bottom_nav_history)) }
        )
        NavigationBarItem(
            selected = currentDestination is AshBikeDestination.Settings,
            onClick = { onNavigate(AshBikeDestination.Settings()) },
            icon = {
                if (showSettingsBadge) {
                    BadgedBox(badge = { Badge() }) { Icon(Icons.Default.Settings, stringResource(R.string.applications_ashbike_apps_mobile_bottom_nav_settings)) }
                } else {
                    Icon(Icons.Default.Settings, stringResource(R.string.applications_ashbike_apps_mobile_bottom_nav_settings))
                }
            },
            label = { Text(stringResource(R.string.applications_ashbike_apps_mobile_bottom_nav_settings)) }
        )
    }
}
