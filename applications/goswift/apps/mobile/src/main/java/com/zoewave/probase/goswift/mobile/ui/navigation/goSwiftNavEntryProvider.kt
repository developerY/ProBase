package com.zoewave.probase.goswift.mobile.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.goswift.features.main.navigation.GoSwiftDestination
import com.zoewave.probase.goswift.mobile.home.ui.HomeUiRoute
import com.zoewave.probase.goswift.mobile.hydration.ui.HydrationUiRoute
import com.zoewave.probase.goswift.mobile.input.ui.InputUiRoute
import com.zoewave.probase.goswift.mobile.settings.ui.SettingsUiRoute
import com.zoewave.probase.goswift.mobile.shots.ui.AddShotUiRoute
import com.zoewave.probase.goswift.mobile.shots.ui.ShotsUiRoute

fun goSwiftNavEntryProvider(
    key: GoSwiftDestination,
    navigateTo: (GoSwiftDestination) -> Unit,
    onBack: () -> Unit
): NavEntry<GoSwiftDestination> {
    return NavEntry(key) {
        when (key) {
            GoSwiftDestination.Home -> {
                HomeUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    navTo = navigateTo
                )
            }
            GoSwiftDestination.Log -> {
                InputUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    navTo = navigateTo
                )
            }
            GoSwiftDestination.Shots -> {
                ShotsUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    navTo = navigateTo
                )
            }
            GoSwiftDestination.AddShot -> {
                AddShotUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    onBack = onBack
                )
            }
            GoSwiftDestination.Settings -> {
                SettingsUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    navTo = navigateTo
                )
            }
            GoSwiftDestination.Hydration -> {
                HydrationUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    navTo = navigateTo
                )
            }
        }
    }
}
