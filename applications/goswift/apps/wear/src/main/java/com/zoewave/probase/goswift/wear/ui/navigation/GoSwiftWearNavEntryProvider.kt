package com.zoewave.probase.goswift.wear.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.goswift.features.main.navigation.GoSwiftDestination
import com.zoewave.probase.goswift.wear.home.ui.HomeRoute
import com.zoewave.probase.goswift.wear.input.ui.LogRoute

fun goSwiftWearNavEntryProvider(
    key: GoSwiftDestination,
    navigateTo: (GoSwiftDestination) -> Unit,
    onBack: () -> Unit
): NavEntry<GoSwiftDestination> {
    return NavEntry(key) {
        when (key) {
            GoSwiftDestination.Home -> {
                HomeRoute(
                    modifier = Modifier.fillMaxSize(),
                    navigateTo = navigateTo
                )
            }
            GoSwiftDestination.Log -> {
                LogRoute(
                    modifier = Modifier.fillMaxSize(),
                    onBack = onBack
                )
            }
            else -> {
                // Fallback for sub-routes if needed on Wear
                HomeRoute(
                    modifier = Modifier.fillMaxSize(),
                    navigateTo = navigateTo
                )
            }
        }
    }
}
