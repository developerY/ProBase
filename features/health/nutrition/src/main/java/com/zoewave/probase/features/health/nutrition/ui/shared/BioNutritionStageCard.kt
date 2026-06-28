package com.zoewave.probase.features.health.nutrition.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.features.health.nutrition.data.NutritionStage

@Composable
fun BioNutritionStageCard(
    stage: NutritionStage,
    modifier: Modifier = Modifier
) {
    var showScientific by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (showScientific) stage.title else stage.suggestedMealTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = if (showScientific) stage.subtitle else stage.suggestedMealSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    onClick = { showScientific = !showScientific },
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.05f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (showScientific) Icons.Rounded.Restaurant else Icons.Rounded.Science,
                            contentDescription = "Switch View",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Black.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Text(
                text = if (showScientific) stage.scientificBody else stage.suggestedMealBody,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = Color.Black.copy(alpha = 0.8f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color.Black.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${stage.startTime} ${stage.endTime?.let { "- $it" } ?: ""}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9F7F2)
@Composable
private fun BioNutritionStageCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            BioNutritionStageCard(
                stage = NutritionStage(
                    id = "1",
                    title = "Metabolic Priming",
                    subtitle = "mTOR Activation",
                    scientificBody = "Scientific focus on mTOR signaling pathway...",
                    suggestedMealTitle = "Golden Turmeric Elixir",
                    suggestedMealSubtitle = "Anti-inflammatory",
                    suggestedMealBody = "A light morning drink to prepare the system...",
                    startTime = "07:00",
                    endTime = "09:00"
                )
            )
        }
    }
}
