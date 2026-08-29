package com.zoewave.probase.kocolor.features.clothingcapture.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.network.ServiceStatus
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.features.camera.productcapture.ui.AnalysisView
import com.zoewave.probase.features.camera.productcapture.ui.CaptureStepConfig
import com.zoewave.probase.features.camera.productcapture.ui.ColorConfirmationUiState
import com.zoewave.probase.features.camera.productcapture.ui.ColorConfirmationView
import com.zoewave.probase.features.camera.productcapture.ui.DiscoveryStatusScreen
import com.zoewave.probase.features.camera.productcapture.ui.ErrorView
import com.zoewave.probase.features.camera.productcapture.ui.GenericProductCaptureUiRoute
import com.zoewave.probase.features.camera.productcapture.ui.PriceConfirmationUiState
import com.zoewave.probase.features.camera.productcapture.ui.PriceConfirmationView
import com.zoewave.probase.features.camera.productcapture.ui.ProductCaptureSessionConfig
import com.zoewave.probase.features.camera.productcapture.ui.ProductCaptureUiEvent
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog
import com.zoewave.probase.core.ui.util.parseColor
import com.zoewave.probase.core.ui.util.toHex
import com.zoewave.probase.kocolor.features.clothingcapture.R
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
    data object ContinueToReview : ClothingCaptureEvent
    data object FinalizeProduct : ClothingCaptureEvent
    data object SaveProduct : ClothingCaptureEvent
    data class FinalReviewColorChanged(val hex: String) : ClothingCaptureEvent
    data class OnColorSelected(val hex: String) : ClothingCaptureEvent
    data object ConfirmColor : ClothingCaptureEvent
    data object ClearColor : ClothingCaptureEvent
    data object ConfirmPrice : ClothingCaptureEvent
    data class OnPriceChanged(val price: Double) : ClothingCaptureEvent
}

@Composable
fun ClothingCaptureUiRoute(
    uiState: ClothingCaptureUiState,
    discoveryStatus: com.zoewave.probase.core.model.network.DiscoveryStatus,
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
        discoveryStatus = discoveryStatus,
        modifier = modifier,
        onEvent = onEvent,
        navTo = navTo
    )
}

@Composable
internal fun ClothingCaptureScreen(
    uiState: ClothingCaptureUiState,
    discoveryStatus: com.zoewave.probase.core.model.network.DiscoveryStatus,
    modifier: Modifier = Modifier,
    onEvent: (ClothingCaptureEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val themePink = Color(0xFFf472b6)

    when (uiState) {
        is ClothingCaptureUiState.Idle -> {
            val steps = ClothingCaptureStep.ALL
            val config = remember<ProductCaptureSessionConfig>(uiState.extractedColorHex) {
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
                        is ProductCaptureUiEvent.OnPriceChanged -> onEvent(ClothingCaptureEvent.OnPriceChanged(event.price))
                        is ProductCaptureUiEvent.BarcodeScanned -> { /* Not used in clothing flow */ }
                    }
                },
                modifier = modifier
            )
        }
        is ClothingCaptureUiState.Analyzing -> {
            DiscoveryStatusScreen(
                status = discoveryStatus,
                mode = com.zoewave.probase.features.camera.productcapture.ui.DiscoveryMode.DETERMINISTIC,
                onBack = { onEvent(ClothingCaptureEvent.Retry) },
                onNext = { onEvent(ClothingCaptureEvent.ContinueToReview) }
            )
        }
        is ClothingCaptureUiState.AiAnalyzing -> {
            DiscoveryStatusScreen(
                status = discoveryStatus,
                mode = com.zoewave.probase.features.camera.productcapture.ui.DiscoveryMode.AI_SYNTHESIS,
                onBack = { onEvent(ClothingCaptureEvent.Retry) },
                onNext = { onEvent(ClothingCaptureEvent.FinalizeProduct) }
            )
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
        is ClothingCaptureUiState.PriceConfirmation -> {
            PriceConfirmationView(
                uiState = PriceConfirmationUiState(
                    detectedPrice = uiState.detectedPrice,
                    themeColor = themePink
                ),
                onPriceChanged = { onEvent(ClothingCaptureEvent.OnPriceChanged(it)) },
                onConfirm = { onEvent(ClothingCaptureEvent.ConfirmPrice) },
                onManualEntry = { onEvent(ClothingCaptureEvent.OnPriceChanged(0.0)) }
            )
        }
        is ClothingCaptureUiState.Review -> {
            ReviewView(
                uiState = ReviewViewUiState(
                    capturedUris = uiState.capturedUris,
                    labelsOcr = uiState.labelsOcr,
                    manualColorHex = uiState.manualColorHex,
                    price = uiState.price
                ),
                onEvent = onEvent,
                themeColor = themePink
            )
        }
        is ClothingCaptureUiState.Error -> {
            ErrorView(uiState.message, onRetry = { onEvent(ClothingCaptureEvent.Retry) })
        }
        is ClothingCaptureUiState.FinalReview -> {
            FinalClothingValidationView(
                item = uiState.item,
                onColorChanged = { onEvent(ClothingCaptureEvent.FinalReviewColorChanged(it)) },
                onSave = { onEvent(ClothingCaptureEvent.SaveProduct) },
                onCancel = { onEvent(ClothingCaptureEvent.Dismiss) }
            )
        }
        is ClothingCaptureUiState.Success -> {}
    }
}

@Composable
private fun FinalClothingValidationView(
    item: com.zoewave.probase.core.model.ritual.ClothingItem,
    onColorChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0f172a))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Style Synthesis",
            color = Color.White,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
        )
        Text(
            "AI has refined your garment details",
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(Modifier.height(48.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(item.brand?.uppercase() ?: "UNKNOWN BRAND", color = Color(0xFFf472b6), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                Text(item.name, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                
                Spacer(Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showColorPicker = true }
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(parseColor(item.colorHex), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.applications_kocolor_features_clothingcapture_verified_color), color = Color.White, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        if (showColorPicker) {
            ColorPickerDialog(
                initialColor = parseColor(item.colorHex),
                onColorSelected = { onColorChanged(it.toHex()) },
                onDismissRequest = { showColorPicker = false }
            )
        }
        
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFf472b6))
        ) {
            Text(stringResource(R.string.applications_kocolor_features_clothingcapture_add_to_wardrobe), color = Color.Black, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onCancel, modifier = Modifier.padding(top = 16.dp)) {
            Text(stringResource(R.string.applications_kocolor_features_clothingcapture_discard), color = Color.Red.copy(alpha = 0.7f))
        }
    }
}

private fun getHintForStep(step: ClothingCaptureStep): String = when (step) {
    ClothingCaptureStep.FRONT -> "Capture the front silhouette"
    ClothingCaptureStep.BACK -> "Capture the overall back view"
    ClothingCaptureStep.LABEL -> "Capture the brand and care label"
    ClothingCaptureStep.PRICE -> "Align the price tag within the frame"
    ClothingCaptureStep.COLOR -> "Capture the truest representation of fabric color"
}

data class ReviewViewUiState(
    val capturedUris: List<String>,
    val labelsOcr: String,
    val manualColorHex: String? = null,
    val price: Double? = null
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
                ReviewSection(title = "Price Analysis", themeColor = themeColor) {
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$",
                                style = MaterialTheme.typography.titleLarge,
                                color = themeColor
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
            Text(stringResource(R.string.applications_kocolor_features_clothingcapture_finalize_gemini), color = Color.Black, fontWeight = FontWeight.Bold)
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
