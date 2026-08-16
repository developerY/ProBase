package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.zoewave.probase.core.ui.util.parseColor
import com.zoewave.probase.kocolor.features.analyzer.R

@Composable
fun HandBlueprintView(
    data: VisualBlueprintData,
    modifier: Modifier = Modifier
) {
    // SINGLE SOURCE OF TRUTH: Tracks the currently expanded card
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    val blueprintOffset = 10.dp
    val horizontalShift = 0.dp
    
    // 2. Define Feature Anchor Points (Start of the lines)
    val nailsAnchor = Offset(-49.dp.value, -59.dp.value) // Ring finger
    val nailsCallout = Offset(-105.dp.value, 119.dp.value)

    // 4. Animate Width
    val nailsWidth by animateDpAsState(if (expandedCategory == "NAILS") 160.dp else 120.dp, label = "nailsWidth")
    val dotCenterOffset = 3.dp // Dot center is 3dp inside the card corner

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Central Hand Anchor
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = horizontalShift, y = blueprintOffset)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f))
        ) {
            Image(
                painter = painterResource(id = R.drawable.applications_kocolor_features_analyzer_hand),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.4f),
                contentScale = ContentScale.Fit
            )
        }

        // Callout Lines & Shades
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2 + horizontalShift.toPx(), size.height / 2 + blueprintOffset.toPx())
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

            val nailsHex = data.nailsItem?.colorHex

            // Calibrated Nail Coordinates
            nailsHex?.let { hex ->
                val pigment = parseColor(hex).copy(alpha = 0.5f)

                // Leftmost finger (Thumb)
                drawCircle(pigment, radius = 9.dp.toPx(), center = Offset(center.x + (-20).dp.toPx(), center.y + (-74).dp.toPx()))

                // Mid-Left finger (Index)
                drawCircle(pigment, radius = 7.dp.toPx(), center = Offset(center.x + (13).dp.toPx(), center.y + (-132).dp.toPx()))

                // Top finger (Middle)
                drawCircle(pigment, radius = 7.dp.toPx(), center = Offset(center.x + (-29).dp.toPx(), center.y + (-113).dp.toPx()))

                // Mid-Right finger (Ring)
                drawCircle(pigment, radius = 7.dp.toPx(), center = Offset(center.x + (-49).dp.toPx(), center.y + (-70).dp.toPx()))

                // Bottom-Right edge (Pinky)
                drawCircle(pigment, radius = 7.dp.toPx(), center = Offset(center.x + (-44).dp.toPx(), center.y + (-37).dp.toPx()))
            }

            // Elegant Curved Callout Line
            val start = Offset(center.x + nailsAnchor.x.dp.toPx(), center.y + nailsAnchor.y.dp.toPx())
            val end = Offset(center.x + nailsCallout.x.dp.toPx(), center.y + nailsCallout.y.dp.toPx())

            val path = Path().apply {
                moveTo(start.x, start.y)
                quadraticTo(
                    center.x + nailsCallout.x.dp.toPx(), start.y, // Control point
                    end.x, end.y
                )
            }

            drawPath(
                path = path,
                color = Color.LightGray,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
            )
        }

        BlueprintCallout(
            label = "NAILS",
            productName = data.nailsItem?.name ?: "Pending...",
            colorHex = data.nailsItem?.colorHex,
            isExpanded = expandedCategory == "NAILS",
            onExpandToggle = { expandedCategory = if (expandedCategory == "NAILS") null else "NAILS" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "NAILS") 10f else 1f)
                .width(nailsWidth)
                .offset(
                    // Pin TopStart (Top-Left) dot to the line
                    x = horizontalShift + nailsCallout.x.dp - dotCenterOffset,
                    y = blueprintOffset + nailsCallout.y.dp - dotCenterOffset
                ),
            anchorAlignment = Alignment.TopStart
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HandBlueprintViewPreview() {
    HandBlueprintView(data = VisualBlueprintData())
}
