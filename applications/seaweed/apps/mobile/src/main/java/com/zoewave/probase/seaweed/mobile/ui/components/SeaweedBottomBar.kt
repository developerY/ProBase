package com.zoewave.probase.seaweed.mobile.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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
                // ✅ Highlight Transactions tab if on CategoryGrid too (they are related)
                selected = currentDestination::class == destination::class || 
                           (destination is SeaweedDestination.Transactions && currentDestination is SeaweedDestination.CategoryGrid),
                onClick = { navTo(destination) },
                icon = { Icon(imageVector = destination.icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}
