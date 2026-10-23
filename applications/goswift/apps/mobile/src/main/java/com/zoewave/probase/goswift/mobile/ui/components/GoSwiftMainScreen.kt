package com.zoewave.probase.goswift.mobile.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
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
        val rootDestinations = listOf(
            GoSwiftDestination.Home,
            GoSwiftDestination.Schedule,
            GoSwiftDestination.Insights,
            GoSwiftDestination.HealthSync,
            GoSwiftDestination.Settings
        )
        if (rootDestinations.contains(destination)) {
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
                selected = currentDestination == GoSwiftDestination.Home,
                onClick = { navigateTo(GoSwiftDestination.Home) },
                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Live Fuel") },
                label = { Text("Home") }
            )
            item(
                selected = currentDestination == GoSwiftDestination.Schedule,
                onClick = { navigateTo(GoSwiftDestination.Schedule) },
                icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Schedule") },
                label = { Text("Schedule") }
            )
            item(
                selected = currentDestination == GoSwiftDestination.Insights,
                onClick = { navigateTo(GoSwiftDestination.Insights) },
                icon = { Icon(imageVector = Icons.Default.Timeline, contentDescription = "Insights") },
                label = { Text("Insights") }
            )
            item(
                selected = currentDestination == GoSwiftDestination.HealthSync,
                onClick = { navigateTo(GoSwiftDestination.HealthSync) },
                icon = { Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync") },
                label = { Text("Sync") }
            )
            item(
                selected = currentDestination == GoSwiftDestination.Settings,
                onClick = { navigateTo(GoSwiftDestination.Settings) },
                icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                label = { Text("Settings") }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { navigateBack() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = { key ->
                goSwiftNavEntryProvider(
                    key = key as GoSwiftDestination,
                    navigateTo = { dest -> navigateTo(dest) },
                    onBack = { navigateBack() }
                )
            }
        )
    }
}
