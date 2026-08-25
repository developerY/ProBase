package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.ui.util.parseColor

@Composable
fun BlueprintCallout(
    label: String,
    productName: String,
    colorHex: String?,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier,
    anchorAlignment: Alignment = Alignment.TopEnd
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .width(if (isExpanded) 160.dp else 120.dp)
                .clickable { onExpandToggle() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 12.dp else 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = Color.Gray.copy(alpha = 0.6f)
                )

                if (isExpanded) {
                    Text(
                        text = productName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 16.sp
                        ),
                        color = Color.Black,
                        maxLines = 2
                    )

                    // Color Ribbon (All about the color)
                    if (colorHex != null) {
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            parseColor(colorHex),
                                            parseColor(colorHex).copy(alpha = 0.7f),
                                            parseColor(colorHex).copy(alpha = 0.9f)
                                        )
                                    )
                                )
                                .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                        )
                    }
                } else {
                    Text(
                        text = productName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Serif
                        ),
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Color Indicator (The "Anchor Dot")
        AnchorDot(
            colorHex = colorHex,
            isExpanded = isExpanded,
            anchorAlignment = anchorAlignment,
            modifier = Modifier.align(anchorAlignment) // Uses the BoxScope from BlueprintCallout
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BlueprintCalloutPreview() {
    var expanded by remember { mutableStateOf(false) }
    BlueprintCallout(
        label = "EYES",
        productName = "Midnight Mascara",
        colorHex = "#2C3E50",
        isExpanded = expanded,
        onExpandToggle = { expanded = !expanded }
    )
}
