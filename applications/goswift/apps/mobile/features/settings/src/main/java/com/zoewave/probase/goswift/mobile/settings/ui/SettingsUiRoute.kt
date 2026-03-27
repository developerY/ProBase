package com.zoewave.probase.goswift.mobile.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zoewave.probase.goswift.features.main.navigation.GoSwiftDestination

@Composable
fun SettingsUiRoute(
    modifier: Modifier = Modifier,
    navTo: (GoSwiftDestination) -> Unit
) {
    SettingsScreen(
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
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
                headlineContent = { Text("App Version") },
                supportingContent = { Text("0.0.1") }
            )
            Divider()
            ListItem(
                headlineContent = { Text("Caffeine Cut-off") },
                supportingContent = { Text("18:00 (Standard)") }
            )
        }
    }
}
