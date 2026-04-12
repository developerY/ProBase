package com.zoewave.probase.seaweed.mobile.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass.Companion.calculateFromSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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
                            if (selectedDestination is SeaweedDestination.Transactions) {
                                backStack.add(SeaweedDestination.Transactions())
                            } else {
                                backStack.add(selectedDestination)
                            }
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun SeaweedMainScreenPreview() {
    SeaweedMainScreen(
        windowSizeClass = calculateFromSize(DpSize(400.dp, 800.dp))
    )
}
