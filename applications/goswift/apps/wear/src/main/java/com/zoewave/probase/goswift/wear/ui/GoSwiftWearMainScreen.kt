package com.zoewave.probase.goswift.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.SwipeToDismissBox
import androidx.wear.compose.navigation3.SwipeDismissableSceneStrategy
import com.zoewave.probase.goswift.features.main.navigation.GoSwiftDestination
import com.zoewave.probase.goswift.wear.ui.navigation.goSwiftWearNavEntryProvider
import com.zoewave.probase.goswift.wear.ui.theme.GoSwiftWearTheme

@Composable
fun GoSwiftWearMainScreen() {
    val backStack = remember {
        mutableStateListOf<GoSwiftDestination>(GoSwiftDestination.Home)
    }

    fun navigateTo(destination: GoSwiftDestination) {
        if (destination == GoSwiftDestination.Home) {
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

    GoSwiftWearTheme {
        AppScaffold {
            SwipeToDismissBox(
                onDismissed = { navigateBack() },
                state = rememberSwipeToDismissBoxState(),
                backgroundKey = backStack.getOrNull(backStack.size - 2) ?: GoSwiftDestination.Home,
                contentKey = backStack.lastOrNull() ?: GoSwiftDestination.Home
            ) { key ->
                NavDisplay(
                    backStack = backStack,
                    sceneStrategy = SwipeDismissableSceneStrategy(),
                    onBack = { navigateBack() },
                    entryProvider = { dest: GoSwiftDestination ->
                        goSwiftWearNavEntryProvider(
                            key = dest,
                            navigateTo = ::navigateTo,
                            onBack = ::navigateBack
                        )
                    }
                )
            }
        }
    }
}
