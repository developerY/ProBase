package com.zoewave.ashbike.mobile.glass.newui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.stack.VerticalStack
import androidx.xr.glimmer.surface
import com.zoewave.ashbike.mobile.glass.R
import com.zoewave.ashbike.mobile.glass.newui.sections.HeaderBar
import com.zoewave.ashbike.mobile.glass.newui.sections.VelocityDash
import com.zoewave.ashbike.mobile.glass.newui.sections.SummaryStatCard
import com.zoewave.ashbike.mobile.glass.newui.sections.DualStatCard
import com.zoewave.ashbike.mobile.glass.ui.GlassUiEvent
import com.zoewave.ashbike.mobile.glass.ui.GlassUiState

@Composable
fun AshGlassLayout(
    modifier: Modifier = Modifier,
    uiState: GlassUiState,
    areVisualsOn: Boolean,
    isVisualUiSupported: Boolean,
    onEvent: (GlassUiEvent) -> Unit
) {
    Box(
        modifier = modifier
            .background(if (areVisualsOn) Color.Black else Color.Transparent)
            .surface()
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 1. TOP HEADER: GEAR & STATUS
            HeaderBar(
                isConnected = uiState.isBikeConnected,
                gear = uiState.currentGear,
                batteryZone = uiState.batteryZone,
                batteryText = uiState.formattedBattery,
                onGearUp = { onEvent(GlassUiEvent.GearUp) },
                onGearDown = { onEvent(GlassUiEvent.GearDown) }
            )

            Spacer(Modifier.height(16.dp))

            // 2. MAIN CONTENT: IMMERSIVE STACK
            VerticalStack(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Item 1: SPEED & HEADING (Focus on current performance)
                item {
                    VelocityDash(
                        speed = uiState.formattedSpeed,
                        heading = uiState.formattedHeading,
                        modifier = Modifier.itemDecoration(GlimmerTheme.shapes.medium)
                    )
                }

                // Item 2: AVG SPEED (Glanceable History)
                item {
                    SummaryStatCard(
                        label = "AVG SPEED",
                        value = "${uiState.averageSpeed} mph",
                        icon = Icons.Rounded.Speed,
                        modifier = Modifier.itemDecoration(GlimmerTheme.shapes.medium)
                    )
                }

                // Item 3: DISTANCE & DURATION (Trip Progress)
                item {
                    DualStatCard(
                        label1 = "DIST", value1 = "${uiState.tripDistance} mi", icon1 = Icons.Rounded.Straighten,
                        label2 = "TIME", value2 = uiState.rideDuration, icon2 = Icons.Rounded.AvTimer,
                        modifier = Modifier.itemDecoration(GlimmerTheme.shapes.medium)
                    )
                }

                // Item 4: CALORIES (Energy Burned)
                item {
                    SummaryStatCard(
                        label = "CALORIES",
                        value = uiState.calories,
                        icon = Icons.Rounded.LocalFireDepartment,
                        accentColor = GlimmerTheme.colors.positive,
                        modifier = Modifier.itemDecoration(GlimmerTheme.shapes.medium)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 3. EXIT BUTTON (Centered at bottom)
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(onClick = { onEvent(GlassUiEvent.CloseApp) }) {
                    Text(stringResource(R.string.applications_ashbike_apps_mobile_features_glass_exit))
                }
            }
        }
    }
}
