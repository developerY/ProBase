package com.zoewave.probase.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.features.nav3.ui.inventory.FeatureInventory

@Composable
fun MainScaffold() {
    val backStack = rememberNavBackStack(FeatureInventory.List)
    
    val navigateTo: (NavKey) -> Unit = { dest ->
        // For top-level tabs, we might want to clear the backstack or handle it differently
        // but for now, we'll just add it. 
        // A better approach for tabs in Nav3 is often to have multiple backstacks, 
        // but let's keep it simple first.
        if (backStack.lastOrNull() != dest) {
            backStack.add(dest)
        }
    }

    val navigateBack: () -> Unit = { backStack.removeLastOrNull() }

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentKey = backStack.lastOrNull() ?: FeatureInventory.List
                
                NavigationBarItem(
                    selected = currentKey is FeatureInventory.List,
                    onClick = { 
                        if (currentKey !is FeatureInventory.List) {
                            backStack.clear()
                            backStack.add(FeatureInventory.List)
                        } 
                    },
                    icon = { Icon(Icons.Default.List, contentDescription = "Main") },
                    label = { Text("Main") }
                )
                NavigationBarItem(
                    selected = currentKey is FeatureInventory.GlassXR,
                    onClick = { 
                        if (currentKey !is FeatureInventory.GlassXR) {
                            backStack.clear()
                            backStack.add(FeatureInventory.GlassXR)
                        } 
                    },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Examples") },
                    label = { Text("Examples") }
                )
                NavigationBarItem(
                    selected = currentKey is FeatureInventory.Settings,
                    onClick = { 
                        if (currentKey !is FeatureInventory.Settings) {
                            backStack.clear()
                            backStack.add(FeatureInventory.Settings)
                        } 
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = { key ->
                featureInventoryEntryProvider(
                    key = key,
                    navigateTo = navigateTo,
                    navigateBack = navigateBack
                )
            }
        )
    }
}
