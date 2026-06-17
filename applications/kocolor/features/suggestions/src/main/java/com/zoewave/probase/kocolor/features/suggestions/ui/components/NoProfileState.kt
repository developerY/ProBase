package com.zoewave.probase.kocolor.features.suggestions.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.suggestions.R
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun NoProfileState(
    uiState: Unit,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.applications_kocolor_features_suggestions_no_profile), textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true)
@Composable
private fun NoProfileStatePreview() {
    MaterialTheme {
        NoProfileState(uiState = Unit, onEvent = {}, navTo = {})
    }
}
