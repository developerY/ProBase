# AshBike Telemetry & Calculations

This document explains the mathematical algorithms and business logic used to calculate bike ride telemetry (Distance, Speed, Time, Elevation, and Calories) in the AshBike application. 

The core calculation engines are located in:
*   `CalculateCaloriesUseCase.kt`
*   `RideStatsUseCase.kt`

---

## 1. Time & Duration
**Engine:** `RideStatsUseCase.sessionFlow`

To prevent massive negative or positive time jumps caused by the user changing timezones or the cellular network updating the device's clock mid-ride, AshBike does **not** use `System.currentTimeMillis()` for durations.

Instead, the app captures `android.os.SystemClock.elapsedRealtime()` at the start of the ride. 
*   **Elapsed Time:** `currentRealtime - startRealtime`

---

## 2. Distance
**Engine:** `RideStatsUseCase.distanceKmFlow`

Distance is calculated by continuously accumulating the delta between consecutive GPS `Location` emissions.

*   **Formula:** `prevLocation.distanceTo(currLocation) / 1000f` (Converted to Kilometers).
*   **Jitter Filter:** To prevent "phantom distance" when a rider is stationary (due to GPS satellite bounce), the algorithm ignores any position change of less than **3.0 meters**. 

---

## 3. Speed
**Engine:** `RideStatsUseCase`

### Maximum Speed (`maxSpeedFlow`)
A running state that inspects every emission from the speed flow (which comes from either the GPS location speed or a BLE sensor). It holds the peak value using `maxOf(maxSoFar, currentSpeed)`.

### Average Speed (`averageSpeedFlow`)
Averaging a stream of speed samples is mathematically inaccurate (especially if stopped at a light). Instead, true average speed is strictly calculated against total work done over time.
*   **Formula:** `Total Distance (km) / Elapsed Time (hours)`
*   **Safety:** Safely handles zero-elapsed-time to prevent `NaN` or `Infinity` crashes.

---

## 4. Elevation
**Engine:** `RideStatsUseCase.elevationGainFlow` & `elevationLossFlow`

Elevation gain and loss are separated into two distinct accumulating flows based on GPS `altitude`.
*   **Gain:** If `current.altitude > previous.altitude`, the delta is added to the total Elevation Gain.
*   **Loss:** If `current.altitude < previous.altitude`, the delta is added to the total Elevation Loss.

---

## 5. Calories (Caloric Burn Model)
**Engine:** `CalculateCaloriesUseCase.kt`

Calories are calculated on a "tick-by-tick" basis using a Metabolic Equivalent of Task (MET) curve based on the rider's current speed and weight.

### The Math
For every tick (delta in milliseconds between updates), the app calculates:
`tickCalories = MET * userWeightKg * deltaHours`

**CRITICAL DELTA EMISSION:** To prevent "geometric progression" bugs (e.g. calories jumping to 100,000 in 15 minutes), `CalculateCaloriesUseCase` strictly emits **only the delta burned during the current tick** (`tickCalories`). It does *not* emit a running total. The downstream services (`BikeForegroundService` and `RideStatsUseCase`) take these delta emissions and accumulate them safely using `+=` or `.scan { acc, delta -> acc + delta }`.

### The MET Curve
AshBike uses an expanded aerodynamic curve to estimate effort based on speed (km/h):
*   **< 16 km/h:** 4.0 MET *(Leisurely / Warmup)*
*   **< 19 km/h:** 6.0 MET *(Light effort)*
*   **< 22 km/h:** 8.0 MET *(Moderate)*
*   **< 26 km/h:** 10.0 MET *(Vigorous)*
*   **< 32 km/h:** 12.0 MET *(Very fast / Fast Club Ride)*
*   **< 38 km/h:** 15.8 MET *(Racing pace)*
*   **< 45 km/h:** 19.0 MET *(Elite Time Trial / Breakaway)*
*   **< 55 km/h:** 24.0 MET *(Pro Lead-out / Fast Descent)*
*   **< 65 km/h:** 30.0 MET *(Pro Sprint)*
*   **< 75 km/h:** 38.0 MET *(Motor-paced / Steep Descent)*
*   **< 80 km/h:** 45.0 MET *(Extreme Mountain Descent)*
*   **80+ km/h:** 55.0 MET *(Absolute free-fall)*

### Caloric Gatekeepers & Guards
To prevent falsely inflating calorie burn counts, the use case has three strict guards:
1.  **The Pause Guard:** If a gap between updates exceeds 30 seconds, that time block is ignored. This safely accommodates slow 10-20s GPS intervals without dropping tracking, but ignores massive jumps.
2.  **The Stoplight Guard:** If `currentSpeedKmh <= 0f`, the algorithm emits `0f` calories and advances the internal timer. You do not burn active cycling calories while waiting at a red light.
3.  **The Phantom Speed Guard:** If `currentSpeed > 0` but `deltaDistance <= 0`, the algorithm emits `0f` calories. This protects against false speed readings from GPS bounce while completely stationary. The elapsed time is held in reserve until actual distance is covered, ensuring the accumulated time delta is correctly applied to the subsequent movement without double-counting.