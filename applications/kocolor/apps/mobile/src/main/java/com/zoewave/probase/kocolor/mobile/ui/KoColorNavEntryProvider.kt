package com.zoewave.probase.kocolor.mobile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.features.camera.ui.CameraUIRoute
import com.zoewave.probase.kocolor.features.analyzer.ui.AnalyzerUiRoute
import com.zoewave.probase.kocolor.features.color.ui.ColorDetailScreen
import com.zoewave.probase.kocolor.features.color.ui.ColorUiRoute
import com.zoewave.probase.kocolor.features.color.ui.ColorViewModel
import com.zoewave.probase.kocolor.features.suggestions.ui.SuggestionsUiRoute
import com.zoewave.probase.features.ar.naillab.ui.NailLabUiRoute
import com.zoewave.probase.kocolor.mobile.features.home.ui.HomeUiRoute
import com.zoewave.probase.kocolor.mobile.features.settings.ui.components.SettingsUiRoute
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.SavedAnalysis

fun koColorNavEntryProvider(
    route: KoColorRoute,
    windowSizeClass: WindowSizeClass,
    onNavigateTo: (KoColorRoute) -> Unit,
    onBack: () -> Unit,
    onFaceCaptured: (String) -> Unit,
    onHairCaptured: (String) -> Unit,
    onShoesCaptured: (String) -> Unit,
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
                windowSizeClass = windowSizeClass,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.ColorDetail -> NavEntry(route) {
            val colorViewModel: ColorViewModel = hiltViewModel()
            val uiState by colorViewModel.uiState.collectAsStateWithLifecycle()
            val analysis = uiState.savedSuggestions.find { it.id == route.suggestionId }
            if (analysis != null) {
                ColorDetailScreen(
                    analysis = analysis,
                    onBack = onBack
                )
            }
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
        is KoColorRoute.NailLab -> NavEntry(route) {
            NailLabUiRoute(
                colorHex = route.colorHex,
                finish = route.finish,
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
                            "hair" -> onHairCaptured(uri)
                            "shoes" -> onShoesCaptured(uri)
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
