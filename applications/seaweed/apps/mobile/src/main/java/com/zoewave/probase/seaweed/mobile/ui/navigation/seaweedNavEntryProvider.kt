package com.zoewave.probase.seaweed.mobile.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.features.camera.ui.CameraUIRoute
import com.zoewave.probase.seaweed.features.receiptcapture.ui.SmartReceiptUiRoute
import com.zoewave.probase.seaweed.mobile.budget.ui.BudgetUiRoute
import com.zoewave.probase.seaweed.mobile.home.ui.CategoryGridRoute
import com.zoewave.probase.seaweed.mobile.home.ui.HomeUiRoute
import com.zoewave.probase.seaweed.mobile.settings.ui.SettingsUiRoute
import com.zoewave.probase.seaweed.mobile.transaction.ui.AddTransactionUiEvent
import com.zoewave.probase.seaweed.mobile.transaction.ui.AddTransactionUiRoute
import com.zoewave.probase.seaweed.mobile.transaction.ui.AddTransactionViewModel
import com.zoewave.probase.seaweed.mobile.transaction.ui.AiDebugScreen
import com.zoewave.probase.seaweed.mobile.transaction.ui.AnalyticsUiRoute
import com.zoewave.probase.seaweed.mobile.transaction.ui.TransactionsUiRoute
import com.zoewave.probase.seaweed.mobile.ui.components.AdaptiveSeaweedScreen
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination

fun seaweedNavEntryProvider(
    key: SeaweedDestination,
    windowSizeClass: WindowSizeClass,
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
            SeaweedDestination.CategoryGrid -> {
                CategoryGridRoute(
                    modifier = Modifier.fillMaxSize(),
                    navTo = navigateTo,
                    onBack = onBack
                )
            }
            SeaweedDestination.Bills -> {
                // Bills are now part of Transactions, but we handle the direct destination for backward compatibility or deep links
                TransactionsUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    initialTab = com.zoewave.probase.seaweed.model.navigation.TransactionTab.CYCLIC,
                    navTo = navigateTo
                )
            }
            SeaweedDestination.Budget -> {
                BudgetUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    onBack = onBack
                )
            }
            is SeaweedDestination.Transactions -> {
                val isExpanded = windowSizeClass.widthSizeClass != androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Compact
                if (isExpanded) {
                    AdaptiveSeaweedScreen(
                        windowSizeClass = windowSizeClass,
                        navTo = navigateTo,
                        initialCategory = key.category,
                        initialTab = key.initialTab,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    TransactionsUiRoute(
                        modifier = Modifier.fillMaxSize(),
                        initialCategory = key.category,
                        initialTransactionId = key.transactionId,
                        initialTab = key.initialTab,
                        navTo = navigateTo
                    )
                }
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
            SeaweedDestination.Analytics -> {
                AnalyticsUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    onBack = onBack
                )
            }
            SeaweedDestination.Camera -> {
                val viewModel: AddTransactionViewModel = hiltViewModel()
                CameraUIRoute(
                    navTo = { routeString ->
                        if (routeString.startsWith("result_ok:")) {
                            val uriString = routeString.removePrefix("result_ok:")
                            viewModel.onEvent(AddTransactionUiEvent.ReceiptAttached(uriString))
                            onBack()
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is SeaweedDestination.SmartReceipt -> {
                SmartReceiptUiRoute(
                    initialPhotoUri = key.photoUri,
                    onComplete = onBack,
                    onDismiss = onBack
                )
            }
            is SeaweedDestination.SmartReceiptDebug -> {
                AiDebugScreen(
                    rawResponse = key.rawResponse,
                    logs = key.logs,
                    engineUsed = key.engineUsed,
                    onBack = onBack
                )
            }
        }
    }
}
