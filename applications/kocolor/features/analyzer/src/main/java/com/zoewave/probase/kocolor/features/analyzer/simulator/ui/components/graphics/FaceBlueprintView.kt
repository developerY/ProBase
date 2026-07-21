package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

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
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState

@Composable
fun FaceBlueprintView(uiState: StyleSimulatorUiState) {


    // 1. Shift the entire face right to avoid the color palette
    val blueprintOffset = 10.dp
    val horizontalShift = 20.dp // Added a 20dp shift away from the left edge

    // 2. Tighten the Unified Offsets (Closer to the face)
    val eyesAnchor = Offset(-35.dp.value, -45.dp.value)
    val eyesCallout = Offset(-60.dp.value, -100.dp.value) // Brought in and pushed up

    val cheeksAnchor = Offset(-45.dp.value, 25.dp.value)
    val cheeksCallout = Offset(-70.dp.value, 90.dp.value) // Brought in and pushed down

    val lipsAnchor = Offset(0.dp.value, 75.dp.value)
    val lipsCallout = Offset(60.dp.value, 150.dp.value) // Brought in and pushed down
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
            val center = Offset(
                size.width / 2 + horizontalShift.toPx(),
                size.height / 2 + blueprintOffset.toPx()
            )

            // 1. Draw "Shades" (Soft Glows on the face)
            val eyesItem =
                uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.EYES }
            val cheeksItem =
                uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.DIMENSION }
            val lipsItem =
                uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.LIPS }

            // Eyes Shade
            eyesItem?.colorHex?.let { hex ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(
                            center.x + eyesAnchor.x.dp.toPx(),
                            center.y + eyesAnchor.y.dp.toPx()
                        ),
                        radius = 20.dp.toPx()
                    ),
                    radius = 20.dp.toPx(),
                    center = Offset(
                        center.x + eyesAnchor.x.dp.toPx(),
                        center.y + eyesAnchor.y.dp.toPx()
                    )
                )
                // Right Eye
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(
                            center.x - eyesAnchor.x.dp.toPx(),
                            center.y + eyesAnchor.y.dp.toPx()
                        ),
                        radius = 20.dp.toPx()
                    ),
                    radius = 20.dp.toPx(),
                    center = Offset(
                        center.x - eyesAnchor.x.dp.toPx(),
                        center.y + eyesAnchor.y.dp.toPx()
                    )
                )
            }

            // Cheeks Shade
            cheeksItem?.colorHex?.let { hex ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(
                            center.x + cheeksAnchor.x.dp.toPx(),
                            center.y + cheeksAnchor.y.dp.toPx()
                        ),
                        radius = 35.dp.toPx()
                    ),
                    radius = 35.dp.toPx(),
                    center = Offset(
                        center.x + cheeksAnchor.x.dp.toPx(),
                        center.y + cheeksAnchor.y.dp.toPx()
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(
                            center.x - cheeksAnchor.x.dp.toPx(),
                            center.y + cheeksAnchor.y.dp.toPx()
                        ),
                        radius = 35.dp.toPx()
                    ),
                    radius = 35.dp.toPx(),
                    center = Offset(
                        center.x - cheeksAnchor.x.dp.toPx(),
                        center.y + cheeksAnchor.y.dp.toPx()
                    )
                )
            }

            // Lips Shade
            lipsItem?.colorHex?.let { hex ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(
                            center.x + lipsAnchor.x.dp.toPx(),
                            center.y + lipsAnchor.y.dp.toPx()
                        ),
                        radius = 25.dp.toPx()
                    ),
                    radius = 25.dp.toPx(),
                    center = Offset(
                        center.x + lipsAnchor.x.dp.toPx(),
                        center.y + lipsAnchor.y.dp.toPx()
                    )
                )
            }

            // 2. Draw Callout Lines (Solid with anchor dots)
            val lineStroke = 0.8.dp.toPx()
            val anchorRadius = 2.dp.toPx()
            val lineColor = Color.DarkGray.copy(alpha = 0.4f)

            // Eyes (Top Left)
            val eyesStart =
                Offset(center.x + eyesAnchor.x.dp.toPx(), center.y + eyesAnchor.y.dp.toPx())
            val eyesEnd =
                Offset(center.x + eyesCallout.x.dp.toPx(), center.y + eyesCallout.y.dp.toPx())
            drawLine(lineColor, eyesStart, eyesEnd, lineStroke)
            drawCircle(lineColor, anchorRadius, eyesStart)

            // Cheeks (Mid Left)
            val cheeksStart =
                Offset(center.x + cheeksAnchor.x.dp.toPx(), center.y + cheeksAnchor.y.dp.toPx())
            val cheeksEnd =
                Offset(center.x + cheeksCallout.x.dp.toPx(), center.y + cheeksCallout.y.dp.toPx())
            drawLine(lineColor, cheeksStart, cheeksEnd, lineStroke)
            drawCircle(lineColor, anchorRadius, cheeksStart)

            // Lips (Bottom Right)
            val lipsStart =
                Offset(center.x + lipsAnchor.x.dp.toPx(), center.y + lipsAnchor.y.dp.toPx())
            val lipsEnd =
                Offset(center.x + lipsCallout.x.dp.toPx(), center.y + lipsCallout.y.dp.toPx())
            drawLine(lineColor, lipsStart, lipsEnd, lineStroke)
            drawCircle(lineColor, anchorRadius, lipsStart)
        }

        val eyesItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.EYES }
        val cheeksItem =
            uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.DIMENSION }
        val lipsItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.LIPS }

        // 3. Deterministic Alignment Math
        // By locking the width of the callout, we force long text to wrap,
        // preventing UI collisions. It also makes our offset math perfectly accurate.
        val calloutWidth = 130.dp
        val calloutHalfWidth = calloutWidth / 2
        val calloutHalfHeight = 24.dp // Estimated half-height for wrapping text

        BlueprintCallout(
            label = "EYES",
            productName = eyesItem?.name ?: "Pending...",
            colorHex = eyesItem?.colorHex,
            modifier = Modifier
                .width(calloutWidth) // Strict constraint for text wrapping
                .offset(
                    x = horizontalShift + eyesCallout.x.dp - calloutHalfWidth,
                    y = blueprintOffset + eyesCallout.y.dp + calloutHalfHeight
                ),
            anchorAlignment = Alignment.TopEnd
        )

        BlueprintCallout(
            label = "CHEEKS",
            productName = cheeksItem?.name ?: "Pending...",
            colorHex = cheeksItem?.colorHex,
            modifier = Modifier
                .width(calloutWidth)
                .offset(
                    x = horizontalShift + cheeksCallout.x.dp - calloutHalfWidth,
                    y = blueprintOffset + cheeksCallout.y.dp + calloutHalfHeight
                ),
            anchorAlignment = Alignment.TopEnd
        )

        BlueprintCallout(
            label = "LIPS",
            productName = lipsItem?.name ?: "Pending...",
            colorHex = lipsItem?.colorHex,
            modifier = Modifier
                .width(calloutWidth)
                .offset(
                    x = horizontalShift + lipsCallout.x.dp + calloutHalfWidth,
                    y = blueprintOffset + lipsCallout.y.dp + calloutHalfHeight
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
