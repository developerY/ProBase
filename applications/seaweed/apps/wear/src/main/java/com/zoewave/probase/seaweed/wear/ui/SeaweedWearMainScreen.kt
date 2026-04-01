package com.zoewave.probase.seaweed.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.SwipeToDismissBox
import androidx.wear.compose.navigation3.SwipeDismissableSceneStrategy
import com.zoewave.probase.seaweed.features.main.navigation.SeaweedDestination
import com.zoewave.probase.seaweed.wear.ui.navigation.seaweedWearNavEntryProvider
import com.zoewave.probase.seaweed.wear.ui.theme.SeaweedWearTheme

@Composable
fun SeaweedWearMainScreen() {
    val backStack = remember {
        mutableStateListOf<SeaweedDestination>(SeaweedDestination.Home)
    }

    fun navigateTo(destination: SeaweedDestination) {
        if (destination is SeaweedDestination.Home) {
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

    SeaweedWearTheme {
        AppScaffold {
            SwipeToDismissBox(
                onDismissed = { navigateBack() },
                state = rememberSwipeToDismissBoxState(),
                backgroundKey = backStack.getOrNull(backStack.size - 2) ?: SeaweedDestination.Home,
                contentKey = backStack.lastOrNull() ?: SeaweedDestination.Home
            ) { key ->
                NavDisplay(
                    backStack = backStack,
                    sceneStrategy = SwipeDismissableSceneStrategy(),
                    onBack = { navigateBack() },
                    entryProvider = { dest: SeaweedDestination ->
                        seaweedWearNavEntryProvider(
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
