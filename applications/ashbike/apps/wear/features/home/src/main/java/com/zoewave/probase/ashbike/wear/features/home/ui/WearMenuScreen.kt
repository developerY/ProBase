package com.zoewave.probase.ashbike.wear.features.home.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import com.zoewave.ashbike.wear.home.R

@Composable
fun WearMenuScreen(
    onNavigateToRides: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Button(
                onClick = onNavigateToRides,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(stringResource(R.string.applications_ashbike_apps_wear_features_home_past_rides))
            }
        }
        item {
            Button(
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(stringResource(R.string.applications_ashbike_apps_wear_features_home_settings))
            }
        }
    }
}