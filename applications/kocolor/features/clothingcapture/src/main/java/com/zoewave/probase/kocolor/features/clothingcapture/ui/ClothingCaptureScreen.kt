package com.zoewave.probase.kocolor.features.clothingcapture.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.features.camera.productcapture.ui.AnalysisView
import com.zoewave.probase.features.camera.productcapture.ui.CaptureStepConfig
import com.zoewave.probase.features.camera.productcapture.ui.ColorConfirmationUiState
import com.zoewave.probase.features.camera.productcapture.ui.ColorConfirmationView
import com.zoewave.probase.features.camera.productcapture.ui.ErrorView
import com.zoewave.probase.features.camera.productcapture.ui.GenericProductCaptureUiRoute
import com.zoewave.probase.features.camera.productcapture.ui.ProductCaptureSessionConfig
import com.zoewave.probase.features.camera.productcapture.ui.ProductCaptureUiEvent
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.toHex
import com.zoewave.probase.kocolor.features.clothingcapture.ui.state.ClothingCaptureStep
import com.zoewave.probase.kocolor.features.clothingcapture.ui.state.ClothingCaptureUiState
import com.zoewave.probase.kocolor.model.KoColorRoute

sealed interface ClothingCaptureEvent {
    data class Capture(val uri: String) : ClothingCaptureEvent
    data object Retry : ClothingCaptureEvent
    data object Dismiss : ClothingCaptureEvent
    data class Success(val item: ClothingItem) : ClothingCaptureEvent
    data class DeletePhoto(val index: Int) : ClothingCaptureEvent
    data object SubmitToAi : ClothingCaptureEvent
    data object SkipStep : ClothingCaptureEvent
    data class OnColorSelected(val hex: String) : ClothingCaptureEvent
    data object ConfirmColor : ClothingCaptureEvent
    data object ClearColor : ClothingCaptureEvent
}

@Composable
fun ClothingCaptureUiRoute(
    uiState: ClothingCaptureUiState,
    modifier: Modifier = Modifier,
    onEvent: (ClothingCaptureEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    LaunchedEffect(uiState) {
        if (uiState is ClothingCaptureUiState.Success) {
            onEvent(ClothingCaptureEvent.Success(uiState.item))
        }
    }

    ClothingCaptureScreen(
        uiState = uiState,
        modifier = modifier,
        onEvent = onEvent,
        navTo = navTo
    )
}

@Composable
internal fun ClothingCaptureScreen(
    uiState: ClothingCaptureUiState,
    modifier: Modifier = Modifier,
    onEvent: (ClothingCaptureEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val themePink = Color(0xFFf472b6)

    when (uiState) {
        is ClothingCaptureUiState.Idle -> {
            val steps = ClothingCaptureStep.ALL
            val config = remember(uiState.extractedColorHex) {
                ProductCaptureSessionConfig(
                    title = "Capture Clothing",
                    steps = steps.map { step ->
                        CaptureStepConfig(
                            id = step.name,
                            label = step.label,
                            hint = getHintForStep(step),
                            isSkippable = step.isSkippable,
                            viewfinderOverlay = {
                                if (step == ClothingCaptureStep.COLOR) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .border(2.dp, Color.White, CircleShape)
                                            .padding(4.dp)
                                            .background(uiState.extractedColorHex?.let { parseColor(it) } ?: Color.Transparent, CircleShape)
                                            .clip(CircleShape)
                                    )
                                }
                            }
                        )
                    },
                    themeColor = themePink
                )
            }

            GenericProductCaptureUiRoute(
                config = config,
                capturedUris = uiState.capturedUris,
                currentStepIndex = steps.indexOf(uiState.currentStep),
                onEvent = { event ->
                    when (event) {
                        is ProductCaptureUiEvent.Capture -> onEvent(ClothingCaptureEvent.Capture(event.uri))
                        ProductCaptureUiEvent.SkipStep -> onEvent(ClothingCaptureEvent.SkipStep)
                        is ProductCaptureUiEvent.DeletePhoto -> onEvent(ClothingCaptureEvent.DeletePhoto(event.index))
                        ProductCaptureUiEvent.Close -> onEvent(ClothingCaptureEvent.Dismiss)
                    }
                },
                modifier = modifier
            )
        }
        is ClothingCaptureUiState.Analyzing -> {
            AnalysisView(uiState.progress, themeColor = themePink)
        }
        is ClothingCaptureUiState.ColorConfirmation -> {
            ColorConfirmationView(
                uiState = ColorConfirmationUiState(
                    photoUri = uiState.capturedUris.last { it.isNotBlank() },
                    suggestedColors = uiState.suggestedColors,
                    selectedColorHex = uiState.selectedColorHex
                ),
                onColorSelected = { onEvent(ClothingCaptureEvent.OnColorSelected(it)) },
                onConfirm = { onEvent(ClothingCaptureEvent.ConfirmColor) },
                onClear = { onEvent(ClothingCaptureEvent.ClearColor) },
                themeColor = themePink
            )
        }
        is ClothingCaptureUiState.Review -> {
            ReviewView(
                uiState = ReviewViewUiState(
                    capturedUris = uiState.capturedUris,
                    labelsOcr = uiState.labelsOcr,
                    manualColorHex = uiState.manualColorHex
                ),
                onEvent = onEvent,
                themeColor = themePink
            )
        }
        is ClothingCaptureUiState.Error -> {
            ErrorView(uiState.message, onRetry = { onEvent(ClothingCaptureEvent.Retry) })
        }
        is ClothingCaptureUiState.Success -> {}
    }
}

private fun getHintForStep(step: ClothingCaptureStep): String = when (step) {
    ClothingCaptureStep.FRONT -> "Capture the front silhouette"
    ClothingCaptureStep.BACK -> "Capture the overall back view"
    ClothingCaptureStep.LABEL -> "Capture the brand and care label"
    ClothingCaptureStep.COLOR -> "Capture the truest representation of fabric color"
}

data class ReviewViewUiState(
    val capturedUris: List<String>,
    val labelsOcr: String,
    val manualColorHex: String? = null
)

@Composable
private fun ReviewView(
    uiState: ReviewViewUiState,
    onEvent: (ClothingCaptureEvent) -> Unit,
    themeColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0f172a))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wardrobe Review",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onEvent(ClothingCaptureEvent.Dismiss) }) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ReviewSection(title = "Captured Photos", themeColor = themeColor) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(uiState.capturedUris) { index, uri ->
                            if (uri.isNotBlank()) {
                                Box {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { onEvent(ClothingCaptureEvent.DeletePhoto(index)) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                ReviewSection(title = "Fabric Color", themeColor = themeColor) {
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = uiState.manualColorHex?.let { parseColor(it) } ?: Color.Transparent,
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                if (uiState.manualColorHex == null) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = Color.Gray, modifier = Modifier.padding(8.dp))
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = uiState.manualColorHex ?: "AI will identify color from photos",
                                color = if (uiState.manualColorHex != null) Color.White else Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            item {
                ReviewSection(title = "Local Label OCR", themeColor = themeColor) {
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp)
                    ) {
                        val scroll = rememberScrollState()
                        Box(modifier = Modifier.padding(16.dp).verticalScroll(scroll)) {
                            Text(
                                text = if (uiState.labelsOcr.isBlank()) "No label text detected." else uiState.labelsOcr,
                                color = if (uiState.labelsOcr.isBlank()) Color.Gray else Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onEvent(ClothingCaptureEvent.SubmitToAi) },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = themeColor)
        ) {
            Icon(Icons.Default.AutoAwesome, null, tint = Color.Black)
            Spacer(Modifier.width(12.dp))
            Text("FINALIZE WITH GEMINI AI", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ReviewSection(title: String, themeColor: Color, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            color = themeColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}
