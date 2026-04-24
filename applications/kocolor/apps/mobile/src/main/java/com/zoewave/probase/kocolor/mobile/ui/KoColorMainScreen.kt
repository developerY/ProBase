package com.zoewave.probase.kocolor.mobile.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.kocolor.mobile.core.ui.theme.KoColorTheme
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.topLevelRoutes
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
fun KoColorMainScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val darkTheme = when (uiState.theme) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    KoColorTheme(
        darkTheme = darkTheme,
        palette = uiState.palette
    ) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                topLevelRoutes.forEach { route ->
                    item(
                        selected = uiState.currentTab::class == route::class,
                        onClick = { viewModel.navigateTo(route) },
                        icon = { route.icon?.let { Icon(it, contentDescription = route.label) } },
                        label = { route.label?.let { Text(it) } }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            NavDisplay<KoColorRoute>(
                backStack = uiState.backStack,
                onBack = { 
                    viewModel.navigateBack()
                },
                entryProvider = { key ->
                    koColorNavEntryProvider(
                        route = key,
                        windowSizeClass = windowSizeClass,
                        onNavigateTo = { dest ->
                            viewModel.navigateTo(dest)
                        },
                        onBack = {
                            viewModel.navigateBack()
                        }
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
            windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp))
        )
    }
}
