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
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.IconButton
import androidx.xr.glimmer.Text
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.xr.glass.samples.*

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
                    }
                }
            }
        }
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
