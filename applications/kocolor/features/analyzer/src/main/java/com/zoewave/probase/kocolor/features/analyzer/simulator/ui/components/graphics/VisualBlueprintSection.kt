package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor

@Composable
fun VisualBlueprintSection(
    data: VisualBlueprintData,
    modifier: Modifier = Modifier,
    initialTab: ResultTab = ResultTab.FACE,
    onTabSelected: (ResultTab) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    
    val displayPalette = remember(selectedTab, data.recommendedPalette) {
        when (selectedTab) {
            ResultTab.FACE -> {
                listOfNotNull(
                    data.eyesItem?.colorHex,
                    data.cheeksItem?.colorHex,
                    data.lipsItem?.colorHex,
                    data.recommendedPalette.getOrNull(0) ?: "#FFFFFF"
                ).distinct().take(4)
            }
            ResultTab.CLOTHES -> {
                listOfNotNull(
                    data.topItem?.colorHex,
                    data.bottomItem?.colorHex,
                    data.shoeItem?.colorHex,
                    data.recommendedPalette.getOrNull(3) ?: "#000000"
                ).distinct().take(4)
            }
            ResultTab.NAILS -> {
                listOfNotNull(
                    data.nailsItem?.colorHex,
                    data.lipsItem?.colorHex,
                    data.topItem?.colorHex,
                    data.recommendedPalette.getOrNull(0) ?: "#FFFFFF"
                ).distinct().take(4)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(520.dp)
            .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(32.dp))
    ) {
        // Left Column: KoColor Sidebar
        Column(
            modifier = Modifier
                .width(72.dp)
                .fillMaxHeight()
                .padding(vertical = 20.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "KoColor",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    ),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                displayPalette.forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(parseColor(hex))
                            .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // Right Column: Blueprint View
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            when (selectedTab) {
                ResultTab.FACE -> FaceBlueprintView(data)
                ResultTab.CLOTHES -> ClothingBlueprintView(data)
                ResultTab.NAILS -> HandBlueprintView(data)
            }

            ResultTabToggle(
                selectedTab = selectedTab,
                onTabSelected = { 
                    selectedTab = it
                    onTabSelected(it)
                },
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
            )
        }
    }
}
