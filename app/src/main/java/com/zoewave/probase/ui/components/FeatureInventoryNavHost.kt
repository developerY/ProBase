package com.zoewave.probase.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.features.nav3.ui.inventory.FeatureInventory


@Composable
fun FeatureInventoryNavHost(
    modifier: Modifier = Modifier
) {
    // 1. Create the BackStack (No generics needed based on your error log)
    val backStack = rememberNavBackStack(FeatureInventory.List)

    // 2. Helper for navigation
    val navigateTo: (NavKey) -> Unit = { dest ->
        backStack.add(dest)
    }

    // Define the Back Action
    val navigateBack: () -> Unit = { backStack.removeLastOrNull() }

    // 3. Back Handler
    // NavBackStack usually implements List/Collection, so checking size works
    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    // 4. NavDisplay
    NavDisplay(
        // FIX: Parameter name is 'backStack' (camelCase), not 'backstack'
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = { key ->
            // ✅ DELEGATE: Call the provider function
            featureInventoryEntryProvider(
                key = key,
                navigateTo = navigateTo,
                navigateBack = navigateBack
            )
        }
    )
}