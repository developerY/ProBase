package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState

@Composable
fun FaceBlueprintView(uiState: StyleSimulatorUiState) {
    // SINGLE SOURCE OF TRUTH: Tracks the currently expanded card
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    // 1. Layout Shifts
    val blueprintOffset = 10.dp
    val horizontalShift = 15.dp

    // 2. Define Static Line Targets (Center of the features)
    val eyesAnchor = Offset(35.dp.value, -45.dp.value)
    val eyesCallout = Offset(100.dp.value, -90.dp.value)

    val cheeksAnchor = Offset(-45.dp.value, 25.dp.value)
    val cheeksCallout = Offset(-90.dp.value, 120.dp.value)

    val lipsAnchor = Offset(0.dp.value, 75.dp.value)
    val lipsCallout = Offset(90.dp.value, 150.dp.value)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Central Face Anchor
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = horizontalShift, y = blueprintOffset)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f))
        ) {
            Image(
                painter = painterResource(id = R.drawable.applications_kocolor_features_analyzer_face),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.4f),
                contentScale = ContentScale.Fit
            )
        }

        // Callout Lines & Shades
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2 + horizontalShift.toPx(), size.height / 2 + blueprintOffset.toPx())

            // Extract Items
            val eyesItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.EYES }
            val cheeksItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.DIMENSION }
            val lipsItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.LIPS }

            // 1. Draw "Shades" (Soft Glows on the face)
            // ... (rest of shades logic remains the same)
            eyesItem?.colorHex?.let { hex ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(center.x + eyesAnchor.x.dp.toPx(), center.y + eyesAnchor.y.dp.toPx()),
                        radius = 20.dp.toPx()
                    ),
                    radius = 20.dp.toPx(),
                    center = Offset(center.x + eyesAnchor.x.dp.toPx(), center.y + eyesAnchor.y.dp.toPx())
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(center.x - eyesAnchor.x.dp.toPx(), center.y + eyesAnchor.y.dp.toPx()),
                        radius = 20.dp.toPx()
                    ),
                    radius = 20.dp.toPx(),
                    center = Offset(center.x - eyesAnchor.x.dp.toPx(), center.y + eyesAnchor.y.dp.toPx())
                )
            }
            cheeksItem?.colorHex?.let { hex ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(center.x + cheeksAnchor.x.dp.toPx(), center.y + cheeksAnchor.y.dp.toPx()),
                        radius = 35.dp.toPx()
                    ),
                    radius = 35.dp.toPx(),
                    center = Offset(center.x + cheeksAnchor.x.dp.toPx(), center.y + cheeksAnchor.y.dp.toPx())
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(center.x - cheeksAnchor.x.dp.toPx(), center.y + cheeksAnchor.y.dp.toPx()),
                        radius = 35.dp.toPx()
                    ),
                    radius = 35.dp.toPx(),
                    center = Offset(center.x - cheeksAnchor.x.dp.toPx(), center.y + cheeksAnchor.y.dp.toPx())
                )
            }
            lipsItem?.colorHex?.let { hex ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(center.x + lipsAnchor.x.dp.toPx(), center.y + lipsAnchor.y.dp.toPx()),
                        radius = 25.dp.toPx()
                    ),
                    radius = 25.dp.toPx(),
                    center = Offset(center.x + lipsAnchor.x.dp.toPx(), center.y + lipsAnchor.y.dp.toPx())
                )
            }

            // 4. Draw Static Callout Lines
            val lineStroke = 0.8.dp.toPx()
            val anchorRadius = 2.dp.toPx()
            val lineColor = Color.DarkGray.copy(alpha = 0.4f)

            drawLine(lineColor, Offset(center.x + eyesAnchor.x.dp.toPx(), center.y + eyesAnchor.y.dp.toPx()), Offset(center.x + eyesCallout.x.dp.toPx(), center.y + eyesCallout.y.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, Offset(center.x + eyesAnchor.x.dp.toPx(), center.y + eyesAnchor.y.dp.toPx()))

            drawLine(lineColor, Offset(center.x + cheeksAnchor.x.dp.toPx(), center.y + cheeksAnchor.y.dp.toPx()), Offset(center.x + cheeksCallout.x.dp.toPx(), center.y + cheeksCallout.y.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, Offset(center.x + cheeksAnchor.x.dp.toPx(), center.y + cheeksAnchor.y.dp.toPx()))

            drawLine(lineColor, Offset(center.x + lipsAnchor.x.dp.toPx(), center.y + lipsAnchor.y.dp.toPx()), Offset(center.x + lipsCallout.x.dp.toPx(), center.y + lipsCallout.y.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, Offset(center.x + lipsAnchor.x.dp.toPx(), center.y + lipsAnchor.y.dp.toPx()))
        }

        val eyesItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.EYES }
        val cheeksItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.DIMENSION }
        val lipsItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.LIPS }

        // 5. Render the Callouts
        // --- EYES CALLOUT (Right Side) ---
        BlueprintCallout(
            label = "EYES",
            productName = eyesItem?.name ?: "Pending...",
            colorHex = eyesItem?.colorHex,
            isExpanded = expandedCategory == "EYES",
            onExpandToggle = { expandedCategory = if (expandedCategory == "EYES") null else "EYES" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "EYES") 10f else 1f)
                .offset(
                    x = horizontalShift + eyesCallout.x.dp,
                    y = blueprintOffset + eyesCallout.y.dp
                ),
            anchorAlignment = Alignment.TopStart
        )

        // --- CHEEKS CALLOUT (Left Side) ---
        BlueprintCallout(
            label = "CHEEKS",
            productName = cheeksItem?.name ?: "Pending...",
            colorHex = cheeksItem?.colorHex,
            isExpanded = expandedCategory == "CHEEKS",
            onExpandToggle = { expandedCategory = if (expandedCategory == "CHEEKS") null else "CHEEKS" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "CHEEKS") 10f else 1f)
                .offset(
                    x = horizontalShift + cheeksCallout.x.dp,
                    y = blueprintOffset + cheeksCallout.y.dp
                ),
            anchorAlignment = Alignment.TopEnd
        )

        // --- LIPS CALLOUT (Right Side) ---
        BlueprintCallout(
            label = "LIPS",
            productName = lipsItem?.name ?: "Pending...",
            colorHex = lipsItem?.colorHex,
            isExpanded = expandedCategory == "LIPS",
            onExpandToggle = { expandedCategory = if (expandedCategory == "LIPS") null else "LIPS" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "LIPS") 10f else 1f)
                .offset(
                    x = horizontalShift + lipsCallout.x.dp,
                    y = blueprintOffset + lipsCallout.y.dp
                ),
            anchorAlignment = Alignment.TopStart
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FaceBlueprintViewPreview() {
    FaceBlueprintView(uiState = StyleSimulatorUiState())
}