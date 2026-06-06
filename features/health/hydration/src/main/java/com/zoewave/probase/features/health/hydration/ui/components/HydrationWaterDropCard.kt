package com.zoewave.probase.features.health.hydration.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HydrationWaterDropCard(
    currentLiters: Double,
    targetLiters: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (currentLiters / targetLiters).toFloat().coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3).copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Hydration",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF2C2420).copy(alpha = 0.7f)
            )

            // Optimized Water Drop for Summary View
            WaterDropVisual(
                progress = progress,
                modifier = Modifier.size(160.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%.1fL".format(currentLiters),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "of %.1fL goal".format(targetLiters),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
