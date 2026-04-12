package com.zoewave.probase.seaweed.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass.Companion.calculateFromSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.zoewave.probase.seaweed.mobile.home.ui.CategoryGridRoute
import com.zoewave.probase.seaweed.mobile.transaction.ui.TransactionsUiRoute
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import com.zoewave.probase.seaweed.model.navigation.TransactionTab

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AdaptiveSeaweedScreen(
    windowSizeClass: WindowSizeClass,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier,
    initialCategory: String? = null,
    initialTab: TransactionTab = TransactionTab.RECENT
) {
    val isExpanded = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    
    if (!isExpanded) {
        return
    }

    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            val listBackground = MaterialTheme.colorScheme.surface
            CategoryGridRoute(
                modifier = Modifier.fillMaxSize().background(listBackground),
                navTo = navTo,
                onBack = { /* Handle back if needed */ }
            )
        },
        detailPane = {
            val detailBackground = MaterialTheme.colorScheme.surface
            TransactionsUiRoute(
                modifier = Modifier.fillMaxSize().background(detailBackground),
                initialCategory = initialCategory,
                initialTab = initialTab,
                navTo = navTo
            )
        },
        modifier = modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, widthDp = 1000, heightDp = 800)
@Composable
private fun AdaptiveSeaweedScreenPreview() {
    AdaptiveSeaweedScreen(
        windowSizeClass = calculateFromSize(DpSize(1000.dp, 800.dp)),
        navTo = {}
    )
}
