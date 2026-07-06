package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.*
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.kocolor.model.KoColorRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleSimulatorScreen(
    uiState: StyleSimulatorUiState,
    modifier: Modifier = Modifier,
    effect: kotlinx.coroutines.flow.Flow<SimulatorEffect>? = null,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onEvent(SimulatorEvent.OnPortraitSelected(it.toString())) }
    }

    if (effect != null) {
        LaunchedEffect(Unit) {
            effect.collect { simulatorEffect ->
                when (simulatorEffect) {
                    SimulatorEffect.NavigateToHistory -> navTo(KoColorRoute.Color)
                    is SimulatorEffect.NavigateToCamera -> navTo(KoColorRoute.Camera(simulatorEffect.target))
                    SimulatorEffect.OpenGalleryPicker -> {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_analyzer_simulator_title), style = MaterialTheme.typography.labelLarge, letterSpacing = 4.sp) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Home) }) {
                        Icon(Icons.Default.Close, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            MagicBackground()

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                AnimatedContent(
                    targetState = uiState.simulationStep,
                    transitionSpec = { fadeIn(tween(1000)) togetherWith fadeOut(tween(1000)) },
                    label = "step_transition"
                ) { step ->
                    when (step) {
                        SimulationStep.MESSAGING -> MessagingStep(
                            userMessage = uiState.userMessage,
                            userPortraitUri = uiState.userPortraitUri,
                            allClothing = uiState.fullClothingInventory,
                            allCosmetics = uiState.fullCosmeticInventory,
                            anchoredClothing = uiState.anchoredClothing,
                            anchoredCosmetics = uiState.anchoredCosmetics,
                            selectedClothingCategory = uiState.selectedClothingCategory,
                            selectedCosmeticCategory = uiState.selectedCosmeticCategory,
                            onEvent = onEvent,
                            navTo = navTo
                        )
                        SimulationStep.BIO_MARKERS, SimulationStep.ROUTINE, SimulationStep.GENERATING -> AnalysisStep(
                            uiState = uiState,
                            onEvent = onEvent,
                            navTo = navTo
                        )
                        SimulationStep.RESULT -> ResultStep(
                            uiState = uiState,
                            onEvent = onEvent,
                            navTo = navTo
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StyleSimulatorScreenPreview() {
    MaterialTheme {
        StyleSimulatorScreen(
            uiState = StyleSimulatorUiState(
                simulationStep = SimulationStep.RESULT,
                recommendedPalette = listOf("#F4D03F", "#16A085", "#2C3E50"),
                recommendedClothing = listOf(ClothingItem(name = "Silk Evening Shirt", category = ClothingCategory.TOPS, brand = "ZoeWave"))
            ),
            effect = null,
            onEvent = {},
            navTo = {}
        )
    }
}
