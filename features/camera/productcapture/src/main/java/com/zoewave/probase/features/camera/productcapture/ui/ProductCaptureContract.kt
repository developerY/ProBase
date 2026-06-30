package com.zoewave.probase.features.camera.productcapture.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Configuration for a single step in the product capture sequence.
 */
data class CaptureStepConfig(
    val id: String,
    val label: String,
    val hint: String,
    val isSkippable: Boolean = false,
    val icon: ImageVector? = null,
    val viewfinderOverlay: @Composable () -> Unit = {}
)

/**
 * High-level configuration for the entire capture session.
 */
data class ProductCaptureSessionConfig(
    val title: String,
    val steps: List<CaptureStepConfig>,
    val themeColor: Color = Color(0xFF22d3ee)
)

sealed interface ProductCaptureUiEvent {
    data class Capture(val uri: String) : ProductCaptureUiEvent
    data object SkipStep : ProductCaptureUiEvent
    data class DeletePhoto(val index: Int) : ProductCaptureUiEvent
    data object Close : ProductCaptureUiEvent
}
