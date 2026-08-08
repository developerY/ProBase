package com.zoewave.probase.kocolor.mobile.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.zoewave.probase.features.health.nutrition.ui.shared.MealsUiEvent
import com.zoewave.probase.features.health.nutrition.ui.shared.MealsUiRoute
import com.zoewave.probase.features.health.nutrition.ui.shared.MealsUiState
import com.zoewave.probase.features.health.nutrition.ui.shared.MealsViewModel
import com.zoewave.probase.features.readers.barcode.ui.BarcodeScannerScreen
import com.zoewave.probase.features.readers.qrscanner.ui.QRCodeScannerScreen
import com.zoewave.probase.features.weather.ui.WeatherUiRoute
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorScreen
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorViewModel
import com.zoewave.probase.kocolor.features.analyzer.ui.AnalyzerUiRoute
import com.zoewave.probase.kocolor.features.analyzer.ui.AnalyzerViewModel
import com.zoewave.probase.kocolor.features.boxcapture.ui.BoxCaptureEvent
import com.zoewave.probase.kocolor.features.boxcapture.ui.BoxCaptureUiRoute
import com.zoewave.probase.kocolor.features.boxcapture.ui.BoxCaptureViewModel
import com.zoewave.probase.kocolor.features.clothingcapture.ui.ClothingCaptureEvent
import com.zoewave.probase.kocolor.features.clothingcapture.ui.ClothingCaptureUiRoute
import com.zoewave.probase.kocolor.features.clothingcapture.ui.ClothingCaptureViewModel
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticAnalyticsScreen
import com.zoewave.probase.features.camera.productcapture.ui.DiscoveryStatusScreen
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticCategoryCoverScreen
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticCategoryCoverUiState
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticDetailScreen
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticDetailUiState
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticsEvent
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticsScreen
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticsViewModel
import com.zoewave.probase.kocolor.features.cosmetics.ui.InventoryManagementScreen
import com.zoewave.probase.kocolor.features.cosmetics.ui.StitchProductBuilder
import com.zoewave.probase.kocolor.features.cosmetics.ui.VanityLandingScreen
import com.zoewave.probase.kocolor.features.inventory.ui.ColorVerificationRoute
import com.zoewave.probase.kocolor.features.inventory.ui.ColorVerificationUiState
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeAnalyticsScreen
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeCategoryCoverScreen
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeCategoryCoverUiState
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeDetailScreen
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeDetailUiState
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeEditScreen
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeEditUiState
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeEvent
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeLandingScreen
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeRoute
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeViewModel
import com.zoewave.probase.kocolor.features.routines.ui.RoutineDetailUiRoute
import com.zoewave.probase.kocolor.features.routines.ui.RoutineDetailUiState
import com.zoewave.probase.kocolor.features.routines.ui.RoutineEditorScreen
import com.zoewave.probase.kocolor.features.routines.ui.RoutineEditorUiState
import com.zoewave.probase.kocolor.features.routines.ui.RoutinesUiRoute
import com.zoewave.probase.kocolor.features.routines.ui.RoutinesViewModel
import com.zoewave.probase.kocolor.features.starterpack.ui.StarterPackEvent
import com.zoewave.probase.kocolor.mobile.core.ui.health.HealthUiRoute
import com.zoewave.probase.kocolor.mobile.features.color.ui.ColorDetailScreen
import com.zoewave.probase.kocolor.mobile.features.color.ui.ColorDetailUiState
import com.zoewave.probase.kocolor.mobile.features.color.ui.ColorSearchScreen
import com.zoewave.probase.kocolor.mobile.features.color.ui.ColorSearchViewModel
import com.zoewave.probase.kocolor.mobile.features.color.ui.ColorUiRoute
import com.zoewave.probase.kocolor.mobile.features.color.ui.ColorViewModel
import com.zoewave.probase.kocolor.mobile.features.color.ui.hub.ColorHubScreen
import com.zoewave.probase.kocolor.mobile.features.color.ui.hub.ColorHubViewModel
import com.zoewave.probase.kocolor.mobile.features.home.ui.CollectionDetailScreen
import com.zoewave.probase.kocolor.mobile.features.home.ui.CollectionHubScreen
import com.zoewave.probase.kocolor.mobile.features.home.ui.HomeUiRoute
import com.zoewave.probase.kocolor.mobile.features.home.ui.HomeViewModel
import com.zoewave.probase.kocolor.mobile.features.settings.ui.components.SettingsUiRoute
import com.zoewave.probase.kocolor.features.starterpack.ui.synchub.SyncHubScreen
import com.zoewave.probase.kocolor.features.starterpack.ui.PackPreviewScreen
import com.zoewave.probase.kocolor.features.starterpack.ui.PackPreviewViewModel
import com.zoewave.probase.kocolor.features.starterpack.ui.StarterPackViewModel
import com.zoewave.probase.kocolor.model.KoColorRoute
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalProjectedApi::class)
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
    onRitualStepCaptured: (Long, String, String) -> Unit,
    onColorCaptured: (String) -> Unit,
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
        is KoColorRoute.CollectionHub -> NavEntry(route) {
            val viewModel: HomeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CollectionHubScreen(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.BoxCapture -> NavEntry(route) {
            val viewModel: BoxCaptureViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val discoveryStatus by viewModel.discoveryStatus.collectAsStateWithLifecycle()

            androidx.compose.runtime.LaunchedEffect(route.mode) {
                viewModel.setMode(route.mode)
            }

            BoxCaptureUiRoute(
                uiState = state,
                discoveryStatus = discoveryStatus,
                onEvent = { event ->
                    when (event) {
                        is BoxCaptureEvent.Success -> onBack()
                        BoxCaptureEvent.Dismiss -> onBack()
                        else -> viewModel.onEvent(event)
                    }
                },
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.ClothingCapture -> NavEntry(route) {
            val viewModel: ClothingCaptureViewModel = hiltViewModel()
            val wardrobeViewModel: WardrobeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val discoveryStatus by viewModel.discoveryStatus.collectAsStateWithLifecycle()

            ClothingCaptureUiRoute(
                uiState = state,
                discoveryStatus = discoveryStatus,
                onEvent = { event ->
                    when (event) {
                        is ClothingCaptureEvent.Success -> {
                            wardrobeViewModel.onEvent(WardrobeEvent.AddItem(event.item))
                            onBack()
                        }
                        ClothingCaptureEvent.Dismiss -> onBack()
                        else -> viewModel.onEvent(event)
                    }
                },
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Stitch -> NavEntry(route) {
            val viewModel: com.zoewave.probase.kocolor.features.stitch.ui.StitchViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            
            androidx.compose.runtime.LaunchedEffect(route.id, route.isCopy) {
                viewModel.onEvent(com.zoewave.probase.kocolor.features.stitch.ui.StitchEvent.Initialize(route.id, route.isCopy))
            }
            
            com.zoewave.probase.kocolor.features.stitch.ui.StitchScreen(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.CollectionDetail -> NavEntry(route) {
            val viewModel: HomeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val analysis = state.savedSuggestions.find { it.id == route.collectionId }
            if (analysis != null) {
                CollectionDetailScreen(
                    analysis = analysis,
                    navTo = onNavigateTo
                )
            }
        }
        is KoColorRoute.ColorSearch -> NavEntry(route) {
            val viewModel: ColorSearchViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            ColorSearchScreen(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.ColorHub -> NavEntry(route) {
            val viewModel: ColorHubViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            ColorHubScreen(
                uiState = state,
                onEvent = {},
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Analyzer -> NavEntry(route) {
            val viewModel: AnalyzerViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            AnalyzerUiRoute(
                uiState = state,
                onEvent = viewModel::onEvent,
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
            val context = LocalContext.current
            androidx.compose.runtime.LaunchedEffect(Unit) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.BAKLAVA) {
                    ProjectedContext.isProjectedDeviceConnected(context, this.coroutineContext)
                        .collectLatest { isConnected ->
                            viewModel.updateGlassConnection(isConnected)
                        }
                } else {
                    viewModel.updateGlassConnection(false)
                }
            }
            RoutinesUiRoute(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo,
                sideEffects = viewModel.sideEffect
            )
        }
        is KoColorRoute.RoutineDetail -> NavEntry(route) {
            val viewModel: RoutinesViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
            androidx.compose.runtime.LaunchedEffect(Unit) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.BAKLAVA) {
                    ProjectedContext.isProjectedDeviceConnected(context, this.coroutineContext)
                        .collectLatest { isConnected ->
                            viewModel.updateGlassConnection(isConnected)
                        }
                } else {
                    viewModel.updateGlassConnection(false)
                }
            }
            RoutineDetailUiRoute(
                uiState = RoutineDetailUiState(route.routineId, state),
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo,
                sideEffects = viewModel.sideEffect
            )
        }
        is KoColorRoute.RoutineEditor -> NavEntry(route) {
            val viewModel: RoutinesViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            androidx.compose.runtime.LaunchedEffect(route.routineId) {
                viewModel.onEvent(com.zoewave.probase.kocolor.features.routines.ui.RoutinesEvent.StartEditing(route.routineId))
            }
            RoutineEditorScreen(
                uiState = RoutineEditorUiState(route.stepId, state, onBack),
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
        is KoColorRoute.DiscoveryStatus -> NavEntry(route) {
            val viewModel: DiscoveryStatusViewModel = hiltViewModel()
            val status by viewModel.discoveryStatus.collectAsStateWithLifecycle()
            DiscoveryStatusScreen(status = status, onBack = onBack)
        }
        is KoColorRoute.CosmeticAnalytics -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CosmeticAnalyticsScreen(
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
                effect = viewModel.effect,
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
        is KoColorRoute.WardrobeAnalytics -> NavEntry(route) {
            val viewModel: WardrobeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            WardrobeAnalyticsScreen(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.InventoryManagement -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            InventoryManagementScreen(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.ExpiringSoon -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            com.zoewave.probase.kocolor.features.cosmetics.ui.ExpiringCosmeticsScreen(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Cosmetics -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CosmeticsScreen(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.CosmeticCategoryCover -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CosmeticCategoryCoverScreen(
                uiState = CosmeticCategoryCoverUiState(route.categoryName, state),
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
                uiState = WardrobeCategoryCoverUiState(route.categoryName, state),
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.WardrobeDetail -> NavEntry(route) {
            val viewModel: WardrobeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            WardrobeDetailScreen(
                uiState = WardrobeDetailUiState(route.itemId, state),
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.WardrobeEdit -> NavEntry(route) {
            val viewModel: WardrobeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            WardrobeEditScreen(
                uiState = WardrobeEditUiState(route.itemId, state),
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.WardrobeColorVerification -> NavEntry(route) {
            val viewModel: WardrobeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            ColorVerificationRoute(
                uiState = ColorVerificationUiState(uiState.items),
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
                    uiState = ColorDetailUiState(analysis),
                    onEvent = colorViewModel::onEvent,
                    navTo = onNavigateTo
                )
            }
        }
        is KoColorRoute.Suggestions -> NavEntry(route) {
            val viewModel: com.zoewave.probase.kocolor.features.suggestions.ui.SuggestionsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            
            androidx.compose.runtime.LaunchedEffect(state.fashionProfile) {
                if (state.fashionProfile != null && state.loadingState is com.zoewave.probase.kocolor.features.suggestions.ui.SuggestionsLoadingState.Idle) {
                    viewModel.getSuggestions()
                }
            }

            com.zoewave.probase.kocolor.features.suggestions.ui.SuggestionsUiRoute(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.CosmeticAdd -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            
            androidx.compose.runtime.LaunchedEffect(route.categoryFilter) {
                viewModel.onEvent(CosmeticsEvent.InitializeAdd(route.categoryFilter))
            }

            StitchProductBuilder(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.CosmeticDetail -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val item = state.items.find { it.id == route.itemId }
            CosmeticDetailScreen(
                uiState = CosmeticDetailUiState(item),
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.CosmeticEdit -> NavEntry(route) {
            val viewModel: CosmeticsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            
            androidx.compose.runtime.LaunchedEffect(route.itemId) {
                viewModel.onEvent(CosmeticsEvent.InitializeEdit(route.itemId))
            }

            StitchProductBuilder(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Settings -> NavEntry(route) {
            val viewModel: com.zoewave.probase.kocolor.mobile.features.settings.ui.SettingsViewModel = hiltViewModel()
            
            androidx.compose.runtime.LaunchedEffect(route.section) {
                val section = route.section
                if (section != null) {
                    viewModel.onEvent(com.zoewave.probase.kocolor.mobile.features.settings.ui.SettingsEvent.InitializeWithSection(section))
                }
            }

            val state by viewModel.uiState.collectAsStateWithLifecycle()
            SettingsUiRoute(
                uiState = state,
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.StarterPack -> NavEntry(route) {
            val viewModel: com.zoewave.probase.kocolor.features.starterpack.ui.StarterPackViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            SyncHubScreen(
                uiState = state,
                onEvent = viewModel::onEvent,
                onNavigateTo = onNavigateTo,
                onBack = onBack
            )
        }
        is KoColorRoute.PackPreview -> NavEntry(route) {
            val viewModel: PackPreviewViewModel = hiltViewModel()

            LaunchedEffect(route.packId) {
                viewModel.initialize(
                    packId = route.packId,
                    targetItemId = route.targetItemId,
                    sha256 = route.sha256,
                    publisher = route.publisher
                )
            }

            val state by viewModel.uiState.collectAsStateWithLifecycle()
            PackPreviewScreen(
                uiState = state,
                onToggleSelection = viewModel::onToggleSelection,
                onSelectAll = viewModel::onSelectAll,
                onDeselectAll = viewModel::onDeselectAll,
                onImportSelected = viewModel::onImportSelected,
                onBack = onBack
            )
        }
        is KoColorRoute.Health -> NavEntry(route) {
            val viewModel: com.zoewave.probase.features.health.core.ui.HealthViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            
            HealthUiRoute(
                uiState = com.zoewave.probase.kocolor.mobile.core.ui.health.KoColorHealthUiState(
                    featureState = state,
                    sideEffects = viewModel.sideEffect
                ),
                onEvent = viewModel::onEvent,
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.Hydration -> NavEntry(route) {
            com.zoewave.probase.features.health.hydration.ui.HydrationUiRoute(
                onNavigateToSettings = { onNavigateTo(KoColorRoute.Settings("Hydration")) },
                onBack = onBack
            )
        }
        is KoColorRoute.Weather -> NavEntry(route) {
            val settingsViewModel: com.zoewave.probase.kocolor.mobile.features.settings.ui.SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            WeatherUiRoute(
                onBack = onBack,
                onNavigateToSunIntelligence = { onNavigateTo(KoColorRoute.SunIntelligence) },
                tempUnit = settingsState.tempUnit
            )
        }
        is KoColorRoute.SunIntelligence -> NavEntry(route) {
            com.zoewave.probase.features.weather.ui.sun.SunIntelligenceScreen(onBack = onBack)
        }
        is KoColorRoute.Nutrition -> NavEntry(route) {
            val viewModel: com.zoewave.probase.kocolor.features.routines.ui.RoutinesViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val routineId = state.mealsRoutine?.id ?: 0L
            
            com.zoewave.probase.features.health.nutrition.ui.ritual.NutritionUiRoute(
                onBack = onBack,
                onNavigateToKnowledgeHub = { stepId -> 
                    onNavigateTo(KoColorRoute.RoutineEditor(routineId, stepId))
                }
            )
        }
        is KoColorRoute.MealsHub -> NavEntry(route) {
            val viewModel: MealsViewModel = hiltViewModel()
            
            androidx.compose.runtime.LaunchedEffect(route.mealId, route.isCooking) {
                if (route.mealId != null) {
                    val currentState = viewModel.uiState.value
                    if (currentState is MealsUiState.Success) {
                        val meal = currentState.meals.find { it.id == route.mealId }
                        if (meal != null) {
                            viewModel.onEvent(MealsUiEvent.SelectMeal(meal))
                            if (route.isCooking) {
                                viewModel.onEvent(MealsUiEvent.StartCooking(meal))
                            }
                        }
                    }
                }
            }

            MealsUiRoute(
                onBack = onBack,
                onNavigateToHome = { onNavigateTo(KoColorRoute.Home) }
            )
        }
        /* is KoColorRoute.NailLab -> NavEntry(route) {
            com.zoewave.probase.features.ar.naillab.ui.NailLabUiRoute(
                uiState = com.zoewave.probase.features.ar.naillab.ui.NailLabInitialUiState(route.colorHex, route.finish),
                onEvent = {},
                navTo = onNavigateTo
            )
        }
        is KoColorRoute.FaceLab -> NavEntry(route) {
            com.zoewave.probase.features.ar.facelab.ui.FaceLabUiRoute(
                uiState = com.zoewave.probase.features.ar.facelab.ui.FaceLabInitialUiState(route.colorHex, route.category),
                onEvent = {},
                navTo = onNavigateTo
            )
        }*/
        is KoColorRoute.QRScanner -> NavEntry(route) {
            QRCodeScannerScreen(onCodeScanned = { onCodeScanned(it); onBack() })
        }
        is KoColorRoute.BarcodeScanner -> NavEntry(route) {
            BarcodeScannerScreen(onCodeScanned = { onCodeScanned(it); onBack() })
        }
        is KoColorRoute.GoogleXRTest -> NavEntry(route) {
            val context = LocalContext.current
            androidx.compose.runtime.LaunchedEffect(Unit) {
                if (android.os.Build.VERSION.SDK_INT >= 35) {
                    try {
                        val options = ProjectedContext.createProjectedActivityOptions(context)
                        val intent = android.content.Intent(context, com.zoewave.probase.features.xr.glass.GoogleTestGlassesActivity::class.java).apply {
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent, options.toBundle())
                    } catch (e: Exception) {
                    }
                } else {
                    val intent = android.content.Intent(context, com.zoewave.probase.features.xr.glass.GoogleTestGlassesActivity::class.java).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                onBack()
            }
        }
        is KoColorRoute.Camera -> NavEntry(route) {
            com.zoewave.probase.features.camera.ui.CameraUIRoute(
                target = route.target,
                navTo = { result ->
                    if (result.startsWith("result_ok:")) {
                        val uri = result.substringAfter("result_ok:")
                        when (route.target) {
                            "face", "face_simulator" -> onFaceCaptured(uri)
                            "hair" -> onHairCaptured(uri)
                            "shoes" -> onShoesCaptured(uri)
                            "clothes" -> onClothesCaptured(uri)
                            "inventory_item" -> onInventoryItemCaptured(uri)
                            "color_scan" -> onColorCaptured(uri)
                            else -> {
                                if (route.target.startsWith("ritual_step:")) {
                                    val parts = route.target.split(":")
                                    if (parts.size == 3) {
                                        val routineId = parts[1].toLongOrNull() ?: 0L
                                        val stepId = parts[2]
                                        onRitualStepCaptured(routineId, stepId, uri)
                                    }
                                }
                            }
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
