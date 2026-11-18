# AshBike Bluetooth & Sensor Integration

AshBike connects to e-bikes and external fitness sensors via Bluetooth Low Energy (BLE). This document outlines the architecture for managing these connections across the phone and Wear OS companion app.

## Bluetooth Engine

The Bluetooth connection logic is encapsulated in the data layer (e.g., `MobileLocationBleEngine` and `WearRideSyncEngine`). 

### Core Responsibilities
1. **Scanning**: Discovering the e-bike and authorized sensors.
2. **Connecting**: Establishing the GATT connection.
3. **Data Extraction**: Subscribing to characteristics for speed, cadence, battery level, and heart rate.
4. **State Management**: Updating the `RideState` and `BikeRideInfo` flows to reflect the active hardware telemetry.

## Permissions Required

The Bluetooth stack requires the following runtime and manifest permissions:
*   `BLUETOOTH_SCAN`
*   `BLUETOOTH_CONNECT`
*   `ACCESS_FINE_LOCATION` (Required on some Android versions for BLE scanning).

## Hardware Handoff (Phone ↔ Wear OS)

The application supports dual-tracking. If the phone loses connection to the bike or the user starts a ride solely from their watch:
*   The Wear OS app (`AshBikeWearApp`) runs as a standalone `BikeForegroundService`.
*   The Wear app directly interfaces with the watch's internal heart rate sensor via Health Services (requiring `BODY_SENSORS`).
*   Upon reconnection, the Wear app uses the Wearable Data Layer (`RideAckListenerService` and `WearRideSyncEngine`) to sync the orphaned ride telemetry back to the phone's Room database for persistence.

## Fallback Modes
If the BLE connection to the e-bike drops mid-ride, the application immediately falls back to using the device's internal GPS (Location Services) to calculate speed and distance, ensuring the ride is not lost.
