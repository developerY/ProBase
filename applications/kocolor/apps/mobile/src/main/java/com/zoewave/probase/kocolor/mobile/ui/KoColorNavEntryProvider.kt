package com.zoewave.probase.kocolor.mobile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.features.ar.facelab.ui.FaceLabUiRoute
import com.zoewave.probase.features.ar.naillab.ui.NailLabUiRoute
import com.zoewave.probase.features.camera.ui.CameraUIRoute
import com.zoewave.probase.features.readers.barcode.ui.BarcodeScannerScreen
import com.zoewave.probase.features.readers.qrscanner.ui.QRCodeScannerScreen
import com.zoewave.probase.kocolor.features.analyzer.ui.AnalyzerUiRoute
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorScreen
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorViewModel
import com.zoewave.probase.kocolor.features.color.ui.ColorDetailScreen
import com.zoewave.probase.kocolor.features.color.ui.ColorUiRoute
import com.zoewave.probase.kocolor.features.color.ui.ColorViewModel
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticDetailScreen
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticEditScreen
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticsUiRoute
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticCategoryCoverScreen
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticsViewModel
import com.zoewave.probase.kocolor.features.cosmetics.ui.VanityLandingScreen
import com.zoewave.probase.kocolor.features.cosmetics.ui.StitchProductBuilder
import com.zoewave.probase.kocolor.features.inventory.ui.*
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeRoute
import com.zoewave.probase.kocolor.features.routines.ui.RoutinesScreen
import com.zoewave.probase.kocolor.features.routines.ui.RoutinesViewModel
import com.zoewave.probase.kocolor.features.routines.ui.RoutineEditorScreen
import com.zoewave.probase.kocolor.features.routines.ui.RoutineDetailScreen
import com.zoewave.probase.kocolor.features.suggestions.ui.SuggestionsUiRoute
import com.zoewave.probase.kocolor.mobile.features.health.HealthUiRoute
import com.zoewave.probase.kocolor.mobile.features.home.ui.HomeUiRoute
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
            HomeUiRoute(
                uiState = windowSizeClass,
                onEvent = {},
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
            ColorUiRoute(
                uiState = windowSizeClass,
                onEvent = {},
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
                routineId = route.routineId,
                uiState = state,
                onEvent = viewModel::onEvent,
                onBack = onBack,
                onEdit = { id -> onNavigateTo(KoColorRoute.RoutineEditor(id)) }
            )
        }
        is KoColorRoute.RoutineEditor -> NavEntry(route) {
            val viewModel: RoutinesViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            androidx.compose.runtime.LaunchedEffect(route.routineId) {
                viewModel.onEvent(com.zoewave.probase.kocolor.features.routines.ui.RoutinesEvent.StartEditing(route.routineId))
            }
            RoutineEditorScreen(
                uiState = state,
                onEvent = viewModel::onEvent,
                onBack = onBack
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
            CosmeticsUiRoute(
                initialFilter = route.filter,
                onCodeScanned = onCodeScanned,
                onEvent = {},
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.CosmeticCategoryCover -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CosmeticCategoryCoverScreen(
                categoryName = route.categoryName,
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Back -> NavEntry(route) {
            // This route should be handled by onNavigateTo before reaching here, 
            // but we provide a placeholder just in case.
            onBack()
            androidx.compose.runtime.LaunchedEffect(Unit) { onBack() }
        }
        is KoColorRoute.Wardrobe -> NavEntry(route) {
            WardrobeRoute(
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.WardrobeCategoryCover -> NavEntry(route) {
            val viewModel: WardrobeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            WardrobeCategoryCoverScreen(
                categoryName = route.categoryName,
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.WardrobeDetail -> NavEntry(route) {
            val viewModel: WardrobeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            WardrobeDetailScreen(
                itemId = route.itemId,
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.WardrobeEdit -> NavEntry(route) {
            val viewModel: WardrobeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            WardrobeEditScreen(
                itemId = route.itemId,
                uiState = state,
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
                itemId = route.itemId,
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.CosmeticEdit -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CosmeticEditScreen(
                itemId = route.itemId,
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Settings -> NavEntry(route) {
            SettingsUiRoute(
                uiState = Unit,
                onEvent = {},
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Health -> NavEntry(route) {
            HealthUiRoute(
                onBack = onBack
            )
        }
        is KoColorRoute.NailLab -> NavEntry(route) {
            NailLabUiRoute(
                uiState = route.colorHex to route.finish,
                onEvent = {},
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.FaceLab -> NavEntry(route) {
            FaceLabUiRoute(
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
            CameraUIRoute(
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
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
