# BLE Hub Hardware Test Protocol

**App Version:** v0.0.5
**Device Model:** [FILL IN]
**Android OS:** [FILL IN]
**Tester:** [FILL IN]
**Date:** [FILL IN]

## 1. Desk Simulation (No Bike Needed)

### A. OpenBikeControl
- **Setup:** Run the OpenBikeControl BLE simulator (`d273f680-...`) or use an ESP32/nRF52840 button box.
- **Test:** 
  1. Connect to the device in AshBike Settings.
  2. Verify the App Info payload (`0x04`) is sent successfully upon connection.
  3. Press simulated buttons for `Pause`, `Resume`, and `Shift`. Verify actions trigger in the UI.
  4. Verify haptic buzz confirmation (`0x03`) triggers on the simulator when `Pause`/`Resume` are hit.
- **Pass/Fail:** [ ] PASS [ ] FAIL

### B. Standard Sensor Streams (HRS + CPS + CSC + FTMS)
- **Setup:** Use nRF Connect GATT Server or a CircuitPython script on an nRF52840 to broadcast `0x180D`, `0x1818`, `0x1816`, `0x1826` and `0x180F`.
- **Test:**
  1. Broadcast FTMS Indoor Bike Data sample: `[0x44,0x00,0xB2,0x0C,0xB5,0x00,0xF0,0x00]`.
  2. Verify Dashboard shows: `32.5 km/h`, `90.5 RPM`, `240 W`.
  3. Verify distance accumulates even when GPS is stationary.
  4. Broadcast low battery (`< 15%`) via `0x180F`. Verify UI alert.
  5. Stop broadcasting movement; wait 3 seconds and verify coasting zero-out logic sets speed/cadence to 0.
- **Pass/Fail:** [ ] PASS [ ] FAIL

## 2. Real Ride & Reconnect Test
- **Setup:** Pair a real HR strap and a Speed/Cadence or Power meter (or indoor smart trainer).
- **Test:**
  1. Start ride, confirm live data streaming.
  2. Walk out of BLE range (~20 meters) until disconnected.
  3. Walk back into range; confirm clean reconnect without speed/distance spike.
  4. Stop ride and save.
  5. Open Google Health Connect (or Google Fit) and verify the `HeartRateRecord` graph appears.
- **Pass/Fail:** [ ] PASS [ ] FAIL

## 3. NFC Forum BLE Handover (Tap-to-Pair)
- **Setup:** Program a blank NTAG215 sticker using the "Write NFC Tag" feature in AshBike Settings.
- **Test:**
  1. Hold the NTAG215 tag against the back of the phone.
  2. Verify AshBike instantly attempts to connect to the previously written sensor MAC address via standard BLE.
  3. Go to Android Settings -> Apps -> AshBike -> Uninstall. Reinstall.
  4. Open BLE Settings; verify the saved sensor list is completely empty.
- **Pass/Fail:** [ ] PASS [ ] FAIL

## Diagnostic Logs
Use the following `adb logcat` filter during tests to monitor the BLE GATT queues and sensor fusion:
```sh
adb logcat -s "BleSensorHub" "RideSensorFusionEngine" "BikeForegroundService"
```
