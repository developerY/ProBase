package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.mobile.features.home.ui.CuratedCollectionUiState
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun CuratedCollectionCard(
    uiState: CuratedCollectionUiState,
    modifier: Modifier = Modifier,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val analysis = uiState.analysis
    val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, yyyy - HH:mm", java.util.Locale.getDefault()) }
    val dateStr = dateFormat.format(java.util.Date(analysis.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onEvent() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray.copy(alpha = 0.6f)
                )

                // Seasonal Badge
                Surface(
                    color = Color(0xFFF3E5F5), // Light Lavender
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = analysis.advice.seasonalType.name.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF745E7A),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = analysis.advice.title ?: "The Personal Collection",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Local Architect: ${analysis.advice.summary}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                analysis.advice.recommendedPalette.take(4).forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(parseColor(hex))
                            .border(1.dp, Color.Black.copy(alpha = 0.05f), CircleShape)
                    )
                }
            }
        }
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
    } catch (e: Exception) {
        Color.Gray
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Curated Collection Card Preview")
@Composable
private fun CuratedCollectionCardPreview() {
    MaterialTheme {
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(16.dp)) {
            val dummyAnalysis = com.zoewave.probase.core.model.ritual.SavedAnalysis(
                id = 1L,
                timestamp = System.currentTimeMillis(),
                advice = com.zoewave.probase.core.model.ritual.FashionAdvice(
                    title = "The Autumnal Neutral Capsule",
                    summary = "Combines warm earthy tones with structured silhouettes for daytime elegance.",
                    seasonalType = com.zoewave.probase.core.model.ritual.SeasonalType.AUTUMN,
                    undertone = com.zoewave.probase.core.model.ritual.Undertone.WARM,
                    makeupSuggestions = emptyList(),
                    outfitSuggestions = emptyList(),
                    recommendedPalette = listOf("#D4C4B7", "#2C2A29", "#7A8B76", "#C18C5D")
                )
            )
            CuratedCollectionCard(
                uiState = com.zoewave.probase.kocolor.mobile.features.home.ui.CuratedCollectionUiState(dummyAnalysis),
                onEvent = {},
                navTo = {}
            )
        }
    }
}
