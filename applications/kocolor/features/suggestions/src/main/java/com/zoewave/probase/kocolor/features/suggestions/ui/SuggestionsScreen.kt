package com.zoewave.probase.kocolor.features.suggestions.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.kocolor.model.FashionAdvice
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun SuggestionsUiRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SuggestionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.fashionProfile) {
        if (uiState.fashionProfile != null && uiState.loadingState is SuggestionsLoadingState.Idle) {
            viewModel.getSuggestions()
        }
    }

    SuggestionsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = { route -> if (route == null) onBack() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsScreen(
    uiState: SuggestionsScreenUiState,
    onEvent: (SuggestionsEvent) -> Unit,
    navTo: (KoColorRoute?) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Suggestions") },
                navigationIcon = {
                    IconButton(onClick = { navTo(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.fashionProfile == null) {
                NoProfileState()
            } else {
                when (val state = uiState.loadingState) {
                    is SuggestionsLoadingState.Idle, is SuggestionsLoadingState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is SuggestionsLoadingState.Success -> {
                        SuggestionsList(state.advice)
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
fun NoProfileState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No fashion profile found. Please analyze your style first.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun SuggestionsList(advice: FashionAdvice) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Summary", style = MaterialTheme.typography.titleMedium)
            Text(advice.summary, style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Text("Outfit Suggestions", style = MaterialTheme.typography.titleMedium)
        }
        items(advice.outfitSuggestions) { outfit ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(outfit.occasion, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(outfit.advice)
                    if (outfit.keyPieces.isNotEmpty()) {
                        Text("Key pieces: ${outfit.keyPieces.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item {
            Text("Makeup Suggestions", style = MaterialTheme.typography.titleMedium)
        }
        items(advice.makeupSuggestions) { makeup ->
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
                fashionProfile = com.zoewave.probase.kocolor.model.FashionProfile(),
                loadingState = SuggestionsLoadingState.Loading
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
