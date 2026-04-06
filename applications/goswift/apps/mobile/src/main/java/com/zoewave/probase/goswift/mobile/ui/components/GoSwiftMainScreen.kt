package com.zoewave.probase.goswift.mobile.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.goswift.features.main.navigation.GoSwiftDestination
import com.zoewave.probase.goswift.mobile.ui.navigation.goSwiftNavEntryProvider

@Composable
fun GoSwiftMainScreen() {
    val backStack = remember {
        mutableStateListOf<GoSwiftDestination>(GoSwiftDestination.Home)
    }

    val currentDestination = backStack.lastOrNull() ?: GoSwiftDestination.Home

    fun navigateTo(destination: GoSwiftDestination) {
        if (destination == GoSwiftDestination.Home || destination == GoSwiftDestination.Shots || destination == GoSwiftDestination.Settings) {
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
            GoSwiftBottomBar(
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
                goSwiftNavEntryProvider(
                    key = key,
                    navigateTo = { dest -> navigateTo(dest) },
                    onBack = { navigateBack() }
                )
            }
        )
    }
}

@Composable
fun GoSwiftBottomBar(
    currentDestination: GoSwiftDestination,
    onNavigate: (GoSwiftDestination) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentDestination == GoSwiftDestination.Home,
            onClick = { onNavigate(GoSwiftDestination.Home) },
            icon = { Icon(Icons.Default.Bolt, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentDestination == GoSwiftDestination.Shots,
            onClick = { onNavigate(GoSwiftDestination.Shots) },
            icon = { Icon(Icons.Default.History, contentDescription = "Shots") },
            label = { Text("Shots") }
        )
        NavigationBarItem(
            selected = currentDestination == GoSwiftDestination.Hydration,
            onClick = { onNavigate(GoSwiftDestination.Hydration) },
            icon = { Icon(Icons.Default.WaterDrop, contentDescription = "Hydration") },
            label = { Text("Water") }
        )
        NavigationBarItem(
            selected = currentDestination == GoSwiftDestination.Settings,
            onClick = { onNavigate(GoSwiftDestination.Settings) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}
