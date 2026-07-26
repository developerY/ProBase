package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.zoewave.probase.kocolor.features.analyzer.R

@Composable
fun ClothingBlueprintView(
    data: VisualBlueprintData,
    modifier: Modifier = Modifier
) {
    // SINGLE SOURCE OF TRUTH: Tracks the currently expanded card
    var expandedCategory by remember { mutableStateOf<String?>(null) }
    var lastTapCoords by remember { mutableStateOf<String?>(null) }

    val blueprintOffset = 10.dp
    val horizontalShift = 0.dp
    
    // 2. Define Feature Anchor Points (Start of the lines)
    val topAnchor = Offset(10.dp.value, -120.dp.value)
    val bottomAnchor = Offset(-10.dp.value, 20.dp.value)
    val shoesAnchor = Offset(10.dp.value, 180.dp.value)

    // 3. Define Dynamic Callout Targets (End of the lines)
    val topTarget by animateOffsetAsState(
        if (expandedCategory == "TOP") Offset(70f, -170f) else Offset(90f, -150f),
        label = "topTarget"
    )
    val bottomTarget by animateOffsetAsState(
        if (expandedCategory == "BOTTOM") Offset(-80f, 40f) else Offset(-100f, 60f),
        label = "bottomTarget"
    )
    val shoesTarget by animateOffsetAsState(
        if (expandedCategory == "SHOES") Offset(80f, 180f) else Offset(100f, 200f),
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

        // Callout Lines
        val localDensity = LocalDensity.current
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f + horizontalShift.toPx()
                        val centerY = size.height / 2f + blueprintOffset.toPx()
                        with(localDensity) {
                            val dpX = (tapOffset.x - centerX).toDp().value.toInt()
                            val dpY = (tapOffset.y - centerY).toDp().value.toInt()
                            
                            lastTapCoords = "X: $dpX, Y: $dpY"
                            
                            Log.d("BlueprintCalibration", "--- BODY TAP DETECTED ---")
                            Log.d("BlueprintCalibration", "Anchor Point: Offset(${dpX}.dp.value, ${dpY}.dp.value)")
                            Log.d("BlueprintCalibration", "Target Point: Offset(${dpX}f, ${dpY}f)")
                        }
                    }
                }
        ) {
            val center = Offset(size.width / 2 + horizontalShift.toPx(), size.height / 2 + blueprintOffset.toPx())
            
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

            // Debug Markers
            val markerColor = Color.Red.copy(alpha = 0.5f)
            val markerRadius = 3.dp.toPx()
            listOf(topAnchor, bottomAnchor, shoesAnchor).forEach { point ->
                drawCircle(markerColor, markerRadius, Offset(center.x + point.x.dp.toPx(), center.y + point.y.dp.toPx()))
            }
        }

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
                    x = horizontalShift + topTarget.x.dp + 6.dp,
                    y = blueprintOffset + topTarget.y.dp - (if (expandedCategory == "TOP") 80.dp else 48.dp) - 6.dp
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
                    x = horizontalShift + bottomTarget.x.dp - bottomWidth - 6.dp,
                    y = blueprintOffset + bottomTarget.y.dp + 6.dp
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
                    x = horizontalShift + shoesTarget.x.dp + 6.dp,
                    y = blueprintOffset + shoesTarget.y.dp + 6.dp
                ),
            anchorAlignment = Alignment.TopStart
        )

        // Live Coordinate Overlay
        lastTapCoords?.let { coords ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = coords,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClothingBlueprintViewPreview() {
    ClothingBlueprintView(data = VisualBlueprintData())
}
