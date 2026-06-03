package com.zoewave.probase.kocolor.features.suggestions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.kocolor.features.suggestions.R
import com.zoewave.probase.kocolor.model.FashionAdvice
import com.zoewave.probase.kocolor.model.KoColorRoute

@Preview(showBackground = true)
@Composable
private fun SuggestionsUiRoutePreview() {
    MaterialTheme {
        SuggestionsUiRoute(navTo = {})
    }
}

@Composable
fun SuggestionsUiRoute(
    uiState: Unit = Unit,
    onEvent: (Unit) -> Unit = {},
    navTo: (KoColorRoute) -> Unit
) {
    val viewModel: SuggestionsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.fashionProfile) {
        if (state.fashionProfile != null && state.loadingState is SuggestionsLoadingState.Idle) {
            viewModel.getSuggestions()
        }
    }

    SuggestionsScreen(
        uiState = state,
        onEvent = viewModel::onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsScreen(
    uiState: SuggestionsScreenUiState,
    onEvent: (SuggestionsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_suggestions_title)) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_suggestions_back))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.fashionProfile == null) {
                NoProfileState(
                    uiState = Unit,
                    onEvent = {},
                    navTo = navTo
                )
            } else {
                when (val state = uiState.loadingState) {
                    is SuggestionsLoadingState.Idle, is SuggestionsLoadingState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is SuggestionsLoadingState.Success -> {
                        SuggestionsList(
                            uiState = state.advice,
                            onEvent = {},
                            navTo = navTo
                        )
                    }
                    is SuggestionsLoadingState.Error -> {
                        Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun NoProfileState(
    uiState: Unit,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.applications_kocolor_features_suggestions_no_profile), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

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
                    Text(outfit.occasion, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
                    Text(makeup.category, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(makeup.advice)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoProfileStatePreview() {
    MaterialTheme {
        NoProfileState(uiState = Unit, onEvent = {}, navTo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SuggestionsListPreview() {
    MaterialTheme {
        SuggestionsList(
            uiState = FashionAdvice(
                summary = "Advice summary",
                seasonalType = com.zoewave.probase.kocolor.model.SeasonalType.WINTER,
                undertone = com.zoewave.probase.kocolor.model.Undertone.COOL,
                makeupSuggestions = emptyList(),
                outfitSuggestions = emptyList(),
                recommendedPalette = emptyList()
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuggestionsScreenPreview_NoProfile() {
    MaterialTheme {
        SuggestionsScreen(
            uiState = SuggestionsScreenUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuggestionsScreenPreview_Loading() {
    MaterialTheme {
        SuggestionsScreen(
            uiState = SuggestionsScreenUiState(
                fashionProfile = com.zoewave.probase.kocolor.model.FashionProfile(
                    seasonalType = com.zoewave.probase.kocolor.model.SeasonalType.WINTER,
                    undertone = com.zoewave.probase.kocolor.model.Undertone.COOL
                ),
                loadingState = SuggestionsLoadingState.Loading
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
