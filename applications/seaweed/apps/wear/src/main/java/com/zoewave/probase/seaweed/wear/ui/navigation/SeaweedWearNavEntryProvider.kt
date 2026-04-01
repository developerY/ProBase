package com.zoewave.probase.seaweed.wear.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.seaweed.features.main.navigation.SeaweedDestination
import com.zoewave.probase.seaweed.wear.features.home.HomeRoute
import com.zoewave.probase.seaweed.wear.features.transaction.TransactionListRoute

fun seaweedWearNavEntryProvider(
    key: SeaweedDestination,
    navigateTo: (SeaweedDestination) -> Unit,
    onBack: () -> Unit
): NavEntry<SeaweedDestination> {
    return NavEntry(key) {
        when (key) {
            SeaweedDestination.Home -> {
                HomeRoute(
                    modifier = Modifier.fillMaxSize(),
                    onTransactionsClick = { navigateTo(SeaweedDestination.Transactions(category = null)) }
                )
            }
            is SeaweedDestination.Transactions -> {
                TransactionListRoute(
                    modifier = Modifier.fillMaxSize(),
                    onBack = onBack
                )
            }
            else -> {
                // Not supported on Wear yet
                HomeRoute(
                    modifier = Modifier.fillMaxSize(),
                    onTransactionsClick = { navigateTo(SeaweedDestination.Transactions(category = null)) }
                )
            }
        }
    }
}
