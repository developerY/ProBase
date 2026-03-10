
# AshBike: Glass Feature (Android XR / Glimmer)

The `features/glass` module contains the implementation for the AshBike Heads-Up Display (HUD), designed specifically for the **Google / Samsung AI Glass** ecosystem.

## Overview

Unlike standalone XR applications, the AshBike Glass feature utilizes a **Phone-hosted, Projected-context** architecture. All heavy application logic and data processing (GPS, sensor fusion, and AI) execute on the mobile device to preserve the glasses' battery and thermal profile, while the UI is projected to the monocular display using the **Android XR (Baklava/15+)** APIs.

## Architecture & Logic

* **Projected Context:** The HUD is launched via `ProjectedContext`, targeting the external glass display. It bypasses traditional mirroring to provide a purpose-built, high-contrast overlay.
* **HomeViewModel Integration:** * Manages the `HomeEvent.ToggleGlassProjection` state.
* Tracks `glassButtonState` (NO_GLASSES, READY_TO_START, PROJECTING).
* Synchronizes real-time cycling data (Gears, Speed, Heart Rate) from the phone to the glass overlay.


* **Multimodal Input:** Supports voice-activated gear shifting and UI navigation, allowing for a "hands-on-bars" experience.

## Technical Stack

* **Jetpack Compose XR / Glimmer:** Uses the Glimmer design system for glanceable, high-contrast UI components optimized for see-through optical displays.
* **Jetpack Projected SDK:** Handles the lifecycle of the activity as it moves from the phone's local context to the Glass projection.
* **Kotlin Coroutines & Flow:** Used for low-latency state synchronization between the bike sensors and the HUD.

## Key Files

* `GlassesMainActivity.kt`: The entry point for the projected glass experience.
* `GlassUiState.kt`: Defines the immutable state for the HUD, including gear position and biometric data.
* `HomeUiRoute.kt`: Contains the `LaunchedEffect` logic that monitors `BikeSideEffect.LaunchGlassProjection` to trigger the external display intent.

## Implementation Details

```kotlin
// Example of the Projection Launch Logic
val isConnected by viewModel.isGlassConnected.collectAsState()

LaunchedEffect(sideEffect) {
    if (sideEffect is BikeSideEffect.LaunchGlassProjection) {
        // Targets Android 15+ (Baklava) External Glass Display
        val intent = Intent(context, GlassesMainActivity::class.java)
        ProjectedContext.launch(intent) 
    }
}

```

## UI Principles (Glimmer)

Following the Glimmer design language, this feature adheres to:

1. **Additive Color:** Pure black (#000000) is rendered as transparent to ensure the rider's vision is never obstructed.
2. **Glanceability:** Information is prioritized in the periphery of the monocular display.
3. **High Contrast:** Uses bold outlines and vibrant accents to remain legible in varying outdoor light conditions.

---

*This module is a core component of the AshBike production-grade Android system, unifying Mobile, Wear OS, and AI Glass experiences.*