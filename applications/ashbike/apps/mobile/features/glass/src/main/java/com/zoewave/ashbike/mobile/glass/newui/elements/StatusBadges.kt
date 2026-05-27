package com.zoewave.ashbike.mobile.glass.newui.elements

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.BatteryUnknown
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.Text
import com.zoewave.ashbike.mobile.glass.ui.BatteryZone

@Composable
fun BatteryBadge(
    zone: BatteryZone,
    level: String,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = when (zone) {
        BatteryZone.UNKNOWN -> Icons.AutoMirrored.Rounded.BatteryUnknown to GlimmerTheme.colors.outline
        BatteryZone.CRITICAL -> Icons.Rounded.BatteryAlert to GlimmerTheme.colors.negative
        BatteryZone.WARNING -> Icons.Rounded.BatteryStd to GlimmerTheme.colors.positive
        BatteryZone.GOOD -> Icons.Rounded.BatteryFull to GlimmerTheme.colors.positive
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = level,
            color = tint,
            style = GlimmerTheme.typography.caption
        )
    }
}

@Composable
fun ConnectionBadge(active: Boolean, modifier: Modifier = Modifier) {
    val color = if (active) GlimmerTheme.colors.secondary else GlimmerTheme.colors.outline
    val icon = if (active) Icons.Rounded.BluetoothConnected else Icons.Rounded.BluetoothDisabled
    val text = if (active) "CNX" else "DISC"

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            style = GlimmerTheme.typography.caption
        )
    }
}
