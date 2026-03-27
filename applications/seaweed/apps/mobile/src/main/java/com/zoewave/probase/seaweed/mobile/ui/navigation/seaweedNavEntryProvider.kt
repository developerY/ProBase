package com.zoewave.probase.seaweed.mobile.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.seaweed.features.main.navigation.SeaweedDestination
import com.zoewave.probase.seaweed.mobile.transaction.ui.TransactionsUiRoute
import com.zoewave.probase.seaweed.mobile.transaction.ui.AddTransactionUiRoute
import com.zoewave.probase.seaweed.mobile.home.ui.HomeUiRoute
import com.zoewave.probase.seaweed.mobile.settings.ui.SettingsUiRoute

fun seaweedNavEntryProvider(
    key: SeaweedDestination,
    navigateTo: (SeaweedDestination) -> Unit,
    onBack: () -> Unit
): NavEntry<SeaweedDestination> {
    return NavEntry(key) {
        when (key) {
            SeaweedDestination.Home -> {
                HomeUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    navTo = navigateTo
                )
            }
            SeaweedDestination.Transactions -> {
                TransactionsUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    navTo = navigateTo
                )
            }
            SeaweedDestination.AddTransaction -> {
                AddTransactionUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    navTo = navigateTo,
                    onBack = onBack
                )
            }
            SeaweedDestination.Settings -> {
                SettingsUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    navTo = navigateTo
                )
            }
        }
    }
}
