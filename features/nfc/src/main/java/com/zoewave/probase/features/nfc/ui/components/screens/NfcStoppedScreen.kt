package com.zoewave.probase.features.nfc.ui.components.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zoewave.probase.features.nfc.R
import com.zoewave.probase.features.nfc.ui.NfcRwEvent

// NFC available but not scanning yet.
@Composable
fun NfcStoppedScreen(
    onEvent: (NfcRwEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.features_nfc_ready_instructions))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onEvent(NfcRwEvent.StartScan) }) {
            Text(stringResource(R.string.features_nfc_action_start_scan))
        }
    }
}
