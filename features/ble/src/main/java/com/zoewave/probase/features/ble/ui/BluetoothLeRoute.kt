package com.zoewave.probase.features.ble.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.zoewave.probase.core.model.ble.DeviceService
import com.zoewave.probase.core.model.ble.GattConnectionState
import com.zoewave.probase.core.model.ble.ScanState
import com.zoewave.probase.features.ble.ui.components.BluetoothLeSuccessScreen
import com.zoewave.probase.features.ble.ui.components.ErrorScreen
import com.zoewave.probase.features.ble.ui.components.LoadingScreen
import com.zoewave.probase.features.ble.ui.components.PermissionsDenied
import com.zoewave.probase.features.ble.ui.components.PermissionsRationale
import com.zoewave.probase.features.ble.ui.components.StatusBar

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BluetoothLeRoute(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    viewModel: BluetoothLeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val gattConnectionState by viewModel.gattConnectionState.collectAsStateWithLifecycle()
    val isStartButtonEnabled by viewModel.isStartButtonEnabled.collectAsStateWithLifecycle()
    val gattServicesList by viewModel.gattServicesList.collectAsStateWithLifecycle()

    val blePermissions = listOf(
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH_ADVERTISE,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
    )

    val permissionState = rememberMultiplePermissionsState(permissions = blePermissions)

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            viewModel.onEvent(BluetoothLeEvent.PermissionsGranted)
        } else if (!permissionState.shouldShowRationale) {
            viewModel.onEvent(BluetoothLeEvent.PermissionsDenied)
        }
    }

    BluetoothLeRoute(
        uiState = uiState,
        scanState = scanState,
        gattConnectionState = gattConnectionState,
        isStartButtonEnabled = isStartButtonEnabled,
        gattServicesList = gattServicesList,
        onEvent = viewModel::onEvent,
        permissionState = permissionState,
        modifier = modifier.padding(paddingValues)
    )
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun BluetoothLeRoute(
    uiState: BluetoothLeUiState,
    scanState: ScanState,
    gattConnectionState: GattConnectionState,
    isStartButtonEnabled: Boolean,
    gattServicesList: List<DeviceService>,
    onEvent: (BluetoothLeEvent) -> Unit,
    permissionState: MultiplePermissionsState?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        StatusBar(
            permissionState = permissionState!!,
            onManagePermissionsClick = {
                permissionState?.launchMultiplePermissionRequest()
            },
            scanState = scanState
        )

        when (uiState) {
            BluetoothLeUiState.ShowBluetoothDialog -> {
                LaunchedEffect(Unit) {
                    val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    val activity = context as? Activity
                    activity?.startActivityForResult(enableBluetoothIntent, 1)
                }
            }

            is BluetoothLeUiState.PermissionsRequired -> PermissionsRationale {
                permissionState?.launchMultiplePermissionRequest()
            }

            is BluetoothLeUiState.PermissionsDenied -> PermissionsDenied {
                permissionState?.launchMultiplePermissionRequest()
            }

            is BluetoothLeUiState.Loading -> LoadingScreen()

            is BluetoothLeUiState.DataLoaded -> {
                BluetoothLeSuccessScreen(
                    scanState = scanState,
                    gattConnectionState = gattConnectionState,
                    activeDevice = uiState.activeDevice,
                    discoveredDevices = uiState.discoveredDevices,
                    isStartScanningEnabled = isStartButtonEnabled,
                    startScan = { onEvent(BluetoothLeEvent.StartScan) },
                    stopScan = { onEvent(BluetoothLeEvent.StopScan) },
                    connectToActiveDevice = { onEvent(BluetoothLeEvent.ConnectToSensorTag) },
                    readCharacteristics = { onEvent(BluetoothLeEvent.ReadCharacteristics) },
                    gattServicesList = gattServicesList,
                    onDeviceSelected = { device ->
                        onEvent(BluetoothLeEvent.SetActiveDevice(device))
                    }
                )
            }

            is BluetoothLeUiState.Error -> ErrorScreen(uiState.message)
        }
    }
}
