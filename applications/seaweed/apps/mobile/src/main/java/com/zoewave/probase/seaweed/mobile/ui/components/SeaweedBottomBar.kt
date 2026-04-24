package com.zoewave.probase.seaweed.mobile.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import com.zoewave.probase.seaweed.model.navigation.topLevelDestinations

@Composable
fun SeaweedBottomBar(
    currentDestination: SeaweedDestination,
    navTo: (SeaweedDestination) -> Unit
) {
    NavigationBar {
        topLevelDestinations.forEach { destination ->
            val titleRes = destination.titleRes
            val title = destination.title
            val label = when {
                titleRes != null -> stringResource(titleRes)
                title != null -> title
                else -> ""
            }
            NavigationBarItem(
                selected = when (destination) {
                    is SeaweedDestination.Home -> currentDestination is SeaweedDestination.Home || 
                                                 currentDestination is SeaweedDestination.Budget || 
                                                 currentDestination is SeaweedDestination.CategoryGrid
                    is SeaweedDestination.Transactions -> currentDestination is SeaweedDestination.Transactions || 
                                                         currentDestination is SeaweedDestination.Analytics || 
                                                         currentDestination is SeaweedDestination.Bills
                    is SeaweedDestination.Settings -> currentDestination is SeaweedDestination.Settings
                    else -> false
                },
                onClick = { navTo(destination) },
                icon = { Icon(imageVector = destination.icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SeaweedBottomBarPreview() {
    SeaweedBottomBar(
        currentDestination = SeaweedDestination.Home,
        navTo = {}
    )
}
