package com.zoewave.probase.core.model.ble

sealed class GattConnectionState {
    object Disconnected : GattConnectionState()
    object Connecting : GattConnectionState()
    object Connected : GattConnectionState()
}
