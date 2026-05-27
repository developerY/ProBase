package com.zoewave.ashbike.mobile.glass.newui.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.Text

@Composable
fun SummaryStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color = GlimmerTheme.colors.secondary
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = {} // Focusable
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    text = value,
                    style = GlimmerTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = label.uppercase(),
                    style = GlimmerTheme.typography.caption,
                    color = GlimmerTheme.colors.outline
                )
            }
        }
    }
}

@Composable
fun DualStatCard(
    label1: String, value1: String, icon1: ImageVector,
    label2: String, value2: String, icon2: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = {}
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniStat(label = label1, value = value1, icon = icon1)
            Spacer(Modifier.width(16.dp))
            MiniStat(label = label2, value = value2, icon = icon2)
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GlimmerTheme.colors.secondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(text = value, style = GlimmerTheme.typography.bodyLarge, color = Color.White)
            Text(text = label.uppercase(), style = GlimmerTheme.typography.caption, color = GlimmerTheme.colors.outline)
        }
    }
}
