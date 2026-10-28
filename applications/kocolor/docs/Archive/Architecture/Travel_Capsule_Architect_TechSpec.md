# KoColor Technical Specification: Destination Capsule Architect (Travel Packing AI)

## Overview
The **Destination Capsule Architect** is a personalized AI travel assistant integrated into KoColor. The user inputs their travel destination, trip occasion, and duration. The system leverages KoColor’s multi-node infrastructure (Atmospheric context, Wardrobe/Cosmetic DBs, and the Style Simulator Engine) to generate a hyper-optimized "Capsule Wardrobe." It packs the exact items needed for the anticipated weather, flags any required travel documents (passports, visas), and curates cosmetics tailored to the climate.

---

## 1. System Architecture & Components

The feature spans across four primary modules:
1. **`AtmosphericRepository` (Data Layer)**
2. **`TravelCapsuleViewModel` (Presentation/Logic Layer)**
3. **`StyleSimulatorEngine` & `GenerateStyleResultUseCase` (AI Engine Layer)**
4. **`TravelCapsuleScreen` (UI Layer)**

### High-Level Data Flow
1. **Input:** User inputs Destination, Occasion, and Dates in the `TravelCapsuleScreen`.
2. **Atmospheric Context:** `TravelCapsuleViewModel` calls `fetchTravelForecast(destination)` in the `AtmosphericRepository`.
3. **Intent Profiling:** The `IntentAnalyzer` interprets the combination of Occasion + Destination Weather to create a mathematical `StyleIntentProfile` (evaluating formality, thermal needs, color depth).
4. **AI Generation:** `GenerateStyleResultUseCase` filters the user’s local `WardrobeRepository` and `CosmeticInventoryRepository` against the forecast and intent, delegating to the `StyleSimulatorEngine`.
5. **Output Generation:** The AI returns a `StyleBlueprint` with selected IDs, which are matched to local entities and displayed.
6. **Regulatory Check:** The ViewModel evaluates string heuristics on the destination to trigger international travel warnings.

---

## 2. Component Implementation Specifications

### A. Atmospheric Context Retrieval (`AtmosphericRepository.kt`)
A new method `fetchTravelForecast` allows the app to bypass the physical device GPS and query OpenWeather/OpenMeteo for arbitrary global destinations.

```kotlin
suspend fun fetchTravelForecast(destination: String, startDate: Long, endDate: Long): AtmosphericState? {
    // 1. Queries weatherRepo.openCurrentWeatherByCity(destination)
    // 2. Extracts coordinates from the response (coord.lat, coord.lon)
    // 3. Queries weatherRepo.getEnvironmentalContext(lat, lon) for UV/Humidity.
    // 4. Returns an AtmosphericState combining both models.
}
```

### B. Travel Logic & State Management (`TravelCapsuleViewModel.kt`)
Acts as the central orchestrator.

**State Model (`TravelCapsuleUiState`)**
Maintains the configuration and the final output:
- `destination: String`
- `occasion: String`
- `isInternational: Boolean` (Triggers document warnings)
- `weatherForecast: String`
- `recommendedClothing: List<ClothingItem>`
- `recommendedCosmetics: List<CosmeticItem>`

**Execution Sequence (`generateCapsule`)**:
1. Checks if the destination matches known international heuristics via regex (e.g., `paris|tokyo|london`).
2. Fetches the exact temperature and weather string (e.g., `22f`, `"Clear"`) from `AtmosphericRepository`.
3. Constructs a `StyleRequestContext`:
   ```kotlin
   val context = StyleRequestContext(
       intent = "$occasion in $destination",
       occasion = occasion,
       intentProfile = intentAnalyzer.analyze("$occasion in $destination. Weather: $weatherDesc"),
       weather = weatherDesc,
       weatherTempC = temp
   )
   ```
4. Passes the entire local Wardrobe and Cosmetic list into `generateStyleResultUseCase.execute()`.

### C. Generative Engine Delegation (`StyleSimulatorEngine`)
The engine inherently supports the `StyleRequestContext`. By forcing `context.weather` and `context.weatherTempC` to match the *destination's* weather rather than the *current device* weather, the AI automatically applies its thermal and UV logic.

- **Clothing Evaluation**: Excludes heavy coats if destination temp is >20°C. Promotes formal wear if occasion is "Business".
- **Cosmetics Evaluation**: Promotes SPF if destination UV is high. Suggests waterproof formulations if destination weather indicates rain/humidity.

### D. User Interface (`TravelCapsuleScreen.kt`)
A Compose screen divided into two primary states using `AnimatedVisibility`:
1. **Configuration State:** OutlinedTextFields for Destination and Occasion, accompanied by a "Pack My Bags" button.
2. **Result State:**
   - **Weather Banner:** Displays the fetched temperature and condition.
   - **Document Warning:** A yellow `Warning` card that appears dynamically if `isInternational` is true: *"International Trip Detected! Don't forget your passport and check visa requirements."*
   - **The Capsule:** Renders horizontal or vertical lists of the `AsyncImage`s, names, and categories of the clothing and cosmetics selected by the AI.

---

## 3. Integration Requirements & Next Steps

To fully activate this feature within the application, the following system integrations are required:

1. **Navigation Graph Injection (`KoColorRoute.kt` & `KoColorNavEntryProvider.kt`)**:
   - Add `@Serializable data object TravelCapsule : KoColorRoute()` to the navigation sealed class.
   - Map the route in the `NavEntry` provider to instantiate `TravelCapsuleViewModel` via `hiltViewModel()` and render `TravelCapsuleScreen`.
2. **Dashboard Entry Point (`HomeScreen.kt`)**:
   - Add a "Plan a Trip" card or FAB on the main dashboard that fires `navTo(KoColorRoute.TravelCapsule)`.
3. **Date/Length Support**:
   - Currently, the architecture supports a generic `occasion`. To support length (e.g., 7 days), the `TravelCapsuleViewModel` will multiply the `GenerateStyleResultUseCase` constraints (e.g., forcing the `WardrobeCandidateFilter` to select `currentK = 7 * 1.5` items instead of a daily outfit).
