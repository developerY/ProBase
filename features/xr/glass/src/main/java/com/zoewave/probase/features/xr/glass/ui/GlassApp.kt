package com.zoewave.probase.features.xr.glass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.xr.glass.samples.ButtonsSamples
import com.zoewave.probase.features.xr.glass.samples.CardSamples
import com.zoewave.probase.features.xr.glass.samples.ColorsSamples
import com.zoewave.probase.features.xr.glass.samples.DepthEffectLevelsSample
import com.zoewave.probase.features.xr.glass.samples.GlimmerLazyListSamples
import com.zoewave.probase.features.xr.glass.samples.GlimmerPagerSamples
import com.zoewave.probase.features.xr.glass.samples.IconButtonSamples
import com.zoewave.probase.features.xr.glass.samples.IconSamples
import com.zoewave.probase.features.xr.glass.samples.IconToggleButtonsSamples
import com.zoewave.probase.features.xr.glass.samples.IndirectPointerGestureSamples
import com.zoewave.probase.features.xr.glass.samples.ListItemSamples
import com.zoewave.probase.features.xr.glass.samples.ShapesSamples
import com.zoewave.probase.features.xr.glass.samples.StacksSamples
import com.zoewave.probase.features.xr.glass.samples.SurfaceSamples
import com.zoewave.probase.features.xr.glass.samples.TitleChipSamples
import com.zoewave.probase.features.xr.glass.samples.ToggleButtonSamples
import com.zoewave.probase.features.xr.glass.samples.TypographySamples
import com.zoewave.probase.features.xr.glass.samples.VoiceInputIndicatorSamples

@Composable
fun GlassApp(
    areVisualsOn: Boolean,
    isVisualUiSupported: Boolean,
    isPermissionDenied: Boolean,
    onRetryPermission: () -> Unit,
    onClose: () -> Unit,
    onSpeak: (String) -> Unit,
    initialSample: GlimmerSample? = null,
    viewModel: GlassViewModel = hiltViewModel(),
    demosViewModel: GlassXRDemosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentSample by remember { mutableStateOf<GlimmerSample?>(initialSample) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (currentSample == null) {
            SamplesMenu(
                onSampleSelected = { 
                    currentSample = it
                    demosViewModel.updateActiveSample(it)
                }
            )
        } else {
            when (currentSample) {
                GlimmerSample.Ritual -> {
                    GlassRitualLayout(
                        uiState = uiState,
                        areVisualsOn = areVisualsOn,
                        isVisualUiSupported = isVisualUiSupported,
                        isPermissionDenied = isPermissionDenied,
                        onRetryPermission = onRetryPermission,
                        onEvent = { event ->
                            when (event) {
                                GlassUiEvent.CloseApp -> {
                                    currentSample = null
                                    demosViewModel.updateActiveSample(null)
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
                else -> {}
            }
            
            // Back button or similar could be added here if samples don't have one
            // For now, let's assume we use System Back (not handled here yet)
        }
    }
}
