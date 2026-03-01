# AshBike Tracking Architecture

This document outlines the architectural approach for tracking bike rides across the AshBike Mobile (Phone) and Wear OS applications. 

To meet modern Android "Gold Standard" guidelines and ensure maximum battery efficiency, AshBike uses a **Shared Foreground Service** powered by **Platform-Specific Tracking Engines** via Dependency Injection.

## The Architectural Challenge
Wear OS smartwatches and Android mobile phones possess fundamentally different hardware and APIs:
* **Wear OS:** Highly battery-constrained. Requires the use of `ExerciseClient` (Health Services API) for optimized sensor fusion (GPS + onboard Heart Rate) and background batching.
* **Mobile:** Uses standard `LocationManager` / `FusedLocationProvider` and connects to external Bluetooth LE (BLE) chest straps for heart rate data.

Attempting to run a single, concrete tracking class across both platforms results in app bloat, missing hardware crashes, or severe battery drain on the watch.

## The Solution: Dependency Inversion 

We rely on the **Dependency Inversion Principle** using Hilt. We define a single `BikeForegroundService` in the shared `core` module. This service acts as the "Anchor" to prevent the OS from killing the app in the background, but it delegates all actual hardware tracking to an injected `RideTrackingEngine` interface.

### 1. The Core Interface (Shared Module)
The shared module defines the contract. The service doesn't know *how* the data is collected, only that it will receive a stream of data.

```kotlin
interface RideTrackingEngine {
    val currentHeartRate: StateFlow<Int>
    val currentLocation: StateFlow<LocationPoint?>
    
    fun startRide()
    fun stopRide()
}
```

### 2. The Shared Service (Shared Module)

This service handles the Android lifecycle, creates the persistent notification, and collects data from the injected engine to save to the Room Database.

```kotlin
@AndroidEntryPoint
class BikeForegroundService : Service() {

    // Hilt dynamically injects the correct hardware engine 
    // depending on which app (Wear or Mobile) is currently compiling.
    @Inject lateinit var trackingEngine: RideTrackingEngine 

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildPlatformNotification())
        trackingEngine.startRide()
        return START_STICKY
    }
}

```

## Platform Implementations

### Wear OS: The `ExerciseClient` Engine

On the watch, Hilt binds `WearExerciseClientEngine` to the interface.

* Uses the Wear OS `HealthServices` `ExerciseClient`.
* Requests `BIKING` exercise type.
* Offloads GPS and Heart Rate batching to the watch's dedicated low-power sensor chip.
* Generates the Wear OS specific "Ongoing Activity" indicator.

### Mobile: The Location & BLE Engine

On the phone, Hilt binds `MobileLocationBleEngine` to the interface.

* Uses `FusedLocationProviderClient` for high-accuracy phone GPS.
* Manages BLE GATT connections for external chest straps/armbands.

### Hilt Binding Example

In each respective app module, a Hilt module binds the correct implementation:

```kotlin
// In apps/wear/src/.../di/WearTrackingModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class WearTrackingModule {
    @Binds
    abstract fun bindTrackingEngine(impl: WearExerciseClientEngine): RideTrackingEngine
}

```

## Data Sync & Google Health Connect Pipeline

Because Google Health Connect is a mobile-only API, Wear OS cannot write to it directly. AshBike handles post-ride syncing via a 3-step pipeline:

1. **Track & Save Local:** The Wear OS app tracks the ride via `ExerciseClient` and saves the final `BikeRide` to the watch's local Room database.
2. **Wearable Data Layer Bridge:** The watch uses `PutDataMapRequest` to silently push the ride payload over Bluetooth to the paired phone.
3. **Global Sync:** The phone receives the payload, saves it to the mobile Room database, and triggers the `MobileHealthConnectManager` to write an `ExerciseSessionRecord` to Google Health Connect.

```