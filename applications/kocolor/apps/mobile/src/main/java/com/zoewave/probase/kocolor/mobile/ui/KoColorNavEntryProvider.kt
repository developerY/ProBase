package com.zoewave.probase.kocolor.mobile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.features.camera.ui.CameraUIRoute
import com.zoewave.probase.features.readers.barcode.ui.BarcodeScannerScreen
import com.zoewave.probase.features.readers.qrscanner.ui.QRCodeScannerScreen
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorScreen
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorViewModel
import com.zoewave.probase.kocolor.features.analyzer.ui.AnalyzerUiRoute
import com.zoewave.probase.kocolor.features.color.ui.ColorDetailScreen
import com.zoewave.probase.kocolor.features.color.ui.ColorUiRoute
import com.zoewave.probase.kocolor.features.color.ui.ColorViewModel
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticCategoryCoverScreen
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticDetailScreen
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticEditScreen
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticsUiRoute
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticsViewModel
import com.zoewave.probase.kocolor.features.cosmetics.ui.StitchProductBuilder
import com.zoewave.probase.kocolor.features.cosmetics.ui.VanityLandingScreen
import com.zoewave.probase.kocolor.features.inventory.ui.ColorVerificationRoute
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeCategoryCoverScreen
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeDetailScreen
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeEditScreen
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeLandingScreen
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeRoute
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeViewModel
import com.zoewave.probase.kocolor.features.routines.ui.RoutineDetailScreen
import com.zoewave.probase.kocolor.features.routines.ui.RoutineEditorScreen
import com.zoewave.probase.kocolor.features.routines.ui.RoutinesScreen
import com.zoewave.probase.kocolor.features.routines.ui.RoutinesViewModel
import com.zoewave.probase.kocolor.features.suggestions.ui.SuggestionsUiRoute
import com.zoewave.probase.kocolor.mobile.core.ui.health.HealthUiRoute
import com.zoewave.probase.kocolor.mobile.features.home.ui.HomeUiRoute
import com.zoewave.probase.kocolor.mobile.features.home.ui.HomeViewModel
import com.zoewave.probase.kocolor.mobile.features.settings.ui.components.SettingsUiRoute
import com.zoewave.probase.kocolor.model.KoColorRoute

fun koColorNavEntryProvider(
    route: KoColorRoute,
    windowSizeClass: WindowSizeClass,
    onNavigateTo: (KoColorRoute) -> Unit,
    onBack: () -> Unit,
    onFaceCaptured: (String) -> Unit,
    onHairCaptured: (String) -> Unit,
    onShoesCaptured: (String) -> Unit,
    onClothesCaptured: (String) -> Unit,
    onInventoryItemCaptured: (String) -> Unit,
    onCodeScanned: (String) -> Unit
): NavEntry<KoColorRoute> {
    return when (route) {
        is KoColorRoute.Home -> NavEntry(route) {
            val viewModel: HomeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            HomeUiRoute(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Analyzer -> NavEntry(route) {
            AnalyzerUiRoute(
                uiState = Unit,
                onEvent = {},
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Color -> NavEntry(route) {
            val viewModel: ColorViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            ColorUiRoute(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Routines -> NavEntry(route) {
            val viewModel: RoutinesViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            RoutinesScreen(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.RoutineDetail -> NavEntry(route) {
            val viewModel: RoutinesViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            RoutineDetailScreen(
                uiState = route.routineId to state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.RoutineEditor -> NavEntry(route) {
            val viewModel: RoutinesViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            androidx.compose.runtime.LaunchedEffect(route.routineId) {
                viewModel.onEvent(com.zoewave.probase.kocolor.features.routines.ui.RoutinesEvent.StartEditing(route.routineId))
            }
            RoutineEditorScreen(
                uiState = Triple(route.stepId, state, onBack),
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.VanityLanding -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            VanityLandingScreen(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.StyleSimulator -> NavEntry(route) {
            val viewModel: StyleSimulatorViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            StyleSimulatorScreen(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.WardrobeLanding -> NavEntry(route) {
            val viewModel: WardrobeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            WardrobeLandingScreen(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Cosmetics -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CosmeticsUiRoute(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.CosmeticCategoryCover -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CosmeticCategoryCoverScreen(
                uiState = route.categoryName to state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Back -> NavEntry(route) {
            onBack()
            androidx.compose.runtime.LaunchedEffect(Unit) { onBack() }
        }
        is KoColorRoute.Wardrobe -> NavEntry(route) {
            val viewModel: WardrobeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            WardrobeRoute(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.WardrobeCategoryCover -> NavEntry(route) {
            val viewModel: WardrobeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            WardrobeCategoryCoverScreen(
                uiState = route.categoryName to state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.WardrobeDetail -> NavEntry(route) {
            val viewModel: WardrobeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            WardrobeDetailScreen(
                uiState = route.itemId to state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.WardrobeEdit -> NavEntry(route) {
            val viewModel: WardrobeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            WardrobeEditScreen(
                uiState = route.itemId to state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.WardrobeColorVerification -> NavEntry(route) {
            val viewModel: WardrobeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            ColorVerificationRoute(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.ColorDetail -> NavEntry(route) {
            val colorViewModel: ColorViewModel = hiltViewModel()
            val uiState by colorViewModel.uiState.collectAsStateWithLifecycle()
            val analysis = uiState.savedSuggestions.find { it.id == route.suggestionId }
            if (analysis != null) {
                ColorDetailScreen(
                    uiState = analysis,
                    onEvent = colorViewModel::onEvent,
                    navTo = onNavigateTo
                )
            }
        }
        is KoColorRoute.Suggestions -> NavEntry(route) {
            SuggestionsUiRoute(
                uiState = Unit,
                onEvent = {},
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.CosmeticAdd -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            StitchProductBuilder(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.CosmeticDetail -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CosmeticDetailScreen(
                uiState = route.itemId to state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.CosmeticEdit -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CosmeticEditScreen(
                uiState = route.itemId to state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Settings -> NavEntry(route) {
            val viewModel: com.zoewave.probase.kocolor.mobile.features.settings.ui.SettingsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            SettingsUiRoute(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Health -> NavEntry(route) {
            val viewModel: com.zoewave.probase.features.health.core.ui.HealthViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            HealthUiRoute(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo,
                sideEffects = viewModel.sideEffect
            )
        }
        is KoColorRoute.NailLab -> NavEntry(route) {
            com.zoewave.probase.features.ar.naillab.ui.NailLabUiRoute(
                uiState = route.colorHex to route.finish,
                onEvent = {},
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.FaceLab -> NavEntry(route) {
            com.zoewave.probase.features.ar.facelab.ui.FaceLabUiRoute(
                uiState = route.colorHex to route.category,
                onEvent = {},
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.QRScanner -> NavEntry(route) {
            QRCodeScannerScreen(onCodeScanned = { onCodeScanned(it); onBack() })
        }
        is KoColorRoute.BarcodeScanner -> NavEntry(route) {
            BarcodeScannerScreen(onCodeScanned = { onCodeScanned(it); onBack() })
        }
        is KoColorRoute.Camera -> NavEntry(route) {
            com.zoewave.probase.features.camera.ui.CameraUIRoute(
                navTo = { result ->
                    if (result.startsWith("result_ok:")) {
                        val uri = result.substringAfter("result_ok:")
                        when (route.target) {
                            "face" -> onFaceCaptured(uri)
                            "hair" -> onHairCaptured(uri)
                            "shoes" -> onShoesCaptured(uri)
                            "clothes" -> onClothesCaptured(uri)
                            "inventory_item" -> onInventoryItemCaptured(uri)
                        }
                        onBack()
                    } else {
                        onBack()
                    }
                }
            )
        }
    }
}
