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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState

@Composable
fun HandBlueprintView(uiState: StyleSimulatorUiState) {
    // SINGLE SOURCE OF TRUTH: Tracks the currently expanded card
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    val blueprintOffset = 10.dp
    val horizontalShift = 0.dp
    
    // Define Unified Offsets
    val nailsAnchor = Offset(60.dp.value, -80.dp.value)
    val nailsCallout = Offset(130.dp.value, -120.dp.value)

    Box(
        modifier = Modifier
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

            // Look in both lists for the "Nail" anchor (Allowing AI creativity)
            val nailsCosmetic = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.NAILS }
            val nailsClothing = uiState.recommendedClothing.find { it.category == ClothingCategory.ACCESSORIES && it.name.contains("nail", ignoreCase = true) }
            
            val nailsHex = nailsCosmetic?.colorHex ?: nailsClothing?.colorHex

            // Nails Shade
            nailsHex?.let { hex ->
                val pigment = parseColor(hex).copy(alpha = 0.5f)
                drawCircle(pigment, radius = 6.dp.toPx(), center = Offset(center.x - 85.dp.toPx(), center.y - 48.dp.toPx())) 
                drawCircle(pigment, radius = 7.dp.toPx(), center = Offset(center.x - 48.dp.toPx(), center.y - 88.dp.toPx())) 
                drawCircle(pigment, radius = 7.dp.toPx(), center = Offset(center.x + 5.dp.toPx(), center.y - 105.dp.toPx()))  
                drawCircle(pigment, radius = 7.dp.toPx(), center = Offset(center.x + 60.dp.toPx(), center.y - 80.dp.toPx())) 
                drawCircle(pigment, radius = 7.dp.toPx(), center = Offset(center.x + 95.dp.toPx(), center.y + 12.dp.toPx())) 
            }

            // Elegant Curved Callout Line
            val start = Offset(center.x + nailsAnchor.x.dp.toPx(), center.y + nailsAnchor.y.dp.toPx())
            val end = Offset(center.x + nailsCallout.x.dp.toPx(), center.y + nailsCallout.y.dp.toPx())
            
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(start.x, start.y)
                quadraticTo(
                    center.x + nailsCallout.x.dp.toPx(), start.y, // Control point
                    end.x, end.y
                )
            }
            
            drawPath(
                path = path,
                color = Color.LightGray,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
            )
        }

        val nailsItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.NAILS }

        BlueprintCallout(
            label = "NAILS",
            productName = nailsItem?.name ?: "Pending...",
            colorHex = nailsItem?.colorHex,
            isExpanded = expandedCategory == "NAILS",
            onExpandToggle = { expandedCategory = if (expandedCategory == "NAILS") null else "NAILS" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "NAILS") 10f else 1f)
                .offset(
                    x = horizontalShift + nailsCallout.x.dp,
                    y = blueprintOffset + nailsCallout.y.dp
                ),
            anchorAlignment = Alignment.TopStart
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HandBlueprintViewPreview() {
    HandBlueprintView(uiState = StyleSimulatorUiState())
}
