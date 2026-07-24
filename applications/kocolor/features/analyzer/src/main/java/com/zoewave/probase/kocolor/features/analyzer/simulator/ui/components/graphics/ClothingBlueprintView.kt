package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

    val blueprintOffset = 10.dp
    val horizontalShift = 0.dp
    
    // Define Unified Offsets
    val topAnchor = Offset(10.dp.value, -120.dp.value)
    val topCallout = Offset(130.dp.value, -140.dp.value)
    
    val bottomAnchor = Offset(-10.dp.value, 20.dp.value)
    val bottomCallout = Offset(-150.dp.value, 60.dp.value)
    
    val shoesAnchor = Offset(10.dp.value, 180.dp.value)
    val shoesCallout = Offset(130.dp.value, 200.dp.value)

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
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2 + horizontalShift.toPx(), size.height / 2 + blueprintOffset.toPx())
            
            val lineStroke = 0.8.dp.toPx()
            val anchorRadius = 2.dp.toPx()
            val lineColor = Color.DarkGray.copy(alpha = 0.4f)

            // TOP (Right)
            val topStart = Offset(center.x + topAnchor.x.dp.toPx(), center.y + topAnchor.y.dp.toPx())
            val topEnd = Offset(center.x + topCallout.x.dp.toPx(), center.y + topCallout.y.dp.toPx())
            drawLine(lineColor, topStart, topEnd, lineStroke)
            drawCircle(lineColor, anchorRadius, topStart)

            // BOTTOM (Left)
            val bottomStart = Offset(center.x + bottomAnchor.x.dp.toPx(), center.y + bottomAnchor.y.dp.toPx())
            val bottomEnd = Offset(center.x + bottomCallout.x.dp.toPx(), center.y + bottomCallout.y.dp.toPx())
            drawLine(lineColor, bottomStart, bottomEnd, lineStroke)
            drawCircle(lineColor, anchorRadius, bottomStart)

            // SHOES (Right)
            val shoesStart = Offset(center.x + shoesAnchor.x.dp.toPx(), center.y + shoesAnchor.y.dp.toPx())
            val shoesEnd = Offset(center.x + shoesCallout.x.dp.toPx(), center.y + shoesCallout.y.dp.toPx())
            drawLine(lineColor, shoesStart, shoesEnd, lineStroke)
            drawCircle(lineColor, anchorRadius, shoesStart)
        }

        BlueprintCallout(
            label = "TOP",
            productName = data.topItem?.name ?: "Pending...",
            colorHex = data.topItem?.colorHex,
            isExpanded = expandedCategory == "TOP",
            onExpandToggle = { expandedCategory = if (expandedCategory == "TOP") null else "TOP" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "TOP") 10f else 1f)
                .offset(
                    x = horizontalShift + topCallout.x.dp,
                    y = blueprintOffset + topCallout.y.dp
                ),
            anchorAlignment = Alignment.TopStart
        )

        BlueprintCallout(
            label = "BOTTOM",
            productName = data.bottomItem?.name ?: "Pending...",
            colorHex = data.bottomItem?.colorHex,
            isExpanded = expandedCategory == "BOTTOM",
            onExpandToggle = { expandedCategory = if (expandedCategory == "BOTTOM") null else "BOTTOM" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "BOTTOM") 10f else 1f)
                .offset(
                    x = horizontalShift + bottomCallout.x.dp - 120.dp - 6.dp,
                    y = blueprintOffset + bottomCallout.y.dp + 6.dp
                ),
            anchorAlignment = Alignment.TopEnd
        )

        BlueprintCallout(
            label = "SHOES",
            productName = data.shoeItem?.name ?: "Pending...",
            colorHex = data.shoeItem?.colorHex,
            isExpanded = expandedCategory == "SHOES",
            onExpandToggle = { expandedCategory = if (expandedCategory == "SHOES") null else "SHOES" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "SHOES") 10f else 1f)
                .offset(
                    x = horizontalShift + shoesCallout.x.dp,
                    y = blueprintOffset + shoesCallout.y.dp
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
