package com.zoewave.probase.features.xr.glass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.IconButton
import androidx.xr.glimmer.Text
import com.zoewave.probase.features.xr.glass.samples.ButtonsSamples
import com.zoewave.probase.features.xr.glass.samples.CardSamples
import com.zoewave.probase.features.xr.glass.samples.ColorsSamples
import com.zoewave.probase.features.xr.glass.samples.DepthEffectLevelsSample
import com.zoewave.probase.features.glass.translation.ui.TranslationScreen
import com.zoewave.probase.features.xr.glass.samples.GlimmerLazyListSamples
import com.zoewave.probase.features.xr.glass.samples.GlimmerPagerSamples
import com.zoewave.probase.features.xr.glass.samples.IconButtonSamples
import com.zoewave.probase.features.xr.glass.samples.IconSamples
import com.zoewave.probase.features.xr.glass.samples.IconToggleButtonsSamples
import com.zoewave.probase.features.xr.glass.samples.IndirectPointerGestureSamples
import com.zoewave.probase.features.xr.glass.samples.ListItemSamples
import com.zoewave.probase.features.xr.glass.samples.ObjectRecognitionScreen
import com.zoewave.probase.features.xr.glass.samples.ShapesSamples
import com.zoewave.probase.features.xr.glass.samples.SpatialNoteOverlay
import com.zoewave.probase.features.xr.glass.samples.StacksSamples
import com.zoewave.probase.features.xr.glass.samples.SurfaceSamples
import com.zoewave.probase.features.xr.glass.samples.TitleChipSamples
import com.zoewave.probase.features.xr.glass.samples.ToggleButtonSamples
import com.zoewave.probase.features.xr.glass.samples.TypographySamples
import com.zoewave.probase.features.xr.glass.samples.VoiceInputIndicatorSamples
import com.zoewave.probase.features.glass.vision.ui.VisionRoute
import androidx.camera.core.ExperimentalLensFacing
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@ExperimentalLensFacing
@ExperimentalCamera2Interop
@OptIn(ExperimentalProjectedApi::class, ExperimentalPermissionsApi::class)
@Composable
fun GlassApp(
    areVisualsOn: Boolean,
    isVisualUiSupported: Boolean,
    onClose: () -> Unit,
    onSpeak: (String) -> Unit,
    initialSample: GlimmerSample? = null,
    viewModel: GlassViewModel = hiltViewModel(),
    demosViewModel: GlassXRDemosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeSampleState by demosViewModel.activeSample.collectAsStateWithLifecycle()
    
    // PRIORITY: If we specifically passed "Ritual" via intent, show it.
    // Otherwise follow the demosViewModel state.
    val currentSample = initialSample ?: activeSampleState

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (currentSample == null) {
            SamplesMenu(
                onSampleSelected = { 
                    demosViewModel.updateActiveSample(it)
                }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Hide header for Ritual to provide full-screen immersive ritual experience
                if (currentSample != GlimmerSample.Ritual) {
                    SampleNavigationHeader(
                        sample = currentSample,
                        onBack = { demosViewModel.updateActiveSample(null) },
                        onPrevious = { demosViewModel.updateActiveSample(currentSample.previous()) },
                        onNext = { demosViewModel.updateActiveSample(currentSample.next()) }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentSample) {
                        GlimmerSample.Ritual -> {
                            GlassRitualLayout(
                                uiState = uiState,
                                areVisualsOn = areVisualsOn,
                                isVisualUiSupported = isVisualUiSupported,
                                onEvent = { event ->
                                    when (event) {
                                        GlassUiEvent.CloseApp -> {
                                            if (initialSample != null) onClose() else demosViewModel.updateActiveSample(null)
                                        }
                                        is GlassUiEvent.ToggleStep -> {
                                            viewModel.onEvent(event)
                                            val step = uiState.morningRoutine?.steps?.find { it.id == event.stepId }
                                            if (step != null && !step.isCompleted) {
                                                onSpeak("Step ${step.title} completed.")
                                            }
                                        }
                                        GlassUiEvent.ToggleAi -> viewModel.onEvent(event)
                                    }
                                }
                            )
                        }
                        GlimmerSample.Buttons -> ButtonsSamples()
                        GlimmerSample.Cards -> CardSamples()
                        GlimmerSample.Colors -> ColorsSamples()
                        GlimmerSample.Depth -> DepthEffectLevelsSample()
                        GlimmerSample.LazyList -> GlimmerLazyListSamples()
                        GlimmerSample.Pager -> GlimmerPagerSamples()
                        GlimmerSample.IconButtons -> IconButtonSamples()
                        GlimmerSample.Icons -> IconSamples()
                        GlimmerSample.IconToggleButtons -> IconToggleButtonsSamples()
                        GlimmerSample.IndirectPointer -> IndirectPointerGestureSamples()
                        GlimmerSample.ListItems -> ListItemSamples()
                        GlimmerSample.Shapes -> ShapesSamples()
                        GlimmerSample.Stacks -> StacksSamples()
                        GlimmerSample.Surface -> SurfaceSamples()
                        GlimmerSample.TitleChips -> TitleChipSamples()
                        GlimmerSample.ToggleButtons -> ToggleButtonSamples()
                        GlimmerSample.Typography -> TypographySamples()
                        GlimmerSample.VoiceIndicator -> VoiceInputIndicatorSamples(level = { uiState.aiAudioLevel })
                        GlimmerSample.Translation -> TranslationScreen()
                        GlimmerSample.Vision -> VisionRoute()
                        GlimmerSample.ObjectRecognition -> ObjectRecognitionScreen()
                        GlimmerSample.SpatialNote -> SpatialNoteOverlay()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlassAppPreview() {
    GlimmerTheme {
        GlassApp(
            areVisualsOn = true,
            isVisualUiSupported = true,
            onClose = {},
            onSpeak = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SampleNavigationHeaderPreview() {
    GlimmerTheme {
        SampleNavigationHeader(
            sample = GlimmerSample.Ritual,
            onBack = {},
            onPrevious = {},
            onNext = {}
        )
    }
}

@Composable
fun SampleNavigationHeader(
    sample: GlimmerSample,
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Back to Menu")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = sample.title,
                style = GlimmerTheme.typography.titleSmall,
                color = GlimmerTheme.colors.primary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
            }
            Spacer(Modifier.width(16.dp))
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        }
    }
}
