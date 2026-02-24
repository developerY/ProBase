package com.zoewave.probase.ashbike.wear.ui.navigation



// ✅ Import the specific Material components you requested

// Import your feature screens
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import com.zoewave.probase.ashbike.wear.features.home.WearHomeScreen
import com.zoewave.probase.ashbike.wear.features.rides.RideHistoryRoute
import com.zoewave.probase.ashbike.wear.features.settings.WearSettingsScreen


@Composable
fun AshBikeWearPager(
    onNavigateToRideDetail: (String) -> Unit
) {
    // 1. The state that controls the actual swipeable pager
    val pagerState = rememberPagerState(pageCount = { 3 })

    // 2. The adapter that feeds the pager's position to the dots
    val indicatorState = remember(pagerState) {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = pagerState.currentPageOffsetFraction
            override val selectedPage: Int
                get() = pagerState.currentPage
            override val pageCount: Int
                get() = pagerState.pageCount
        }
    }

    // 3. A Box lets us draw the dots floating on top of the screens
    Box(modifier = Modifier.fillMaxSize()) {

        // The Swipeable Carousel
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> WearHomeScreen()
                1 -> RideHistoryRoute { rideId ->
                    onNavigateToRideDetail(rideId)
                }
                2 -> WearSettingsScreen()
            }
        }

        // The Native Page Indicators (The dots!)
        HorizontalPageIndicator(
            pageIndicatorState = indicatorState,
            modifier = Modifier
                //.align(Alignment.BottomCenter)
                .padding(bottom = 10.dp) // Pushes the dots slightly up from the very bottom curve
        )
    }
}