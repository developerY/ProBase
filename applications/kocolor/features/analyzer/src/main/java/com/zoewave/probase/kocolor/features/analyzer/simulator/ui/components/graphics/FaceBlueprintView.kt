package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState

@Composable
fun FaceBlueprintView(uiState: StyleSimulatorUiState) {
    val blueprintOffset = 10.dp
    val horizontalShift = 0.dp
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Central Face Anchor (Always use Line-Art for Blueprint feel)
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
            
            // 1. Draw "Shades" (Soft Glows on the face)
            val eyesItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.EYES }
            val cheeksItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.DIMENSION }
            val lipsItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.LIPS }

            // Eyes Shade
            eyesItem?.colorHex?.let { hex ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(center.x - 35.dp.toPx(), center.y - 45.dp.toPx()),
                        radius = 20.dp.toPx()
                    ),
                    radius = 20.dp.toPx(),
                    center = Offset(center.x - 35.dp.toPx(), center.y - 45.dp.toPx())
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(center.x + 35.dp.toPx(), center.y - 45.dp.toPx()),
                        radius = 20.dp.toPx()
                    ),
                    radius = 20.dp.toPx(),
                    center = Offset(center.x + 35.dp.toPx(), center.y - 45.dp.toPx())
                )
            }

            // Cheeks Shade
            cheeksItem?.colorHex?.let { hex ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(center.x - 45.dp.toPx(), center.y + 25.dp.toPx()),
                        radius = 35.dp.toPx()
                    ),
                    radius = 35.dp.toPx(),
                    center = Offset(center.x - 45.dp.toPx(), center.y + 25.dp.toPx())
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(center.x + 45.dp.toPx(), center.y + 25.dp.toPx()),
                        radius = 35.dp.toPx()
                    ),
                    radius = 35.dp.toPx(),
                    center = Offset(center.x + 45.dp.toPx(), center.y + 25.dp.toPx())
                )
            }

            // Lips Shade
            lipsItem?.colorHex?.let { hex ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(center.x, center.y + 75.dp.toPx()),
                        radius = 25.dp.toPx()
                    ),
                    radius = 25.dp.toPx(),
                    center = Offset(center.x, center.y + 75.dp.toPx())
                )
            }

            // 2. Draw Callout Lines (Solid with anchor dots)
            val lineStroke = 0.8.dp.toPx()
            val anchorRadius = 2.dp.toPx()
            val lineColor = Color.DarkGray.copy(alpha = 0.4f)

            // Eyes (Top Left)
            val eyesStart = Offset(center.x - 30.dp.toPx(), center.y - 45.dp.toPx())
            drawLine(lineColor, eyesStart, Offset(center.x - 120.dp.toPx(), center.y - 45.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, eyesStart)

            // Cheeks (Mid Left)
            val cheeksStart = Offset(center.x - 45.dp.toPx(), center.y + 20.dp.toPx())
            drawLine(lineColor, cheeksStart, Offset(center.x - 140.dp.toPx(), center.y + 20.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, cheeksStart)

            // Lips (Bottom Right)
            val lipsStart = Offset(center.x, center.y + 70.dp.toPx())
            drawLine(lineColor, lipsStart, Offset(center.x + 100.dp.toPx(), center.y + 160.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, lipsStart)
        }

        val eyesItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.EYES }
        val cheeksItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.DIMENSION }
        val lipsItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.LIPS }

        BlueprintCallout(
            label = "EYES",
            productName = eyesItem?.name ?: "Pending...",
            colorHex = eyesItem?.colorHex,
            modifier = Modifier.align(Alignment.TopStart).padding(top = 80.dp).offset(y = blueprintOffset)
        )

        BlueprintCallout(
            label = "CHEEKS",
            productName = cheeksItem?.name ?: "Pending...",
            colorHex = cheeksItem?.colorHex,
            modifier = Modifier.align(Alignment.CenterStart).padding(top = 220.dp, start = 5.dp).offset(y = blueprintOffset)
        )

        BlueprintCallout(
            label = "LIPS",
            productName = lipsItem?.name ?: "Pending...",
            colorHex = lipsItem?.colorHex,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 0.dp, end = 5.dp).offset(y = blueprintOffset - 10.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FaceBlueprintViewPreview() {
    FaceBlueprintView(uiState = StyleSimulatorUiState())
}
