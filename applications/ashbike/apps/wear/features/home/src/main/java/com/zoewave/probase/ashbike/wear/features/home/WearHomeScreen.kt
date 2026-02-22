package com.zoewave.probase.ashbike.wear.features.home


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun WearHomeScreen(
    onNavigateToMenu: () -> Unit
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Your Speedometer UI ---
        item {
            Text(
                text = "24",
                style = MaterialTheme.typography.displayLarge
            )
        }
        item {
            Text(
                text = "km/h",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // --- 2. The Bottom Overflow Button ---
        item {
            CompactButton(
                onClick = onNavigateToMenu,
                icon = {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Open Menu",
                        // CompactButtons require the ExtraSmall icon size to render correctly
                        modifier = Modifier.size(ButtonDefaults.ExtraSmallIconSize)
                    )
                }
            )
        }
    }
}