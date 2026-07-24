package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ResultTabToggle(
    selectedTab: ResultTab,
    onTabSelected: (ResultTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(200.dp)
            .height(64.dp), // Increased height for 3 tabs + padding
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
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
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color.Black else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResultTabTogglePreview() {
    val selectedTab = remember { mutableStateOf(ResultTab.FACE) }
    ResultTabToggle(
        selectedTab = selectedTab.value,
        onTabSelected = { selectedTab.value = it }
    )
}
