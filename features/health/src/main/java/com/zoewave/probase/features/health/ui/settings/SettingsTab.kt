package com.zoewave.probase.features.health.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zoewave.probase.features.health.R
import com.zoewave.probase.features.health.ui.HealthEvent

@Composable
fun SettingsTab(
    onEvent: (HealthEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.features_health_settings_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Re-use your existing connection status component
        HealthConnectionStatus(onEvent = onEvent)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.features_health_debug_tools_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.features_health_debug_tools_description),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { onEvent(HealthEvent.WriteTestRide) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.features_health_action_add_test_ride))
        }
    }
}