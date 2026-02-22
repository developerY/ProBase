package com.zoewave.probase.ashbike.wear.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import com.zoewave.probase.ashbike.wear.features.home.WearHomeScreen
import com.zoewave.probase.ashbike.wear.features.rides.WearBikeViewModel
import com.zoewave.probase.ashbike.wear.features.rides.WearRidesScreen
import com.zoewave.probase.ashbike.wear.features.settings.WearSettingsScreen

// Import the UI from your isolated feature modules

@Composable
fun AshBikeWearPager(
    onNavigateToRideDetail: (String) -> Unit // The parameter passed from Nav3
) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->

        when (page) {
            0 -> {
                // Inject HomeViewModel and pass it to the Home UI
                // val viewModel: HomeViewModel = hiltViewModel()
                WearHomeScreen()//viewModel = viewModel)
            }

            1 -> {
                // Inject WearBikeViewModel and pass it to the Rides UI
                val viewModel: WearBikeViewModel = hiltViewModel()
                WearRidesScreen(
                    //viewModel = viewModel,
                    // Pass the Nav3 callback down to the feature
                    // onRideSelected = { rideId -> onNavigateToRideDetail(rideId) }
                )
            }

            2 -> {
                // Inject SettingsViewModel and pass it to the Settings UI
                //val viewModel: SettingsViewModel = hiltViewModel()
                WearSettingsScreen()//viewModel = viewModel)
            }
        }
    }
}