package com.zoewave.probase.photodo.mobile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass.Companion.calculateFromSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.ui.navigation.photoTodoNavEntryProvider
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute

@Composable
fun PhotoDoMainScreen(
    windowSizeClass: WindowSizeClass,
    entryProvider: (PhotoTodoRoute, WindowSizeClass, () -> Unit, (PhotoTodoRoute) -> Unit) -> NavEntry<PhotoTodoRoute> = { key, size, back, to ->
        photoTodoNavEntryProvider(key, size, to, back)
    }
) {
    var backStack by remember { mutableStateOf(listOf<PhotoTodoRoute>(PhotoTodoRoute.Home)) }
    val currentRoute = backStack.lastOrNull() ?: PhotoTodoRoute.Home

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            PhotoTodoBottomBar(
                currentRoute = currentRoute,
                navTo = { selectedRoute ->
                    if (currentRoute != selectedRoute) {
                        // 🚀 ATOMIC UPDATE: Ensure backstack is never transiently empty
                        backStack = if (selectedRoute == PhotoTodoRoute.Home) {
                            listOf(PhotoTodoRoute.Home)
                        } else {
                            listOf(PhotoTodoRoute.Home, selectedRoute)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            onBack = { 
                if (backStack.size > 1) {
                    backStack = backStack.dropLast(1)
                }
            },
            entryProvider = { key ->
                // ✅ DELEGATE: Call the provider function
                entryProvider(
                    key,
                    windowSizeClass,
                    { 
                        if (backStack.size > 1) {
                            backStack = backStack.dropLast(1)
                        }
                    },
                    { dest ->
                        if (dest != backStack.lastOrNull()) {
                            backStack = backStack + dest
                        }
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
fun PhotoDoMainScreenPreview() {
    val configuration = LocalConfiguration.current
    @Suppress("ConfigurationScreenWidthHeight")
    val windowSizeClass = calculateFromSize(DpSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp))
    PhotoDoTheme {
        PhotoDoMainScreen(
            windowSizeClass = windowSizeClass,
            entryProvider = { key, _, _, _ ->
                NavEntry(key) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Mock Screen: ${key::class.simpleName}")
                    }
                }
            }
        )
    }
}
