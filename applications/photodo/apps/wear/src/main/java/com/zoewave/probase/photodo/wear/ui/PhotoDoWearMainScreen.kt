package com.zoewave.probase.photodo.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation3.SwipeDismissableSceneStrategy
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.wear.ui.navigation.photoDoWearNavEntryProvider
import com.zoewave.probase.photodo.wear.ui.theme.PhotoDoWearTheme

@Composable
fun PhotoDoWearMainScreen() {
    val backStack = remember {
        mutableStateListOf<PhotoTodoRoute>(PhotoTodoRoute.Home)
    }

    fun navigateTo(destination: PhotoTodoRoute) {
        if (destination is PhotoTodoRoute.Home) {
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

    PhotoDoWearTheme {
        AppScaffold {
            NavDisplay(
                backStack = backStack,
                sceneStrategies = listOf(SwipeDismissableSceneStrategy()),
                onBack = { navigateBack() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = { dest: PhotoTodoRoute ->
                    photoDoWearNavEntryProvider(
                        key = dest,
                        navigateTo = ::navigateTo,
                        onBack = ::navigateBack
                    )
                }
            )
        }
    }
}
