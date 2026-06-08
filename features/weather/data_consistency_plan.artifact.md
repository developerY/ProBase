# Implementation Plan - Weather Data Consistency

I will synchronize the weather and environmental data across the Home screen, Weather dashboard, and Sun Intelligence hub to ensure the user sees consistent real-time information regardless of the entry point.

## 1. Research & Analysis
- **Problem**:
    - Home screen shows "WEEHAWKEN" data (possibly from GPS).
    - Weather screen falls back to "SANTA BARBARA" (possibly due to GPS timeout or logic difference).
    - Sun Intelligence shows different UV levels (mock vs real).
- **Goal**:
    - All screens must use the same location resolution logic.
    - Remove all hardcoded "8.0" UV mocks.
    - Ensure `WeatherViewModel` fetches the full `EnvironmentalContext` (including UV).

## 2. Technical Steps

### Core Model & State Updates
- [ ] **`WeatherUiState.kt`**: Add `environmentalContext` to `Success` state (Done).
- [ ] **`WeatherViewModel.kt`**:
    - Implement real coordinate fetching using `LocationRepository`.
    - Parallel fetch for `OpenWeatherResponse` and `EnvironmentalContext` (UV).
    - Ensure fallback coordinates match `HomeViewModel` (Santa Barbara).

### UI Integration
- [ ] **`WeatherScreen.kt`**:
    - Remove hardcoded `uvIndex = 8.0` (Done).
    - Map `uvIndex` from the state's `environmentalContext` (Done).
- [ ] **`SunIntelligenceScreen.kt`**:
    - Ensure it uses the same coordinate-based fetching as the other two screens.

### Navigation Fixes
- [ ] **`WeatherUiRoute.kt`**: Propagate the new `environmentalContext` to the screen (Done).

## 3. Visual & Aesthetic Standards
- **Sync Visuals**: The "Atmospheric Metrics" on the Weather screen will now exactly match the "Sun Intelligence" hero gauge since they use the same live data source.

## 4. Verification
- [ ] Verify that all 3 screens (Home card, Weather hero, Sun hub) show the same temperature and location.
- [ ] Verify that the UV index on the Weather dashboard matches the detailed Sun Intelligence hub.
- [ ] Test with GPS enabled to ensure "WEEHAWKEN" (or current location) propagates to all screens.
- [ ] Build and run `:applications:kocolor:apps:mobile`.

---
<!-- feedback_request -->
I've already begun the synchronization by removing the UV mocks and updating the weather state. I will now perform a final sweep to ensure the GPS resolution logic is identical across all ViewModels.

**Should I proceed with the final data synchronization?**
