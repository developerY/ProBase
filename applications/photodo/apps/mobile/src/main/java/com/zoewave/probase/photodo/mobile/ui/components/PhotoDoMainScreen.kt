package com.zoewave.probase.photodo.mobile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.photodo.mobile.ui.components.PhotoTodoBottomBar
import com.zoewave.probase.photodo.mobile.ui.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.mobile.ui.navigation.photoTodoNavEntryProvider

@Composable
fun PhotoDoMainScreen() {
    val backStack = remember { mutableStateListOf<PhotoTodoRoute>(PhotoTodoRoute.Home) }
    val currentRoute = backStack.lastOrNull() ?: PhotoTodoRoute.Home

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            PhotoTodoBottomBar(
                currentRoute = currentRoute,
                onNavigateTo = { selectedRoute ->
                    if (currentRoute != selectedRoute) {
                        backStack.clear()
                        backStack.add(PhotoTodoRoute.Home)
                        if (selectedRoute != PhotoTodoRoute.Home) {
                            backStack.add(selectedRoute)
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
                // ✅ DELEGATE: Call the provider function
                photoTodoNavEntryProvider(
                    key = key,
                    navigateTo = { dest ->
                        if (dest != backStack.lastOrNull()) {
                            backStack.add(dest)
                        }
                    }
                )
            }
        )
    }
}