package com.zoewave.probase.photodo.mobile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.photodo.mobile.core.ui.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.ui.navigation.photoTodoNavEntryProvider
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute

@Composable
fun PhotoDoMainScreen(
    entryProvider: (PhotoTodoRoute, () -> Unit, (PhotoTodoRoute) -> Unit) -> NavEntry<PhotoTodoRoute> = { key, back, to ->
        photoTodoNavEntryProvider(key, to, back)
    }
) {
    val backStack = remember { mutableStateListOf<PhotoTodoRoute>(PhotoTodoRoute.Home) }
    val currentRoute = backStack.lastOrNull() ?: PhotoTodoRoute.Home

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            PhotoTodoBottomBar(
                currentRoute = currentRoute,
                navTo = { selectedRoute ->
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
                entryProvider(
                    key,
                    { backStack.removeLastOrNull() },
                    { dest ->
                        if (dest != backStack.lastOrNull()) {
                            backStack.add(dest)
                        }
                    }
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PhotoDoMainScreenPreview() {
    PhotoDoTheme {
        PhotoDoMainScreen(
            entryProvider = { key, _, _ ->
                NavEntry(key) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Mock Screen: ${key::class.simpleName}")
                    }
                }
            }
        )
    }
}
