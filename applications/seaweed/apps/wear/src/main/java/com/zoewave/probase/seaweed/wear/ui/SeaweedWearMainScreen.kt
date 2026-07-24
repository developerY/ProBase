package com.zoewave.probase.seaweed.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation3.SwipeDismissableSceneStrategy
import androidx.compose.ui.tooling.preview.Preview
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
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
            NavDisplay(
                backStack = backStack,
                sceneStrategies = listOf(SwipeDismissableSceneStrategy()),
                onBack = { navigateBack() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
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

@Preview(device = "id:wearos_small_round", showBackground = true)
@Composable
private fun SeaweedWearMainScreenPreview() {
    SeaweedWearMainScreen()
}
