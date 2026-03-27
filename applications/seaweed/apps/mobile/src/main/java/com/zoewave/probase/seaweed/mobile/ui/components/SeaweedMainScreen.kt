package com.zoewave.probase.seaweed.mobile.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.seaweed.features.main.navigation.SeaweedDestination
import com.zoewave.probase.seaweed.mobile.ui.navigation.seaweedNavEntryProvider

@Composable
fun SeaweedMainScreen() {
    val backStack = remember {
        mutableStateListOf<SeaweedDestination>(SeaweedDestination.Home)
    }

    fun navigateTo(destination: SeaweedDestination) {
        backStack.add(destination)
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    BackHandler(enabled = backStack.size > 1) {
        navigateBack()
    }

    NavDisplay(
        backStack = backStack,
        modifier = Modifier,
        onBack = { navigateBack() },
        entryProvider = { key ->
            seaweedNavEntryProvider(
                key = key,
                navigateTo = { dest -> navigateTo(dest) },
                onBack = { navigateBack() }
            )
        }
    )
}
