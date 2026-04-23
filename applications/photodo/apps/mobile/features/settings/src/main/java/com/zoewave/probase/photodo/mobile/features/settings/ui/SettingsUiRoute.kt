package com.zoewave.probase.photodo.mobile.features.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.settings.ui.components.SettingsScreen
import com.zoewave.probase.photodo.mobile.features.settings.ui.components.ThemeIdentifiers
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute


@Composable
fun SettingsUiRoute(
    navTo: (PhotoTodoRoute?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsUiRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo,
        modifier = modifier
    )
}

@Composable
internal fun SettingsUiRoute(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsUiRoutePreview() {
    PhotoDoTheme {
        SettingsUiRoute(
            uiState = SettingsUiState(
                currentTheme = ThemeIdentifiers.SYSTEM,
                initialCardKeyToExpand = ThemeIdentifiers.SYSTEM
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
