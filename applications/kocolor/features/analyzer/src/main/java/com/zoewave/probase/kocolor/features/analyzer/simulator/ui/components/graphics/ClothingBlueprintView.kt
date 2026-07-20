package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState

@Composable
fun ClothingBlueprintView(uiState: StyleSimulatorUiState) {
    val blueprintOffset = 10.dp
    val horizontalShift = 0.dp
    
    Box(
        modifier = Modifier
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
            val topStart = Offset(center.x + 10.dp.toPx(), center.y - 120.dp.toPx())
            drawLine(lineColor, topStart, Offset(center.x + 120.dp.toPx(), center.y - 140.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, topStart)

            // BOTTOM (Left)
            val bottomStart = Offset(center.x - 10.dp.toPx(), center.y + 20.dp.toPx())
            drawLine(lineColor, bottomStart, Offset(center.x - 120.dp.toPx(), center.y + 60.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, bottomStart)

            // SHOES (Right)
            val shoesStart = Offset(center.x + 10.dp.toPx(), center.y + 180.dp.toPx())
            drawLine(lineColor, shoesStart, Offset(center.x + 120.dp.toPx(), center.y + 200.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, shoesStart)
        }

        val topItem = uiState.recommendedClothing.find { it.category == ClothingCategory.TOPS }
        val bottomItem = uiState.recommendedClothing.find { it.category == ClothingCategory.BOTTOMS }
        val shoeItem = uiState.recommendedClothing.find { it.category == ClothingCategory.SHOES }

        BlueprintCallout(
            label = "TOP",
            productName = topItem?.name ?: "Pending...",
            colorHex = topItem?.colorHex,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 10.dp).offset(y = blueprintOffset)
        )

        BlueprintCallout(
            label = "BOTTOM",
            productName = bottomItem?.name ?: "Pending...",
            colorHex = bottomItem?.colorHex,
            modifier = Modifier.align(Alignment.CenterStart).padding(top = 80.dp, start = 5.dp).offset(y = blueprintOffset)
        )

        BlueprintCallout(
            label = "SHOES",
            productName = shoeItem?.name ?: "Pending...",
            colorHex = shoeItem?.colorHex,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 10.dp).offset(y = blueprintOffset)
        )
    }
}
