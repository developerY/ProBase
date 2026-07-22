package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

    // 1. Global Layout Shifts
    val blueprintOffset = 10.dp
    val horizontalShift = 15.dp

    // 2. Define Dynamic Callout Targets (End of the lines)
    // Eyes and Lips move TOWARD center when expanded so they remain fully visible.
    val eyesTarget by animateOffsetAsState(
        if (expandedCategory == "EYES") Offset(60f, -100f) else Offset(90f, -90f),
        label = "eyesTarget"
    )
    val cheeksTarget by animateOffsetAsState(
        // Cheeks move slight UP and LEFT when open.
        if (expandedCategory == "CHEEKS") Offset(-100f, 80f) else Offset(-80f, 120f),
        label = "cheeksTarget"
    )
    val lipsTarget by animateOffsetAsState(
        if (expandedCategory == "LIPS") Offset(50f, 170f) else Offset(80f, 150f),
        label = "lipsTarget"
    )

    // 3. Animate Width
    val eyesWidth by animateDpAsState(if (expandedCategory == "EYES") 160.dp else 120.dp, label = "eyesWidth")
    val cheeksWidth by animateDpAsState(if (expandedCategory == "CHEEKS") 160.dp else 120.dp, label = "cheeksWidth")
    val lipsWidth by animateDpAsState(if (expandedCategory == "LIPS") 160.dp else 120.dp, label = "lipsWidth")

    // Feature Anchor Points (Start of the lines)
    val eyesAnchor = Offset(35.dp.value, -45.dp.value)
    val cheeksAnchor = Offset(-45.dp.value, 25.dp.value)
    val lipsAnchor = Offset(0.dp.value, 75.dp.value)

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
            eyesItem?.colorHex?.let { hex ->
                val pigment = parseColor(hex).copy(alpha = 0.35f)
                drawCircle(Brush.radialGradient(listOf(pigment, Color.Transparent), center = Offset(center.x + eyesAnchor.x.dp.toPx(), center.y + eyesAnchor.y.dp.toPx()), radius = 20.dp.toPx()), radius = 20.dp.toPx(), center = Offset(center.x + eyesAnchor.x.dp.toPx(), center.y + eyesAnchor.y.dp.toPx()))
                drawCircle(Brush.radialGradient(listOf(pigment, Color.Transparent), center = Offset(center.x - eyesAnchor.x.dp.toPx(), center.y + eyesAnchor.y.dp.toPx()), radius = 20.dp.toPx()), radius = 20.dp.toPx(), center = Offset(center.x - eyesAnchor.x.dp.toPx(), center.y + eyesAnchor.y.dp.toPx()))
            }
            cheeksItem?.colorHex?.let { hex ->
                val pigment = parseColor(hex).copy(alpha = 0.3f)
                drawCircle(Brush.radialGradient(listOf(pigment, Color.Transparent), center = Offset(center.x + cheeksAnchor.x.dp.toPx(), center.y + cheeksAnchor.y.dp.toPx()), radius = 35.dp.toPx()), radius = 35.dp.toPx(), center = Offset(center.x + cheeksAnchor.x.dp.toPx(), center.y + cheeksAnchor.y.dp.toPx()))
                drawCircle(Brush.radialGradient(listOf(pigment, Color.Transparent), center = Offset(center.x - cheeksAnchor.x.dp.toPx(), center.y + cheeksAnchor.y.dp.toPx()), radius = 35.dp.toPx()), radius = 35.dp.toPx(), center = Offset(center.x - cheeksAnchor.x.dp.toPx(), center.y + cheeksAnchor.y.dp.toPx()))
            }
            lipsItem?.colorHex?.let { hex ->
                val pigment = parseColor(hex).copy(alpha = 0.4f)
                drawCircle(Brush.radialGradient(listOf(pigment, Color.Transparent), center = Offset(center.x + lipsAnchor.x.dp.toPx(), center.y + lipsAnchor.y.dp.toPx()), radius = 25.dp.toPx()), radius = 25.dp.toPx(), center = Offset(center.x + lipsAnchor.x.dp.toPx(), center.y + lipsAnchor.y.dp.toPx()))
            }

            // 2. Draw Callout Lines (Using animated targets)
            val lineStroke = 0.8.dp.toPx()
            val anchorRadius = 2.dp.toPx()
            val lineColor = Color.DarkGray.copy(alpha = 0.4f)

            // EYES Line
            drawLine(lineColor, Offset(center.x + eyesAnchor.x.dp.toPx(), center.y + eyesAnchor.y.dp.toPx()), Offset(center.x + eyesTarget.x.dp.toPx(), center.y + eyesTarget.y.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, Offset(center.x + eyesAnchor.x.dp.toPx(), center.y + eyesAnchor.y.dp.toPx()))

            // CHEEKS Line
            drawLine(lineColor, Offset(center.x + cheeksAnchor.x.dp.toPx(), center.y + cheeksAnchor.y.dp.toPx()), Offset(center.x + cheeksTarget.x.dp.toPx(), center.y + cheeksTarget.y.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, Offset(center.x + cheeksAnchor.x.dp.toPx(), center.y + cheeksAnchor.y.dp.toPx()))

            // LIPS Line
            drawLine(lineColor, Offset(center.x + lipsAnchor.x.dp.toPx(), center.y + lipsAnchor.y.dp.toPx()), Offset(center.x + lipsTarget.x.dp.toPx(), center.y + lipsTarget.y.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, Offset(center.x + lipsAnchor.x.dp.toPx(), center.y + lipsAnchor.y.dp.toPx()))
        }

        val eyesItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.EYES }
        val cheeksItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.DIMENSION }
        val lipsItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.LIPS }

        // 5. Render the Callouts
        val calloutHalfHeight = 24.dp // Pushes the card down so the top-corner dot hits the line

        // --- EYES CALLOUT (Right Side) ---
        BlueprintCallout(
            label = "EYES",
            productName = eyesItem?.name ?: "Pending...",
            colorHex = eyesItem?.colorHex,
            isExpanded = expandedCategory == "EYES",
            onExpandToggle = { expandedCategory = if (expandedCategory == "EYES") null else "EYES" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "EYES") 10f else 1f)
                .width(eyesWidth)
                .offset(
                    // Add half-width to pin the TopStart (Left) corner dot to the line
                    x = horizontalShift + eyesTarget.x.dp + (eyesWidth / 2),
                    y = blueprintOffset + eyesTarget.y.dp + calloutHalfHeight
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
                .width(cheeksWidth)
                .offset(
                    // Subtract half-width to pin the TopEnd (Right) corner dot to the line
                    x = horizontalShift + cheeksTarget.x.dp - (cheeksWidth / 2), 
                    y = blueprintOffset + cheeksTarget.y.dp + calloutHalfHeight
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
                .width(lipsWidth)
                .offset(
                    // Add half-width to pin the TopStart (Left) corner dot to the line
                    x = horizontalShift + lipsTarget.x.dp + (lipsWidth / 2),
                    y = blueprintOffset + lipsTarget.y.dp + calloutHalfHeight
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
