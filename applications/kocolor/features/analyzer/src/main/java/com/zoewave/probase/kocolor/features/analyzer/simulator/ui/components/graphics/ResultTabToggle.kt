package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.ResultTab

@Composable
fun ResultTabToggle(
    selectedTab: ResultTab,
    onTabSelected: (ResultTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(180.dp)
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ResultTabItem(
                icon = Icons.Default.Face,
                isSelected = selectedTab == ResultTab.FACE,
                onClick = { onTabSelected(ResultTab.FACE) }
            )
            ResultTabItem(
                icon = Icons.Default.Checkroom,
                isSelected = selectedTab == ResultTab.CLOTHES,
                onClick = { onTabSelected(ResultTab.CLOTHES) }
            )
            ResultTabItem(
                icon = Icons.Default.PanTool,
                isSelected = selectedTab == ResultTab.NAILS,
                onClick = { onTabSelected(ResultTab.NAILS) }
            )
        }
    }
}

@Composable
private fun ResultTabItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color.Black else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else Color.Black,
            modifier = Modifier.size(20.dp)
        )
    }
}
