package com.zoewave.probase.ashbike.features.main.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.zoewave.probase.core.ui.NavigationCommand

// We keep the signature exactly the same so your NavGraph doesn't break
@Composable
fun BikeUiRoute(
    modifier: Modifier = Modifier,
    navTo: (NavigationCommand) -> Unit,
    viewModel: BikeViewModel
) {
    // IGNORE everything else for now. Just render "Hi".
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Hi",
            style = MaterialTheme.typography.displayLarge
        )
    }
}