# State Management in AshBike

AshBike heavily leverages modern Android Architecture components to manage UI state, hardware telemetry, and database persistence.

## MVI (Model-View-Intent) Architecture

The UI layer is strictly driven by the MVI pattern.

1. **State (`UiState`)**: Each major screen (e.g., `HomeUiState`, `SettingsUiState`) represents its entire viewable state as an immutable Kotlin Data Class. The Compose UI observes these states using `collectAsStateWithLifecycle()`.
2. **Intent (`Event`)**: User actions (button clicks, swipes) and system triggers are modeled as discrete events (e.g., `HomeEvent.StartRide`, `HomeEvent.ToggleGlassProjection`).
3. **ViewModel**: The `ViewModel` acts as the single source of truth for the screen. It receives `Events`, mutates the underlying flows, and emits a new `UiState`.

## Flow Chains

Because AshBike tracks highly dynamic hardware data (GPS, BLE, Heart Rate), it relies on heavily chained Kotlin Coroutine `Flows`.

*   **Hot Flows (`StateFlow` / `SharedFlow`)**: Used for emitting live telemetry (Speed, Heading, Battery Level) from the `BikeForegroundService`.
*   **Combiners**: Complex UI states are built by combining multiple downstream flows. For example, `RideStatsUseCase.sessionFlow` combines the `distanceFlow`, `speedFlow`, `caloriesFlow`, and `elevationFlow` into a single `RideSession` emission.
*   **DistinctUntilChanged**: Extensively used across the flow chains to prevent unnecessary UI recompositions in Jetpack Compose when sensor data emits redundant values.

## The Foreground Service

The `BikeForegroundService` is the heartbeat of the application. It runs independently of the Activity lifecycle. 

When the user opens the app, the `MainActivity` binds to this service to observe the live `StateFlow`s. If the user backgrounds the app, the service continues to execute the mathematical calculations and write to the Room database, ensuring no ride data is dropped.
