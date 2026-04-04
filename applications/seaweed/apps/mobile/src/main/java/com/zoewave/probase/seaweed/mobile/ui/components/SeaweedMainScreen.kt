package com.zoewave.probase.seaweed.mobile.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.seaweed.mobile.ui.navigation.seaweedNavEntryProvider
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination

@Composable
fun SeaweedMainScreen(
    windowSizeClass: WindowSizeClass
) {
    val backStack = remember {
        mutableStateListOf<SeaweedDestination>(SeaweedDestination.Home)
    }

    val currentDestination = backStack.lastOrNull() ?: SeaweedDestination.Home

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            SeaweedBottomBar(
                currentDestination = currentDestination,
                navTo = { selectedDestination ->
                    if (currentDestination != selectedDestination) {
                        backStack.clear()
                        backStack.add(SeaweedDestination.Home)
                        if (selectedDestination != SeaweedDestination.Home) {
                            backStack.add(selectedDestination)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            onBack = { backStack.removeLastOrNull() },
            entryProvider = { key ->
                seaweedNavEntryProvider(
                    key = key,
                    windowSizeClass = windowSizeClass,
                    navigateTo = { dest ->
                        if (dest != backStack.lastOrNull()) {
                            backStack.add(dest)
                        }
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        )
    }
}
