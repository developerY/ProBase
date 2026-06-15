package com.zoewave.probase.kocolor.features.suggestions.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.suggestions.R
import com.zoewave.probase.core.model.ritual.FashionAdvice
import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.core.model.ritual.Undertone
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun SuggestionsList(
    uiState: FashionAdvice,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(stringResource(R.string.applications_kocolor_features_suggestions_summary), style = MaterialTheme.typography.titleMedium)
            Text(uiState.summary, style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Text(stringResource(R.string.applications_kocolor_features_suggestions_outfit_suggestions), style = MaterialTheme.typography.titleMedium)
        }
        items(uiState.outfitSuggestions) { outfit ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(outfit.occasion, fontWeight = FontWeight.Bold)
                    Text(outfit.advice)
                    if (outfit.keyPieces.isNotEmpty()) {
                        Text(stringResource(R.string.applications_kocolor_features_suggestions_key_pieces_format, outfit.keyPieces.joinToString(", ")), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item {
            Text(stringResource(R.string.applications_kocolor_features_suggestions_makeup_suggestions), style = MaterialTheme.typography.titleMedium)
        }
        items(uiState.makeupSuggestions) { makeup ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(makeup.category, fontWeight = FontWeight.Bold)
                    Text(makeup.advice)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SuggestionsListPreview() {
    MaterialTheme {
        SuggestionsList(
            uiState = FashionAdvice(
                summary = "Advice summary",
                seasonalType = com.zoewave.probase.core.model.ritual.SeasonalType.WINTER,
                undertone = com.zoewave.probase.core.model.ritual.Undertone.COOL,
                makeupSuggestions = emptyList(),
                outfitSuggestions = emptyList(),
                recommendedPalette = emptyList()
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
