package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import android.graphics.BlurMaskFilter
import android.graphics.RectF
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.analyzer.R

@Composable
fun ClothingBlueprintView(
    data: VisualBlueprintData,
    modifier: Modifier = Modifier
) {
    // SINGLE SOURCE OF TRUTH: Tracks the currently expanded card
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    val blueprintOffset = 10.dp
    val horizontalShift = 0.dp

    // 🛠️ 2. Corrected Body Anchors (Chest, Thigh, and Feet)
    val topAnchor = Offset(0.dp.value, -60.dp.value)
    val bottomAnchor = Offset(0.dp.value, 30.dp.value)
    val shoesAnchor = Offset(0.dp.value, 145.dp.value)

    // 3. Define Dynamic Callout Targets (End of the lines)
    val topTarget by animateOffsetAsState(
        // Pulls inward to 15f when expanded, rests at 60f when collapsed
        if (expandedCategory == "TOP") Offset(7f, -120f) else Offset(50f, -125f),
        label = "topTarget"
    )
    val bottomTarget by animateOffsetAsState(
        // Pulls inward to -15f when expanded, rests at -60f when collapsed
        if (expandedCategory == "BOTTOM") Offset(-15f, 50f) else Offset(-60f, 60f),
        label = "bottomTarget"
    )
    val shoesTarget by animateOffsetAsState(
        // Pulls inward to 15f when expanded, rests at 60f when collapsed
        if (expandedCategory == "SHOES") Offset(15f, 170f) else Offset(60f, 180f),
        label = "shoesTarget"
    )

    // 4. Animate Width
    val topWidth by animateDpAsState(if (expandedCategory == "TOP") 160.dp else 120.dp, label = "topWidth")
    val bottomWidth by animateDpAsState(if (expandedCategory == "BOTTOM") 160.dp else 120.dp, label = "bottomWidth")
    val shoesWidth by animateDpAsState(if (expandedCategory == "SHOES") 160.dp else 120.dp, label = "shoesWidth")

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Central Silhouette Anchor (Maximum Scale)
        Box(
            modifier = Modifier
                .width(420.dp)
                .fillMaxHeight()
                .offset(x = horizontalShift, y = blueprintOffset)
                .clip(RoundedCornerShape(32.dp))
                .alpha(0.35f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.applications_kocolor_feartues_analyzer_body),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Callout Lines & Shades
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2 + horizontalShift.toPx(), size.height / 2 + blueprintOffset.toPx())

            // 1. Draw "Shades" (Soft Editorial Tints)
            data.topItem?.let { item ->
                val pigment = parseColor(item.colorHex).copy(alpha = 0.28f)
                val topRect = RectF(
                    center.x - 45.dp.toPx(), center.y - 100.dp.toPx(),
                    center.x + 45.dp.toPx(), center.y - 20.dp.toPx()
                )
                drawIntoCanvas { canvas ->
                    val paint = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = pigment.toArgb()
                        maskFilter = BlurMaskFilter(25f.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.nativeCanvas.drawOval(topRect, paint)
                }
            }

            data.bottomItem?.let { item ->
                val pigment = parseColor(item.colorHex).copy(alpha = 0.25f)
                val bottomRect = RectF(
                    center.x - 35.dp.toPx(), center.y + 0.dp.toPx(),
                    center.x + 35.dp.toPx(), center.y + 120.dp.toPx()
                )
                drawIntoCanvas { canvas ->
                    val paint = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = pigment.toArgb()
                        maskFilter = BlurMaskFilter(30f.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.nativeCanvas.drawOval(bottomRect, paint)
                }
            }

            data.shoeItem?.let { item ->
                val pigment = parseColor(item.colorHex).copy(alpha = 0.35f)
                drawCircle(pigment, radius = 12.dp.toPx(), center = Offset(center.x - 12.dp.toPx(), center.y + 145.dp.toPx()))
                drawCircle(pigment, radius = 12.dp.toPx(), center = Offset(center.x + 12.dp.toPx(), center.y + 145.dp.toPx()))
            }

            // 2. Draw Callout Lines (Using animated targets)
            val lineStroke = 0.8.dp.toPx()
            val anchorRadius = 2.dp.toPx()
            val lineColor = Color.DarkGray.copy(alpha = 0.4f)

            // TOP Line
            drawLine(lineColor, Offset(center.x + topAnchor.x.dp.toPx(), center.y + topAnchor.y.dp.toPx()), Offset(center.x + topTarget.x.dp.toPx(), center.y + topTarget.y.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, Offset(center.x + topAnchor.x.dp.toPx(), center.y + topAnchor.y.dp.toPx()))

            // BOTTOM Line
            drawLine(lineColor, Offset(center.x + bottomAnchor.x.dp.toPx(), center.y + bottomAnchor.y.dp.toPx()), Offset(center.x + bottomTarget.x.dp.toPx(), center.y + bottomTarget.y.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, Offset(center.x + bottomAnchor.x.dp.toPx(), center.y + bottomAnchor.y.dp.toPx()))

            // SHOES Line
            drawLine(lineColor, Offset(center.x + shoesAnchor.x.dp.toPx(), center.y + shoesAnchor.y.dp.toPx()), Offset(center.x + shoesTarget.x.dp.toPx(), center.y + shoesTarget.y.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, Offset(center.x + shoesAnchor.x.dp.toPx(), center.y + shoesAnchor.y.dp.toPx()))
        }

        // 🛠️ The established standard half-height from the Face Blueprint
        val calloutHalfHeight = 24.dp

        // --- TOP CALLOUT (Pinned at BottomStart) ---
        BlueprintCallout(
            label = "TOP",
            productName = data.topItem?.name ?: "Pending...",
            colorHex = data.topItem?.colorHex,
            isExpanded = expandedCategory == "TOP",
            onExpandToggle = { expandedCategory = if (expandedCategory == "TOP") null else "TOP" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "TOP") 10f else 1f)
                .width(topWidth)
                .offset(
                    // 🛠️ Fixed: Uses standard Half-Width and Half-Height logic
                    x = horizontalShift + topTarget.x.dp + (topWidth / 2),
                    y = blueprintOffset + topTarget.y.dp - calloutHalfHeight
                ),
            anchorAlignment = Alignment.BottomStart
        )

        // --- BOTTOM CALLOUT (Pinned at TopEnd) ---
        BlueprintCallout(
            label = "BOTTOM",
            productName = data.bottomItem?.name ?: "Pending...",
            colorHex = data.bottomItem?.colorHex,
            isExpanded = expandedCategory == "BOTTOM",
            onExpandToggle = { expandedCategory = if (expandedCategory == "BOTTOM") null else "BOTTOM" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "BOTTOM") 10f else 1f)
                .width(bottomWidth)
                .offset(
                    // 🛠️ Fixed: Subtract HALF the width so it stays on screen
                    x = horizontalShift + bottomTarget.x.dp - (bottomWidth / 2),
                    y = blueprintOffset + bottomTarget.y.dp + calloutHalfHeight
                ),
            anchorAlignment = Alignment.TopEnd
        )

        // --- SHOES CALLOUT (Pinned at TopStart) ---
        BlueprintCallout(
            label = "SHOES",
            productName = data.shoeItem?.name ?: "Pending...",
            colorHex = data.shoeItem?.colorHex,
            isExpanded = expandedCategory == "SHOES",
            onExpandToggle = { expandedCategory = if (expandedCategory == "SHOES") null else "SHOES" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "SHOES") 10f else 1f)
                .width(shoesWidth)
                .offset(
                    // 🛠️ Fixed: Standard Half-Width and Half-Height logic
                    x = horizontalShift + shoesTarget.x.dp + (shoesWidth / 2),
                    y = blueprintOffset + shoesTarget.y.dp + calloutHalfHeight
                ),
            anchorAlignment = Alignment.TopStart
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ClothingBlueprintViewPreview() {
    ClothingBlueprintView(data = VisualBlueprintData())
}
