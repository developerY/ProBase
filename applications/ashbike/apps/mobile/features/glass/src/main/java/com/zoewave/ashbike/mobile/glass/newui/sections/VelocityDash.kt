package com.zoewave.ashbike.mobile.glass.newui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.Text
import com.zoewave.ashbike.mobile.glass.R

@Composable
fun VelocityDash(
    speed: String,
    heading: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = {} // Enable focus feedback
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. SPEED (Primary Data)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.applications_ashbike_apps_mobile_features_glass_speed).uppercase(),
                    style = GlimmerTheme.typography.caption,
                    color = GlimmerTheme.colors.outline
                )
                Text(
                    text = speed,
                    style = GlimmerTheme.typography.titleLarge,
                    color = GlimmerTheme.colors.secondary // Matches screenshot's blue
                )
            }

            // 2. HEADING (Secondary Data)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = GlimmerTheme.colors.secondary
                )
                Text(
                    text = heading,
                    style = GlimmerTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}