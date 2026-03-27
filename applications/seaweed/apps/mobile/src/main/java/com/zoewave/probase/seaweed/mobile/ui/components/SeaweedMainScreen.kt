package com.zoewave.probase.seaweed.mobile.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
        if (destination == SeaweedDestination.Home || destination == SeaweedDestination.Transactions || destination == SeaweedDestination.Settings) {
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

    Scaffold(
        bottomBar = {
            SeaweedBottomBar(
                currentDestination = currentDestination,
                onNavigate = { navigateTo(it) }
            )
        }
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(padding),
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

@Composable
fun SeaweedBottomBar(
    currentDestination: SeaweedDestination,
    onNavigate: (SeaweedDestination) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentDestination == SeaweedDestination.Home,
            onClick = { onNavigate(SeaweedDestination.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentDestination == SeaweedDestination.Transactions,
            onClick = { onNavigate(SeaweedDestination.Transactions) },
            icon = { Icon(Icons.Default.List, contentDescription = "Transactions") },
            label = { Text("Transactions") }
        )
        NavigationBarItem(
            selected = currentDestination == SeaweedDestination.Settings,
            onClick = { onNavigate(SeaweedDestination.Settings) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}
