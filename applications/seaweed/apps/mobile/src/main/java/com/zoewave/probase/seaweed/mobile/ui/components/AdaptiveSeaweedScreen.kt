package com.zoewave.probase.seaweed.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.seaweed.mobile.home.ui.CategoryGridRoute
import com.zoewave.probase.seaweed.mobile.home.ui.HomeViewModel
import com.zoewave.probase.seaweed.mobile.transaction.ui.TransactionsUiRoute
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AdaptiveSeaweedScreen(
    windowSizeClass: WindowSizeClass,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier,
    initialCategory: String? = null
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
            val homeViewModel = hiltViewModel<HomeViewModel>()
            val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
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
                navTo = navTo
            )
        },
        modifier = modifier.fillMaxSize()
    )
}
