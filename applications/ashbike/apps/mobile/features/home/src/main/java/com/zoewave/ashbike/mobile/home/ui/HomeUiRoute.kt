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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.zoewave.ashbike.mobile.glass.GlassesMainActivity
import com.zoewave.ashbike.mobile.home.components.BikeDashboardContent
import com.zoewave.ashbike.mobile.home.components.WaitingForGpsScreen
import com.zoewave.ashbike.mobile.home.ui.BikeSideEffect
import com.zoewave.ashbike.mobile.home.ui.HomeEvent
import com.zoewave.ashbike.mobile.home.ui.HomeUiState
import com.zoewave.ashbike.mobile.home.ui.HomeViewModel
import com.zoewave.probase.ashbike.features.main.navigation.AshBikeDestination
import com.zoewave.probase.ashbike.features.main.ui.ErrorScreen
import com.zoewave.probase.ashbike.features.main.ui.LoadingScreen
import com.zoewave.probase.feature.places.ui.CoffeeShopEvent
import com.zoewave.probase.feature.places.ui.CoffeeShopUIState
import com.zoewave.probase.feature.places.ui.CoffeeShopViewModel
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalPermissionsApi::class, ExperimentalProjectedApi::class)
@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
@Composable
fun HomeUiRoute(
    navTo: (AshBikeDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val coffeeShopViewModel = hiltViewModel<CoffeeShopViewModel>()

    val homeUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cafeUiState by coffeeShopViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            ProjectedContext.isProjectedDeviceConnected(context, this.coroutineContext)
                .collectLatest { isConnected ->
                    viewModel.updateGlassConnection(isConnected)
                }
        } else {
            viewModel.updateGlassConnection(false)
        }
    }

    DisposableEffect(Unit) {
        viewModel.bikeServiceManager.bindService(context)
        onDispose {
            viewModel.bikeServiceManager.unbindService(context)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.navigationChannel.collect { destination ->
            navTo(destination)
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BikeSideEffect.LaunchGlassProjection -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                        try {
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

    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    HomeUiRoute(
        homeUiState = homeUiState,
        cafeUiState = cafeUiState,
        onHomeEvent = viewModel::onEvent,
        onCoffeeShopEvent = coffeeShopViewModel::onEvent,
        onPermissionRequest = { permissionState.launchPermissionRequest() },
        onOpenSettings = {
            context.startActivity(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        },
        navTo = navTo,
        modifier = modifier
    )
}

@Composable
internal fun HomeUiRoute(
    homeUiState: HomeUiState,
    cafeUiState: CoffeeShopUIState,
    onHomeEvent: (HomeEvent) -> Unit,
    onCoffeeShopEvent: (CoffeeShopEvent) -> Unit,
    onPermissionRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    navTo: (AshBikeDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    when (homeUiState) {
        is HomeUiState.WaitingForGps -> {
            WaitingForGpsScreen(
                onRequestPermission = onPermissionRequest,
                onEnableGpsSettings = onOpenSettings
            )
        }

        is HomeUiState.Success -> {
            val coffeeShops = when (cafeUiState) {
                is CoffeeShopUIState.Success -> cafeUiState.coffeeShops
                else -> emptyList()
            }

            val onFindCafes = {
                val currentLocation = homeUiState.bikeData.location
                if (currentLocation != null && (currentLocation.latitude != 0.0 || currentLocation.longitude != 0.0)) {
                    onCoffeeShopEvent(
                        CoffeeShopEvent.FindCafesInArea(
                            latitude = currentLocation.latitude,
                            longitude = currentLocation.longitude,
                            radius = 1000.0
                        )
                    )
                }
            }

            val hasValidLocation = homeUiState.bikeData.location?.let {
                it.latitude != 0.0 || it.longitude != 0.0
            } ?: false

            if (hasValidLocation) {
                BikeDashboardContent(
                    modifier = modifier.fillMaxSize(),
                    uiState = homeUiState,
                    onHomeEvent = onHomeEvent,
                    navTo = navTo,
                    coffeeShops = coffeeShops,
                    placeName = null,
                    onFindCafes = onFindCafes
                )
            } else {
                WaitingForGpsScreen(
                    onRequestPermission = onPermissionRequest,
                    onEnableGpsSettings = onOpenSettings
                )
            }
        }

        is HomeUiState.Error -> {
            ErrorScreen(
                errorMessage = homeUiState.message,
                onRetry = { onHomeEvent(HomeEvent.StartRide) }
            )
        }

        HomeUiState.Loading, HomeUiState.Idle -> {
            LoadingScreen()
        }
    }
}
