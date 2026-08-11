package com.zoewave.probase.core.ui.intelligence

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IntelligenceChip(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFF5F5F5),
    contentColor: Color = Color.Gray,
    borderColor: Color = Color.Transparent
) {
    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(6.dp))
            .then(if (borderColor != Color.Transparent) Modifier.border(0.5.dp, borderColor, RoundedCornerShape(6.dp)) else Modifier)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = contentColor,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}
