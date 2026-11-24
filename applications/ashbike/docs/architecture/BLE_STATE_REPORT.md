# 📶 AshBike BLE (Bluetooth Low Energy) State Report

**Date:** November 2026
**Target:** AshBike Mobile (`:applications:ashbike:apps:mobile`)

This document outlines the exact current state of Bluetooth Low Energy (BLE) integration within the AshBike codebase, detailing what is simulated, what hardware permissions exist, and how to transition to live GATT server connections.

---

## 1. Current State: Simulated (Demo Mode)
Currently, AshBike **does not establish active connections to physical BLE hardware.** 

To facilitate UI development and emulator testing, the app relies on in-memory mocks and a `DemoModeSimulator` to pump realistic data into the UI.

### What it is "Reading" (Simulating):
*   **Heart Rate (`BleHeartRateRepository.kt`):** A coroutine `flow` loops and emits a simulated heart rate (bouncing between 60 BPM and 150 BPM) with randomized variance.
*   **Bike Gears & Suspension (`BikeRepositoryImpl.kt`):** Shifting gears (1 to 12) and toggling suspension states (Open, Trail, Lock) mutate local `StateFlow` variables. There are currently `// TODO` comments stubbed out where the actual GATT `writeCharacteristic()` commands belong.
*   **E-Bike Telemetry (`DemoModeSimulator.kt`):** Fakes active E-bike connections, draining the UI battery meter and generating randomized motor wattage (210W - 290W) to keep the dashboard gauges active.

---

## 2. Can it connect to a Bike GATT server?
**Architecturally, YES. Physically, NOT YET.**

The application is structurally ready to connect to external hardware, but the final wiring has been intentionally decoupled for the current release.

### ✅ The Good (What is ready):
1. **Permissions are configured:** `AndroidManifest.xml` correctly declares `BLUETOOTH_SCAN` (with `neverForLocation` to protect user privacy) and `BLUETOOTH_CONNECT`. 
2. **Foreground Service Safety:** We intentionally removed `FOREGROUND_SERVICE_CONNECTED_DEVICE` from the manifest. This means when AshBike connects to a bike GATT server in the future, it will do so via standard OS background BLE threads rather than forcing Google Play to audit a strict hardware-bound Foreground Service.
3. **Core Bluetooth Engine exists:** The `:core:data` module contains a massive, 900+ line fully functional `BluetoothLeRepoImpl.kt` capable of scanning, authenticating, and mapping GATT Characteristics. 
4. **MVI Architecture:** The UI is completely decoupled. The Jetpack Compose UI listens to generic `Flow<Int>` and `Flow<BikeRideInfo>`. The UI does not know or care if the data comes from a simulator or a real Shimano/Bosch E-bike motor.

### 🚧 The Gap (What needs to be done):
To connect to a real bike GATT server or chest strap, a developer must:
1. **Swap the Dependency Injection (Hilt):** Update `MobileSensorModule.kt` to bind a real BLE repository (like `BluetoothLeRepoImpl`) instead of `BleHeartRateRepository`.
2. **Implement the Heart Rate Callback:** Update `BleHeartRateRepository` to scan for Service UUID `180D` (Standard Heart Rate), connect to the GATT server, and subscribe to notifications on Characteristic `2A37`.
3. **Implement Bike Writes:** Replace the `// TODO: Send bluetooth command` lines in `BikeRepositoryImpl.kt` with actual GATT characteristic writes to the bike's proprietary Service UUIDs to control electronic shifting and suspension lockouts.

---

## 3. Summary
AshBike is a "Mocked-Hardware" application right now. The UI, permissions, and architectural pipelines are production-ready, but the physical radio layers are stubbed to simulators.