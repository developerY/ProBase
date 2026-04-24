package com.zoewave.probase.kocolor.mobile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.features.camera.ui.CameraUIRoute
import com.zoewave.probase.kocolor.features.analyzer.ui.AnalyzerEvent
import com.zoewave.probase.kocolor.features.analyzer.ui.AnalyzerUiRoute
import com.zoewave.probase.kocolor.features.analyzer.ui.AnalyzerViewModel
import com.zoewave.probase.kocolor.features.color.ui.ColorUiRoute
import com.zoewave.probase.kocolor.features.suggestions.ui.SuggestionsUiRoute
import com.zoewave.probase.kocolor.mobile.features.home.ui.HomeUiRoute
import com.zoewave.probase.kocolor.mobile.features.settings.ui.components.SettingsUiRoute
import com.zoewave.probase.kocolor.model.KoColorRoute

fun koColorNavEntryProvider(
    route: KoColorRoute,
    windowSizeClass: WindowSizeClass,
    onNavigateTo: (KoColorRoute) -> Unit,
    onBack: () -> Unit
): NavEntry<KoColorRoute> {
    return when (route) {
        is KoColorRoute.Home -> NavEntry(route) {
            HomeUiRoute(
                onNavigateTo = onNavigateTo,
                windowSizeClass = windowSizeClass
            )
        }
        is KoColorRoute.Analyzer -> NavEntry(route) {
            AnalyzerUiRoute(
                onBack = onBack,
                onNavigateToCamera = { onNavigateTo(KoColorRoute.Camera("analyzer")) },
                onAnalysisSaved = onBack
            )
        }
        is KoColorRoute.Color -> NavEntry(route) {
            ColorUiRoute(
                windowSizeClass = windowSizeClass
            )
        }
        is KoColorRoute.Suggestions -> NavEntry(route) {
            SuggestionsUiRoute(
                onBack = onBack
            )
        }
        is KoColorRoute.Settings -> NavEntry(route) {
            SettingsUiRoute(
                onBack = onBack
            )
        }
        is KoColorRoute.Camera -> NavEntry(route) {
            val analyzerViewModel: AnalyzerViewModel = hiltViewModel()
            CameraUIRoute(
                navTo = { result ->
                    if (result.startsWith("result_ok:")) {
                        val uri = result.substringAfter("result_ok:")
                        analyzerViewModel.onEvent(AnalyzerEvent.OnPhotoCaptured(uri))
                        onBack()
                    } else {
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
