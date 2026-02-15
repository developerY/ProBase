package com.zoewave.probaseapplications.bike.features.main.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.zoewave.ashbike.mobile.glass.GlassesMainActivity
import com.zoewave.ashbike.mobile.home.components.BikeDashboardContent
import com.zoewave.ashbike.mobile.home.components.WaitingForGpsScreen
import com.zoewave.probase.core.ui.BikeScreen
import com.zoewave.probase.core.ui.NavigationCommand
import com.zoewave.probase.feature.places.ui.CoffeeShopEvent
import com.zoewave.probase.feature.places.ui.CoffeeShopUIState
import com.zoewave.probase.feature.places.ui.CoffeeShopViewModel
import com.zoewave.ashbike.mobile.home.ui.BikeSideEffect
import com.zoewave.ashbike.mobile.home.ui.HomeEvent
import com.zoewave.ashbike.mobile.home.ui.HomeUiState
import com.zoewave.ashbike.mobile.home.ui.HomeViewModel
import com.zoewave.probase.ashbike.features.main.navigation.AshBikeDestination
import com.zoewave.probase.ashbike.features.main.ui.ErrorScreen
import com.zoewave.probase.ashbike.features.main.ui.LoadingScreen
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalPermissionsApi::class, ExperimentalProjectedApi::class)
@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
@Composable
fun HomeUiRoute(
    modifier: Modifier = Modifier,
    navTo: (AshBikeDestination) -> Unit,
    viewModel: HomeViewModel
) {
    // val healthViewModel = hiltViewModel<HealthViewModel>()
    // val nfcViewModel = hiltViewModel<NfcViewModel>()
    val coffeeShopViewModel = hiltViewModel<CoffeeShopViewModel>() // Added CoffeeShopViewModel

    val homeUiState by viewModel.uiState.collectAsState()
    val cafeUiState by coffeeShopViewModel.uiState.collectAsState() // Added Cafe UI State
    val context = LocalContext.current

    // --- 2. GLASS CONNECTION LISTENER (The Fix) ---
    // This listens to the hardware: Is the cable plugged in?
    LaunchedEffect(Unit) {
        // Only run on Android 15+ (Baklava/VanillaIceCream)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            // "this.coroutineContext" is valid here inside LaunchedEffect
            ProjectedContext.isProjectedDeviceConnected(context, this.coroutineContext)
                .collectLatest { isConnected ->
                    viewModel.updateGlassConnection(isConnected)
                }
        } else {
            // Fallback for older devices
            viewModel.updateGlassConnection(false)
        }
    }

    // --- 3. LIFECYCLE: Bind/Unbind Bike Service ---
    DisposableEffect(Unit) {
        viewModel.bikeServiceManager.bindService(context)
        onDispose {
            viewModel.bikeServiceManager.unbindService(context)
        }
    }

    // --- 4. NAV 3: LISTEN FOR NAVIGATION EVENTS ---
    // When the VM says "Go to Settings", this block runs and calls navTo().
    LaunchedEffect(viewModel) {
        viewModel.navigationChannel.collect { destination ->
            Log.d("HomeUiRoute", "Received Nav 3 Event: $destination")
            navTo(destination)
        }
    }

    // --- 4. SIDE EFFECTS: Launching Activities & Toasts ---
    // This listens for "One-off" commands from the ViewModel
    LaunchedEffect(key1 = true) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BikeSideEffect.LaunchGlassProjection -> {
                    // LOGIC: Attempt to launch on the external glass display
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                        try {
                            // This specifically targets the Glasses
                            val options = ProjectedContext.createProjectedActivityOptions(context)
                            val intent = Intent(context, GlassesMainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent, options.toBundle())
                        } catch (e: Exception) {
                            Log.e("BikeUiRoute", "Projection Launch Failed", e)
                            Toast.makeText(context, "Projection Failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Glasses require Android 15+", Toast.LENGTH_SHORT).show()
                    }
                }
                is BikeSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val permissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // We removed the manual navigation logic here. We just forward the event
    // to the ViewModel. The VM will emit to 'navigationChannel' if needed.
    val eventHandler = { event: HomeEvent ->
        viewModel.onEvent(event)
    }

    // --- G. UI RENDER LOGIC ---
    when (val currentHomeUiState = homeUiState) {
        is HomeUiState.WaitingForGps -> {
            WaitingForGpsScreen(
                onRequestPermission = { permissionState.launchPermissionRequest() },
                onEnableGpsSettings = {
                    context.startActivity(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }
            )
        }

        is HomeUiState.Success -> {
            val coffeeShops = when (val currentCafeUiState = cafeUiState) {
                is CoffeeShopUIState.Success -> currentCafeUiState.coffeeShops
                else -> emptyList()
            }

            val onFindCafes = {
                val currentLocation = currentHomeUiState.bikeData.location
                if (currentLocation != null && (currentLocation.latitude != 0.0 || currentLocation.longitude != 0.0)) {
                    coffeeShopViewModel.onEvent(
                        CoffeeShopEvent.FindCafesInArea(
                            latitude = currentLocation.latitude,
                            longitude = currentLocation.longitude,
                            radius = 1000.0 // Radius in meters
                        )
                    )
                } else {
                    Log.d("BikeUiRoute", "Cannot find cafes: Location not available or is (0,0)")
                    // Optionally, inform the user e.g., via a Toast or a Snackbar
                }
            }


            // Check if we actually have a valid location before showing Dashboard
            val hasValidLocation = currentHomeUiState.bikeData.location?.let {
                it.latitude != 0.0 || it.longitude != 0.0
            } ?: false

            if (hasValidLocation) {
                BikeDashboardContent(
                    modifier = modifier.fillMaxSize(),
                    uiState = currentHomeUiState,
                    onHomeEvent = eventHandler,
                    navTo = navTo,
                    coffeeShops = coffeeShops, // Passed coffeeShops
                    placeName = null, // Passed null for placeName, decide source later
                    onFindCafes = onFindCafes as () -> Unit // Passed onFindCafes lambda
                )
            } else {
                WaitingForGpsScreen(
                    onRequestPermission = { permissionState.launchPermissionRequest() },
                    onEnableGpsSettings = {
                        context.startActivity(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    }
                )
            }
        }

        is HomeUiState.Error -> {
            ErrorScreen(
                errorMessage = currentHomeUiState.message,
                onRetry = { viewModel.onEvent(HomeEvent.StartRide) }
            )
        }

        HomeUiState.Loading -> {
            LoadingScreen()
        }

        HomeUiState.Idle -> {
            LoadingScreen()
        }
    }
}
