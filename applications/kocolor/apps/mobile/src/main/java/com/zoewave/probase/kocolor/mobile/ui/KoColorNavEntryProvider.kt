package com.zoewave.probase.kocolor.mobile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.features.camera.ui.CameraUIRoute
import com.zoewave.probase.kocolor.features.analyzer.ui.AnalyzerUiRoute
import com.zoewave.probase.kocolor.features.color.ui.ColorUiRoute
import com.zoewave.probase.kocolor.features.suggestions.ui.SuggestionsUiRoute
import com.zoewave.probase.kocolor.mobile.features.home.ui.HomeUiRoute
import com.zoewave.probase.kocolor.mobile.features.settings.ui.components.SettingsUiRoute
import com.zoewave.probase.kocolor.model.KoColorRoute

fun koColorNavEntryProvider(
    route: KoColorRoute,
    windowSizeClass: WindowSizeClass,
    onNavigateTo: (KoColorRoute) -> Unit,
    onBack: () -> Unit,
    onFaceCaptured: (String) -> Unit,
    onClothesCaptured: (String) -> Unit
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
                onNavigateToCamera = { target -> onNavigateTo(KoColorRoute.Camera(target)) },
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
            CameraUIRoute(
                navTo = { result ->
                    if (result.startsWith("result_ok:")) {
                        val uri = result.substringAfter("result_ok:")
                        when (route.target) {
                            "face" -> onFaceCaptured(uri)
                            "clothes" -> onClothesCaptured(uri)
                        }
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
