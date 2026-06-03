package com.zoewave.probase.kocolor.features.suggestions.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.zoewave.probase.kocolor.features.suggestions.R
import com.zoewave.probase.kocolor.features.suggestions.ui.components.NoProfileState
import com.zoewave.probase.kocolor.features.suggestions.ui.components.SuggestionsList
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun SuggestionsUiRoute(
    uiState: SuggestionsScreenUiState,
    onEvent: (SuggestionsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    SuggestionsScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo
    )
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
