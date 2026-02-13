package com.zoewave.probase.ashbike.mobile.ui.navigation


// --- Feature Screens (Legacy & New Imports) ---

// --- Logic & Core ---
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import com.zoewave.ashbike.mobile.home.ui.HomeViewModel
import com.zoewave.ashbike.mobile.rides.ui.RidesUIRoute
import com.zoewave.ashbike.mobile.rides.ui.components.details.RideDetailScreen
import com.zoewave.ashbike.mobile.rides.ui.components.details.RideDetailViewModel
import com.zoewave.ashbike.mobile.rides.ui.components.haversineMeters
import com.zoewave.ashbike.mobile.settings.ui.SettingsUiRoute
import com.zoewave.probase.ashbike.features.main.navigation.AshBikeDestination
import com.zoewave.probase.core.ui.NavigationCommand
import com.zoewave.probase.core.util.Logging
import com.zoewave.probase.feature.places.ui.CoffeeShopEvent
import com.zoewave.probase.feature.places.ui.CoffeeShopUIState
import com.zoewave.probase.feature.places.ui.CoffeeShopViewModel
import com.zoewave.probaseapplications.bike.features.main.ui.HomeUiRoute


@SuppressLint("MissingPermission") // <--- ADD THIS
fun ashBikeNavEntryProvider(
    key: AshBikeDestination,
    navigateTo: (AshBikeDestination) -> Unit,
    homeViewModel: HomeViewModel
): NavEntry<AshBikeDestination> {

    // NavEntry wraps the content and provides the scope for Hilt ViewModels
    return NavEntry(key) {
        when (key) {
            // -----------------------------------------------------------------
            // 1. HOME TAB
            // -----------------------------------------------------------------
            is AshBikeDestination.Home -> {
                HomeUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = homeViewModel,
                    navTo = { command ->
                        // Bridge Legacy "NavigationCommand" to Nav3 Objects
                        when (command) {
                            is NavigationCommand.To -> {
                                // Example: Handle specific string routes if needed,
                                // or log that legacy strings are deprecated.
                                Logging.w(
                                    "Nav3",
                                    "Received legacy string command: ${command.route}"
                                )
                            }
                            // If you added 'ToScreen' to NavigationCommand as discussed:
                            // is NavigationCommand.ToScreen -> navigateTo(command.destination)
                            else -> { /* Handle Back, etc */
                            }
                        }
                    }
                )
            }

            // -----------------------------------------------------------------
            // 2. TRIPS TAB (List)
            // -----------------------------------------------------------------
            is AshBikeDestination.Trips -> {
                RidesUIRoute(
                    modifier = Modifier.fillMaxSize(),
                    // The Trips screen emits a string ID when a row is clicked.
                    // We wrap that String into our Type-Safe 'RideDetail' object.
                    navTo = { rideId ->
                        navigateTo(AshBikeDestination.RideDetail(rideId))
                    }
                )
            }

            // -----------------------------------------------------------------
            // 3. RIDE DETAIL (Detail Screen)
            // -----------------------------------------------------------------
            is AshBikeDestination.RideDetail -> {
                // A. Get ViewModels
                // Hilt scopes these to this specific NavEntry (Screen)
                val rideViewModel: RideDetailViewModel = hiltViewModel()
                val cafeViewModel: CoffeeShopViewModel = hiltViewModel()

                // B. CRITICAL: Bridge Nav3 Key -> ViewModel
                // Since Nav3 doesn't populate SavedStateHandle automatically yet,
                // we manually pass the ID from the Key to the ViewModel.
                LaunchedEffect(key.rideId) {
                    rideViewModel.loadRide(key.rideId)
                }

                // C. Collect State
                val rideWithLocs by rideViewModel.rideWithLocations.collectAsState()
                val cafeUiState by cafeViewModel.uiState.collectAsState()

                // D. Define Cafe Logic (Ported from legacy NavGraph)
                // This calculates the center of the ride and a dynamic radius to find coffee shops.
                val findCafesAction = {
                    val locations = rideWithLocs?.locations
                    if (!locations.isNullOrEmpty()) {
                        val centerLat = locations.map { it.lat }.average()
                        val centerLng = locations.map { it.lng }.average()

                        // Calculate dynamic radius based on ride size
                        val routeRadius = locations.maxOfOrNull { loc ->
                            haversineMeters(centerLat, centerLng, loc.lat, loc.lng)
                        } ?: 0.0

                        // Search slightly wider than the route (min 200m, max 1.5km)
                        val searchRadius = (routeRadius + 100.0).coerceIn(200.0, 1500.0)

                        cafeViewModel.onEvent(
                            CoffeeShopEvent.FindCafesInArea(
                                latitude = centerLat,
                                longitude = centerLng,
                                radius = searchRadius
                            )
                        )
                    } else {
                        Logging.w("Nav3", "User requested cafes, but ride location data is empty.")
                    }
                }

                // E. Extract Shop List safely
                val coffeeShops = (cafeUiState as? CoffeeShopUIState.Success)?.coffeeShops ?: emptyList()

                // F. Render the Screen
                RideDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    rideWithLocs = rideWithLocs,
                    coffeeShops = coffeeShops,
                    onFindCafes = findCafesAction,
                    onEvent = { event -> rideViewModel.onEvent(event) }
                )
            }

            // -----------------------------------------------------------------
            // 4. SETTINGS TAB
            // -----------------------------------------------------------------
            is AshBikeDestination.Settings -> {
                // The 'cardToExpand' arg is available directly on the key.
                SettingsUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    initialCardKeyToExpand = "", // emptyList<List<BusinessInfo>>(),// key.cardToExpand,
                    navTo = { path ->
                        // Handle internal settings navigation if needed
                        // or use navigateTo(...) to go elsewhere.
                        Logging.d("Nav3", "Settings requested nav to: $path")
                    }
                )
            }
        }
    }
}

/**
 * Original Code
 * entryProvider = { key ->
 *                     // Map the Destination Object -> NavEntry Wrapper
 *                     NavEntry(key) {
 *                         when (key) {
 *                             is AshBikeDestination.Home -> {
 *                                 /*HomeRoute(
 *                                     // Navigation Action: Simply add to the list!
 *                                     onNavigateToSettings = {
 *                                         backStack.add(AshBikeDestination.Settings)
 *                                     }
 *                                 )*/
 *                                 HomeUiRoute(
 *                                     viewModel = homeViewModel,
 *                                     // Go to setting so the radio rate can be set
 *                                     navTo = { backStack.add(AshBikeDestination.Settings) }
 *                                 )
 *                             }
 *
 *                             is AshBikeDestination.Trips -> {
 *                                 RidesUIRoute(
 *                                     navTo = { rideId ->
 *                                         navigateTo(AshBikeDestination.RideDetail(rideId))
 *                                     }
 *                                 )
 *
 *                                 /*
 *                                 RidesRoute(
 *                                     // Navigation Action: Simply add to the list!
 *                                     onNavigateToSettings = {
 *                                         backStack.add(AshBikeDestination.Settings)
 *                                     }
 *                                 )
 *                                 */
 *                             }
 *
 *                             is AshBikeDestination.Settings -> {
 *                                 /* SettingsRoute(
 *                                     // Navigation Action: Simply add to the list!
 *                                     onNavigateToSettings = {
 *                                         backStack.add(AshBikeDestination.Settings)
 *                                     }
 *                                 ) */
 *                                 SettingsUiRoute(
 *                                     navTo = { rideId ->
 *                                         navigateTo(AshBikeDestination.RideDetail(rideId))
 *                                     },
 *                                     initialCardKeyToExpand = null
 *                                 )
 *                             }
 *                             // ... inside your bikeNavEntryProvider 'when' block ...
 *
 *                             is AshBikeDestination.RideDetail -> {
 *                                 // A. Get ViewModels
 *                                 val rideViewModel: RideDetailViewModel = hiltViewModel()
 *                                 // val cafeViewModel: CoffeeShopViewModel = hiltViewModel()
 *
 *                                 // B. CRITICAL: Bridge Nav3 Key -> ViewModel
 *                                 // Since SavedStateHandle isn't autopopulated, we pass the ID manually.
 *                                 LaunchedEffect(key.rideId) {
 *                                     rideViewModel.loadRide(key.rideId)
 *                                 }
 *
 *                                 // C. Collect State
 *                                 val rideWithLocs by rideViewModel.rideWithLocations.collectAsState()
 *                                 // val cafeUiState by cafeViewModel.uiState.collectAsState()
 *
 *                                 // D. Define Cafe Logic
 *                                 val findCafesAction = {
 *                                     val locations = rideWithLocs?.locations
 *                                     if (!locations.isNullOrEmpty()) {
 *                                         val centerLat = locations.map { it.lat }.average()
 *                                         val centerLng = locations.map { it.lng }.average()
 *
 *                                         val routeRadius = locations.maxOfOrNull { loc ->
 *                                             haversineMeters(centerLat, centerLng, loc.lat, loc.lng)
 *                                         } ?: 0.0
 *
 *                                         // Search slightly wider than the route
 *                                         val searchRadius = (routeRadius + 100.0).coerceIn(200.0, 1500.0)
 *
 *                                         /*cafeViewModel.onEvent(
 *                                             CoffeeShopEvent.FindCafesInArea(centerLat, centerLng, searchRadius)
 *                                         )*/
 *                                     }
 *                                 }
 *
 *                                 // E. Extract Shop List
 *                                 // val coffeeShops = (cafeUiState as? CoffeeShopUIState.Success)?.coffeeShops ?: emptyList()
 *
 *                                 // F. Render Screen
 *                                 RideDetailScreen(
 *                                     modifier = Modifier.fillMaxSize(),
 *                                     rideWithLocs = rideWithLocs,
 *                                     // coffeeShops = coffeeShops,
 *                                     onFindCafes = findCafesAction,
 *                                     onEvent = rideViewModel::onEvent
 *                                 )
 *                             }
 *
 *                         }
 *                     }
 *                 }
 *
 *
 *
 *
 */