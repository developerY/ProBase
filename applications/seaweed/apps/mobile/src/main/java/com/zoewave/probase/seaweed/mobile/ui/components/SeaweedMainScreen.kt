package com.zoewave.probase.seaweed.mobile.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.seaweed.features.main.navigation.SeaweedDestination
import com.zoewave.probase.seaweed.mobile.ui.navigation.seaweedNavEntryProvider

@Composable
fun SeaweedMainScreen() {
    val backStack = remember {
        mutableStateListOf<SeaweedDestination>(SeaweedDestination.Home)
    }

    val currentDestination = backStack.lastOrNull() ?: SeaweedDestination.Home

    fun navigateTo(destination: SeaweedDestination) {
        if (destination is SeaweedDestination.Home || destination is SeaweedDestination.Transactions || destination is SeaweedDestination.Settings) {
            backStack.clear()
            backStack.add(destination)
        } else {
            backStack.add(destination)
        }
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    BackHandler(enabled = backStack.size > 1) {
        navigateBack()
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                selected = currentDestination is SeaweedDestination.Home || currentDestination is SeaweedDestination.CategoryGrid,
                onClick = { navigateTo(SeaweedDestination.Home) },
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home") }
            )
            item(
                selected = currentDestination is SeaweedDestination.Transactions,
                onClick = { navigateTo(SeaweedDestination.Transactions(category = null)) },
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Transactions") },
                label = { Text("Transactions") }
            )
            item(
                selected = currentDestination is SeaweedDestination.Settings,
                onClick = { navigateTo(SeaweedDestination.Settings) },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                label = { Text("Settings") }
            )
        }
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { navigateBack() },
            entryProvider = { key ->
                seaweedNavEntryProvider(
                    key = key,
                    navigateTo = { dest -> navigateTo(dest) },
                    onBack = { navigateBack() }
                )
            }
        )
    }
}
