package com.zoewave.probase.photodo.features.calendar.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.features.calendar.R

@Composable
internal fun PhotoDoCalendarScreen(
    viewModel: PhotoDoCalendarViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Text(
                text = stringResource(R.string.applications_photodo_features_calendar_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (uiState.isLoading) {
                Text(
                    text = stringResource(R.string.applications_photodo_features_calendar_loading), 
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.applications_photodo_features_calendar_coming_soon), 
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
