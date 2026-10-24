package com.zoewave.probase.goswift.mobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.goswift.features.main.navigation.GoSwiftDestination
import com.zoewave.probase.goswift.mobile.home.ui.HomeUiRoute
import com.zoewave.probase.goswift.mobile.settings.ui.SettingsUiRoute
import com.zoewave.probase.goswift.mobile.shots.ui.ShotsUiRoute
import com.zoewave.probase.goswift.mobile.healthsync.ui.HealthSyncUiRoute
import com.zoewave.probase.goswift.mobile.ui.routes.InsightsUiRoute

@Suppress("UnusedParameter")
fun goSwiftNavEntryProvider(
    key: GoSwiftDestination,
    navigateTo: (GoSwiftDestination) -> Unit,
    onBack: () -> Unit
): NavEntry<GoSwiftDestination> {
    return when (key) {
        GoSwiftDestination.Home -> NavEntry(key) {
            HomeUiRoute(onNavigateToSync = { navigateTo(GoSwiftDestination.HealthSync) })
        }
        GoSwiftDestination.Schedule -> NavEntry(key) {
            ShotsUiRoute()
        }
        GoSwiftDestination.Insights -> NavEntry(key) {
            InsightsUiRoute()
        }
        GoSwiftDestination.HealthSync -> NavEntry(key) {
            HealthSyncUiRoute(onNavigateBack = onBack)
        }
        GoSwiftDestination.Settings -> NavEntry(key) {
            SettingsUiRoute(onNavigateBack = onBack)
        }
        GoSwiftDestination.Log -> NavEntry(key) {
            StubRoute("Log")
        }
    }
}

@Composable
fun StubRoute(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Stub: $name")
    }
}
