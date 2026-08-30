package com.zoewave.probase.kocolor.mobile.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.kocolor.mobile.R
import com.zoewave.probase.kocolor.mobile.core.ui.theme.KoColorTheme
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.topLevelRoutes

data class KoColorMainUiState(
    val mainState: MainUiState,
    val windowSizeClass: WindowSizeClass
)

@Composable
fun KoColorMainScreen(
    uiState: KoColorMainUiState,
    modifier: Modifier = Modifier,
    onEvent: (MainEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val darkTheme = when (uiState.mainState.theme) {
        "DARK" -> true
        "SYSTEM" -> isSystemInDarkTheme()
        else -> false // Default to Light theme
    }

    KoColorTheme(
        darkTheme = darkTheme,
        palette = uiState.mainState.palette
    ) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                topLevelRoutes.forEach { route ->
                    val labelId = when (route) {
                        is KoColorRoute.Home -> R.string.applications_kocolor_apps_mobile_nav_home
                        is KoColorRoute.Color -> R.string.applications_kocolor_apps_mobile_nav_collection
                        is KoColorRoute.Settings -> R.string.applications_kocolor_apps_mobile_nav_settings
                        else -> null
                    }
                    item(
                        selected = uiState.mainState.currentTab::class == route::class,
                        onClick = { onEvent(MainEvent.NavigateTo(route)) },
                        icon = { route.icon?.let { Icon(it, contentDescription = labelId?.let { stringResource(it) }) } },
                        label = { labelId?.let { Text(stringResource(it)) } }
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) {
            NavDisplay<KoColorRoute>(
                backStack = uiState.mainState.backStack,
                onBack = { 
                    onEvent(MainEvent.NavigateBack)
                },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = { key ->
                    koColorNavEntryProvider(
                        route = key,
                        windowSizeClass = uiState.windowSizeClass,
                        onNavigateTo = navTo,
                        onBack = {
                            onEvent(MainEvent.NavigateBack)
                        },
                        onFaceCaptured = { onEvent(MainEvent.FaceCaptured(it)) },
                        onHairCaptured = { onEvent(MainEvent.HairCaptured(it)) },
                        onShoesCaptured = { onEvent(MainEvent.ShoesCaptured(it)) },
                        onColorCaptured = { onEvent(MainEvent.ColorCaptured(it)) },
                        onClothesCaptured = { onEvent(MainEvent.ClothesCaptured(it)) },
                        onInventoryItemCaptured = { onEvent(MainEvent.InventoryItemCaptured(it)) },
                        onRitualStepCaptured = { r, s, u -> onEvent(MainEvent.RitualStepCaptured(r, s, u)) },
                        onCodeScanned = { onEvent(MainEvent.CodeScanned(it)) }
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
private fun KoColorMainScreenPreview() {
    KoColorTheme {
        KoColorMainScreen(
            uiState = KoColorMainUiState(
                mainState = MainUiState(),
                windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp))
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
