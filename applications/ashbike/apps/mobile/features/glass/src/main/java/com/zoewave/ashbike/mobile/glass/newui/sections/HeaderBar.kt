package com.zoewave.ashbike.mobile.glass.newui.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.Text
import com.zoewave.ashbike.mobile.glass.newui.elements.BatteryBadge
import com.zoewave.ashbike.mobile.glass.newui.elements.ConnectionBadge
import com.zoewave.ashbike.mobile.glass.ui.BatteryZone
import com.zoewave.ashbike.mobile.glass.R


@Composable
fun HeaderBar(
    isConnected: Boolean,
    gear: Int,
    batteryZone: BatteryZone,
    batteryText: String,
    onGearUp: () -> Unit,
    onGearDown: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isConnected) {
            // MODE: CONTROLS
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.applications_ashbike_apps_mobile_features_glass_gear, gear).uppercase(),
                    color = GlimmerTheme.colors.positive,
                    style = GlimmerTheme.typography.titleLarge
                )
                Spacer(Modifier.width(12.dp))
                // Gear Dots (Visual representation of range)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (index < gear) GlimmerTheme.colors.positive else GlimmerTheme.colors.outline)
                        )
                    }
                }
            }
            
            Spacer(Modifier.width(24.dp))
            
            Row {
                Button(onClick = onGearDown, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.Remove, contentDescription = "Down")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onGearUp, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.Add, contentDescription = "Up")
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // STATUS COLUMN
        if (isConnected) {
            Column(horizontalAlignment = Alignment.End) {
                ConnectionBadge(active = isConnected)
                Spacer(modifier = Modifier.height(4.dp))
                BatteryBadge(
                    zone = batteryZone,
                    level = batteryText,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}