package com.zoewave.probase.seaweed.mobile.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zoewave.probase.seaweed.features.main.navigation.SeaweedDestination

@Composable
fun SettingsUiRoute(
    modifier: Modifier = Modifier,
    navTo: (SeaweedDestination) -> Unit
) {
    SettingsScreen(
        modifier = modifier,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navTo: (SeaweedDestination) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ListItem(
                headlineContent = { Text("About Seaweed") },
                supportingContent = { Text("Version 0.0.1") }
            )
        }
    }
}
