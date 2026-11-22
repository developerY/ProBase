package com.zoewave.probase.ashbike.wear.features.rides.ui.dash

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.zoewave.ashbike.wear.rides.R.string as RidesR

@Composable
fun FeatureHubScreen(
    onNavigateToWeather: () -> Unit,
    onNavigateToElevation: () -> Unit,
    onNavigateToHrGraph: () -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
    ) {
        item {
            ListHeader {
                Text(
                    text = stringResource(RidesR.applications_ashbike_apps_wear_features_rides_performance_title),
                    style = MaterialTheme.typography.title3
                )
            }
        }

        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToWeather,
                colors = ChipDefaults.secondaryChipColors(),
                label = { Text(stringResource(RidesR.applications_ashbike_apps_wear_features_rides_live_weather_label)) },
                secondaryLabel = { Text(stringResource(RidesR.applications_ashbike_apps_wear_features_rides_pre_ride_conditions), color = Color.Gray) },
                icon = { Icon(Icons.Default.WbSunny, contentDescription = stringResource(RidesR.applications_ashbike_apps_wear_features_rides_weather_icon_description)) }
            )
        }

        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToElevation,
                colors = ChipDefaults.secondaryChipColors(),
                label = { Text(stringResource(RidesR.applications_ashbike_apps_wear_features_rides_elevation_profile_label)) },
                secondaryLabel = { Text(stringResource(RidesR.applications_ashbike_apps_wear_features_rides_canvas_area_chart), color = Color.Gray) },
                icon = { Icon(Icons.Default.Terrain, contentDescription = stringResource(RidesR.applications_ashbike_apps_wear_features_rides_elevation_icon_description)) }
            )
        }

        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToHrGraph,
                colors = ChipDefaults.secondaryChipColors(),
                label = { Text(stringResource(RidesR.applications_ashbike_apps_wear_features_rides_seven_day_heart_rate_label)) },
                secondaryLabel = { Text(stringResource(RidesR.applications_ashbike_apps_wear_features_rides_canvas_bar_chart), color = Color.Gray) },
                icon = { Icon(Icons.Default.Favorite, contentDescription = stringResource(RidesR.applications_ashbike_apps_wear_features_rides_heart_rate_icon_description)) }
            )
        }

        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToMap,
                colors = ChipDefaults.secondaryChipColors(),
                label = { Text(stringResource(RidesR.applications_ashbike_apps_wear_features_rides_ride_map_label)) },
                secondaryLabel = { Text(stringResource(RidesR.applications_ashbike_apps_wear_features_rides_poly_line_gps_trace), color = Color.Gray) },
                icon = { Icon(Icons.Default.Map, contentDescription = stringResource(RidesR.applications_ashbike_apps_wear_features_rides_map_icon_description)) }
            )
        }
    }
}