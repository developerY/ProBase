package com.zoewave.probase.ashbike.wear.features.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable

@Composable
fun WearSettingsScreen() {
    androidx.compose.foundation.layout.Box(contentAlignment = androidx.compose.ui.Alignment.Center, modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        androidx.wear.compose.material3.Text("Settings (Swipe Right to go back)")
    }
}