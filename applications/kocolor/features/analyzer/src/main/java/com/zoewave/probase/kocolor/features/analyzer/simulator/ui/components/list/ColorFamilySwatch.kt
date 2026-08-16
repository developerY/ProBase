package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.model.ritual.ColorFamily
import com.zoewave.probase.core.ui.util.parseColor

@Composable
fun ColorFamilySwatch(
    family: ColorFamily,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(parseColor(family.hex))
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Black.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(16.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ColorFamilySwatchPreview() {
    ColorFamilySwatch(
        family = ColorFamily.TRUE_RED,
        isSelected = true,
        onClick = {}
    )
}
