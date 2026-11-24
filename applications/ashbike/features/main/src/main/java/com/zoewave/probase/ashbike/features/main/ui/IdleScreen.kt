package com.zoewave.probase.ashbike.features.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shown when no ride is active. Offers a button to start a new ride.
 */
@Composable
fun IdleScreen(
    modifier: Modifier = Modifier,
    onStart: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = androidx.compose.ui.res.stringResource(com.zoewave.probase.ashbike.features.main.R.string.applications_ashbike_features_main_ready_to_ride_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(48.dp)
        ) {
            Text(text = androidx.compose.ui.res.stringResource(com.zoewave.probase.ashbike.features.main.R.string.applications_ashbike_features_main_start_ride_button))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun IdleScreenPreview() {
    IdleScreen(onStart = {})
}
