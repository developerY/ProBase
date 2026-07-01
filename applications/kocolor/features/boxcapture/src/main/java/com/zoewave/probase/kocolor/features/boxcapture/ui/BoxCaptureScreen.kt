package com.zoewave.probase.kocolor.features.boxcapture.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.features.camera.productcapture.ui.AnalysisView
import com.zoewave.probase.features.camera.productcapture.ui.CaptureStepConfig
import com.zoewave.probase.features.camera.productcapture.ui.ColorConfirmationUiState
import com.zoewave.probase.features.camera.productcapture.ui.ColorConfirmationView
import com.zoewave.probase.features.camera.productcapture.ui.ErrorView
import com.zoewave.probase.features.camera.productcapture.ui.GenericProductCaptureUiRoute
import com.zoewave.probase.features.camera.productcapture.ui.PriceConfirmationUiState
import com.zoewave.probase.features.camera.productcapture.ui.PriceConfirmationView
import com.zoewave.probase.features.camera.productcapture.ui.ProductCaptureSessionConfig
import com.zoewave.probase.features.camera.productcapture.ui.ProductCaptureUiEvent
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.BoxCaptureUiState
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.CaptureMode
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.CaptureStep
import com.zoewave.probase.kocolor.model.KoColorRoute

sealed interface BoxCaptureEvent {
    data class Capture(val uri: String) : BoxCaptureEvent
    data class BarcodeScanned(val code: String) : BoxCaptureEvent
    data object Retry : BoxCaptureEvent
    data object Dismiss : BoxCaptureEvent
    data class Success(val item: CosmeticItem) : BoxCaptureEvent
    data class DeletePhoto(val index: Int) : BoxCaptureEvent
    data class ChangeMode(val mode: CaptureMode) : BoxCaptureEvent
    data object SubmitToAi : BoxCaptureEvent
    data object SkipBarcode : BoxCaptureEvent
    data object SkipStep : BoxCaptureEvent
    data class OnColorSelected(val hex: String) : BoxCaptureEvent
    data object ConfirmColor : BoxCaptureEvent
    data object ClearColor : BoxCaptureEvent
    data object ConfirmPrice : BoxCaptureEvent
    data class OnPriceChanged(val price: Double) : BoxCaptureEvent
}

@Composable
fun BoxCaptureUiRoute(
    uiState: BoxCaptureUiState,
    modifier: Modifier = Modifier,
    onEvent: (BoxCaptureEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    LaunchedEffect(uiState) {
        if (uiState is BoxCaptureUiState.Success) {
            onEvent(BoxCaptureEvent.Success(uiState.item))
        }
    }

    BoxCaptureScreen(
        uiState = uiState,
        modifier = modifier,
        onEvent = onEvent,
        navTo = navTo
    )
}

@Composable
internal fun BoxCaptureScreen(
    uiState: BoxCaptureUiState,
    modifier: Modifier = Modifier,
    onEvent: (BoxCaptureEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    when (uiState) {
        is BoxCaptureUiState.Idle -> {
            val steps = CaptureStep.getStepsForMode(uiState.mode)
            val config = remember<ProductCaptureSessionConfig>(uiState.mode, uiState.extractedColorHex) {
                ProductCaptureSessionConfig(
                    title = "Capture Product",
                    steps = steps.map { step ->
                        CaptureStepConfig(
                            id = step.name,
                            label = step.label,
                            hint = getHintForStep(step),
                            isSkippable = step.isSkippable,
                            viewfinderOverlay = {
                                if (step == CaptureStep.COLOR) {
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
                    }
                )
            }

            GenericProductCaptureUiRoute(
                config = config,
                capturedUris = uiState.capturedUris,
                currentStepIndex = steps.indexOf(uiState.currentStep),
                onEvent = { event ->
                    when (event) {
                        is ProductCaptureUiEvent.Capture -> onEvent(BoxCaptureEvent.Capture(event.uri))
                        ProductCaptureUiEvent.SkipStep -> onEvent(BoxCaptureEvent.SkipStep)
                        is ProductCaptureUiEvent.DeletePhoto -> onEvent(BoxCaptureEvent.DeletePhoto(event.index))
                        ProductCaptureUiEvent.Close -> onEvent(BoxCaptureEvent.Dismiss)
                        is ProductCaptureUiEvent.OnPriceChanged -> onEvent(BoxCaptureEvent.OnPriceChanged(event.price))
                    }
                },
                modifier = modifier
            )
        }
        is BoxCaptureUiState.Analyzing -> {
            AnalysisView(uiState.progress)
        }
        is BoxCaptureUiState.ColorConfirmation -> {
            ColorConfirmationView(
                uiState = ColorConfirmationUiState(
                    photoUri = uiState.capturedUris.last { it.isNotBlank() },
                    suggestedColors = uiState.suggestedColors,
                    selectedColorHex = uiState.selectedColorHex
                ),
                onColorSelected = { onEvent(BoxCaptureEvent.OnColorSelected(it)) },
                onConfirm = { onEvent(BoxCaptureEvent.ConfirmColor) },
                onClear = { onEvent(BoxCaptureEvent.ClearColor) }
            )
        }
        is BoxCaptureUiState.PriceConfirmation -> {
            PriceConfirmationView(
                uiState = PriceConfirmationUiState(
                    detectedPrice = uiState.detectedPrice
                ),
                onPriceChanged = { onEvent(BoxCaptureEvent.OnPriceChanged(it)) },
                onConfirm = { onEvent(BoxCaptureEvent.ConfirmPrice) },
                onManualEntry = { onEvent(BoxCaptureEvent.OnPriceChanged(0.0)) } 
            )
        }
        is BoxCaptureUiState.Review -> {
            ReviewView(
                uiState = ReviewViewUiState(
                    capturedUris = uiState.capturedUris,
                    barcode = uiState.barcode,
                    ingredientsOcr = uiState.ingredientsOcr,
                    instructionsOcr = uiState.instructionsOcr,
                    enrichmentData = uiState.enrichmentData,
                    manualColorHex = uiState.manualColorHex,
                    price = uiState.price
                ),
                onEvent = onEvent
            )
        }
        is BoxCaptureUiState.Error -> {
            ErrorView(uiState.message, onRetry = { onEvent(BoxCaptureEvent.Retry) })
        }
        is BoxCaptureUiState.Success -> {}
    }
}

private fun getHintForStep(step: CaptureStep): String = when (step) {
    CaptureStep.FRONT -> "Capture the product front"
    CaptureStep.BACK -> "Capture the product back"
    CaptureStep.INGREDIENTS -> "Ensure the ingredients list is clear"
    CaptureStep.INSTRUCTIONS -> "Capture usage instructions (if any)"
    CaptureStep.PRICE -> "Align the price tag within the frame"
    CaptureStep.COLOR -> "Capture the best representation of product color"
    CaptureStep.BARCODE -> "Final step: Scan the barcode"
}

data class ReviewViewUiState(
    val capturedUris: List<String>,
    val barcode: String?,
    val ingredientsOcr: String,
    val instructionsOcr: String,
    val enrichmentData: CosmeticItem? = null,
    val manualColorHex: String? = null,
    val price: Double? = null
)

@Composable
private fun ReviewView(
    uiState: ReviewViewUiState,
    onEvent: (BoxCaptureEvent) -> Unit
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
                text = "Capture Review",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onEvent(BoxCaptureEvent.Dismiss) }) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ReviewSection(title = "Captured Photos (${uiState.capturedUris.filter { it.isNotBlank() }.size})") {
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
                                        onClick = { onEvent(BoxCaptureEvent.DeletePhoto(index)) },
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
                ReviewSection(title = "Color Identity") {
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
                ReviewSection(title = "Price Analysis") {
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF22d3ee)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = uiState.price?.let { String.format("%.2f", it) } ?: "Not captured",
                                color = if (uiState.price != null) Color.White else Color.Gray,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                ReviewSection(title = "Barcode Intelligence") {
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCodeScanner, null, tint = Color(0xFF22d3ee))
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = uiState.barcode ?: "Not scanned",
                                    color = if (uiState.barcode != null) Color.White else Color.Gray,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            uiState.enrichmentData?.let { 
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF22d3ee).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF22d3ee), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Database hit: ${it.brand} ${it.name}",
                                        color = Color(0xFF22d3ee),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                ReviewSection(title = "Local Analysis: Ingredients") {
                    OcrTextArea(text = uiState.ingredientsOcr)
                }
            }

            item {
                ReviewSection(title = "Local Analysis: Instructions") {
                    OcrTextArea(text = uiState.instructionsOcr)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onEvent(BoxCaptureEvent.SubmitToAi) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22d3ee))
        ) {
            Icon(Icons.Default.AutoAwesome, null, tint = Color.Black)
            Spacer(Modifier.width(12.dp))
            Text("FINALIZE WITH GEMINI AI", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ReviewSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            color = Color(0xFF22d3ee),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun OcrTextArea(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp)
    ) {
        val scroll = rememberScrollState()
        Box(modifier = Modifier.padding(16.dp).verticalScroll(scroll)) {
            Text(
                text = if (text.isBlank()) "No text detected locally." else text,
                color = if (text.isBlank()) Color.Gray else Color.White,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )
        }
    }
}
