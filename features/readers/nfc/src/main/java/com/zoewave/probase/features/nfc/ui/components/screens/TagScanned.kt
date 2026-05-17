package com.zoewave.probase.features.readers.nfc.ui.components.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zoewave.probase.features.readers.nfc.R
import com.zoewave.probase.features.readers.nfc.ui.NfcRwEvent
import com.zoewave.probase.features.readers.nfc.ui.NfcUiState

@Composable
fun TagScanned(
    uiState: NfcUiState.TagScanned,
    onEvent: (NfcRwEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.features_nfc_scanned_success))
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.features_nfc_tag_info, uiState.tagInfo))
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = { onEvent(NfcRwEvent.StopScan) }) {
                Text(stringResource(R.string.features_nfc_action_stop_scan))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { onEvent(NfcRwEvent.StartScan) }) {
                Text(stringResource(R.string.features_nfc_action_scan_again))
            }
        }
    }
}
