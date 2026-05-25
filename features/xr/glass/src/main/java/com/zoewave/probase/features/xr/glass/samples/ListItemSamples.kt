package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.ListItem
import androidx.xr.glimmer.Text

@Composable
fun ListItemSamples() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ListItem(onClick = {}) {
            Text("Simple List Item")
        }
        
        ListItem(
            onClick = {},
            leadingIcon = { Icon(Icons.Default.Person, null) }
        ) {
            Text("Item with Leading Icon")
        }
        
        ListItem(
            onClick = {},
            trailingIcon = { Icon(Icons.Default.ChevronRight, null) }
        ) {
            Text("Item with Trailing Icon")
        }
    }
}
