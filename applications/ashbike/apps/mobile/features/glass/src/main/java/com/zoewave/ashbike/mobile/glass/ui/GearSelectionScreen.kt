package com.zoewave.ashbike.mobile.glass.ui

// Glimmer Imports
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.ListItem
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.list.VerticalList
import androidx.xr.glimmer.surface
import com.zoewave.ashbike.mobile.glass.R

@Composable
fun GearSelectionScreen(
    currentGear: Int,
    onGearSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .surface(focusable = false)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(title = { Text(stringResource(R.string.applications_ashbike_apps_mobile_features_glass_select_gear)) }) {
            // Using VerticalList for proper Glimmer focus handling
            VerticalList(
                modifier = Modifier
                    .width(300.dp) // Restrict width so it looks like a menu
                    .height(200.dp) // Restrict height to force scrolling behavior
            ) {
                items(12) { index ->
                    val gearNum = index + 1
                    val isSelected = (gearNum == currentGear)

                    ListItem(
                        onClick = { onGearSelected(gearNum) },
                        // 1. Manually handle the "Selected" background color
                        color = if (isSelected) GlimmerTheme.colors.primary else GlimmerTheme.colors.surface,

                        // 2. Add a Checkmark icon if selected
                        leadingIcon = if (isSelected) {
                            { Icon(imageVector = Icons.Default.Check, contentDescription = stringResource(R.string.applications_ashbike_apps_mobile_features_glass_selected)) }
                        } else null,

                        // 3. The main text goes in the trailing lambda 'content'
                        content = {
                            Text(stringResource(R.string.applications_ashbike_apps_mobile_features_glass_gear_label, gearNum))
                        },

                        // 4. Optional: "Active" label underneath
                        supportingLabel = if (isSelected) {
                            { Text(stringResource(R.string.applications_ashbike_apps_mobile_features_glass_active)) }
                        } else null
                    )
                }
            }
        }
    }
}