package com.zoewave.probase.features.readers.nfc.ui


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.readers.nfc.ui.components.screens.ErrorScreen
import com.zoewave.probase.features.readers.nfc.ui.components.screens.LoadingScreen
import com.zoewave.probase.features.readers.nfc.ui.components.screens.NfcAppScreen


@Composable
fun NfcUiRoute(
    modifier: Modifier = Modifier,
    viewModel: NfcViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NfcUiRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
internal fun NfcUiRoute(
    uiState: NfcUiState,
    onEvent: (NfcRwEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is NfcUiState.Error -> {
            ErrorScreen(
                message = uiState.message,
                onRetry = { onEvent(NfcRwEvent.Retry) }
            )
        }

        is NfcUiState.Loading -> {
            LoadingScreen()
        }
        // For all other states—read and write—we show the main NfcAppScreen.
        is NfcUiState.NfcNotSupported,
        is NfcUiState.NfcDisabled,
        is NfcUiState.Stopped,
        is NfcUiState.WaitingForTag,
        is NfcUiState.TagScanned,
        is NfcUiState.Writing,
        is NfcUiState.WriteError,
        is NfcUiState.WriteSuccess -> {
            NfcAppScreen(
                uiState = uiState,
                onEvent = onEvent,
                modifier = modifier
            )
        }
    }
}

