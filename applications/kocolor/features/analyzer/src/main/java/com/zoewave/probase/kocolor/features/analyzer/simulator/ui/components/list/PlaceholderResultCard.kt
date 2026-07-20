package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PlaceholderResultCard(label: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(140.dp).alpha(0.5f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier.fillMaxHeight().width(120.dp).background(Color(0xFFF9F9F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory2, null, tint = Color.Black.copy(alpha = 0.05f), modifier = Modifier.size(32.dp))
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.LightGray, fontWeight = FontWeight.Bold)
                Text(text = "Pending Selection", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif, color = Color.LightGray)
                Text(text = "AI Curation in Progress", style = MaterialTheme.typography.bodySmall, color = Color.LightGray.copy(alpha = 0.5f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlaceholderResultCardPreview() {
    PlaceholderResultCard(label = "Eyes")
}
