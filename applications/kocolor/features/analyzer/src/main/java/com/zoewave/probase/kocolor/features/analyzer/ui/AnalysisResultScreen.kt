package com.zoewave.probase.kocolor.features.analyzer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.core.model.ritual.FashionAdvice
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.core.model.ritual.Undertone

@Composable
fun AnalysisResultScreen(
    uiState: FashionAdvice,
    modifier: Modifier = Modifier,
    onEvent: (AnalyzerEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(stringResource(R.string.applications_kocolor_features_analyzer_result_title), style = MaterialTheme.typography.headlineSmall)
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.applications_kocolor_features_analyzer_seasonal_type_format, uiState.seasonalType), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.applications_kocolor_features_analyzer_undertone_format, uiState.undertone), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(uiState.summary)
                }
            }
        }
        item {
            Text(stringResource(R.string.applications_kocolor_features_analyzer_recommended_palette), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.recommendedPalette.forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(parseColor(hex))
                    )
                }
            }
        }
        item {
            Text(stringResource(R.string.applications_kocolor_features_analyzer_makeup_nail_suggestions), style = MaterialTheme.typography.titleMedium)
        }
        items(uiState.makeupSuggestions) { suggestion ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(suggestion.category, fontWeight = FontWeight.Bold)
                        Text(suggestion.advice)
                    }
                    if (suggestion.category.contains("Nail", ignoreCase = true) || 
                        suggestion.category.contains("Lip", ignoreCase = true) || 
                        suggestion.category.contains("Blush", ignoreCase = true)) {
                        Button(
                            onClick = {
                                val color = suggestion.recommendedColors.firstOrNull() ?: "#FF0000"
                                if (suggestion.category.contains("Nail", ignoreCase = true)) {
                                    val finish = if (suggestion.advice.contains("Matte", ignoreCase = true)) "MATTE"
                                                 else if (suggestion.advice.contains("Metallic", ignoreCase = true)) "METALLIC"
                                                 else "GLOSSY"
                                    navTo(KoColorRoute.NailLab(color, finish))
                                } else {
                                    navTo(KoColorRoute.FaceLab(color, suggestion.category))
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.applications_kocolor_features_analyzer_experience), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(onClick = { onEvent(AnalyzerEvent.OnResetClicked) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.applications_kocolor_features_analyzer_discard))
                }
                Button(onClick = { onEvent(AnalyzerEvent.OnSaveClicked(uiState)) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.applications_kocolor_features_analyzer_save_analysis))
                }
            }
        }
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalysisResultScreenPreview() {
    MaterialTheme {
        AnalysisResultScreen(
            uiState = FashionAdvice(
                summary = "Test summary",
                seasonalType = SeasonalType.WINTER,
                undertone = Undertone.COOL,
                makeupSuggestions = emptyList(),
                outfitSuggestions = emptyList(),
                recommendedPalette = listOf("#FF0000", "#00FF00")
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
