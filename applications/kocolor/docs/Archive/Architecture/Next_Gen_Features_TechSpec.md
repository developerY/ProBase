# KoColor Technical Specification: Next-Generation Feature Roadmap

This document outlines the technical specifications for four high-impact, architecture-aligned feature expansions. These features are designed to leverage KoColor's existing GenAI, Atmospheric, Inventory, and Store pipelines to drive deeper user engagement and monetization.

---

## 1. Destination Capsule Architect (Travel Packing AI)

### Overview
A personalized travel packing assistant. The user provides a destination, travel dates, and an occasion (e.g., "Ski Trip in Aspen" or "Business Conference in London"). The AI evaluates the multi-day weather forecast and generates a highly optimized "Capsule Wardrobe"—minimizing the total number of items while maximizing outfit combinations.

### Technical Architecture
- **Geocoding & Atmospheric Pipeline**: 
  - Extend `WeatherRepo` to support querying multi-day forecasts via city name or geocoded Lat/Lng instead of just the device's current location.
  - Create a new `fetchTravelForecast(destination: String, startDate: Long, endDate: Long)` in `AtmosphericRepository`.
- **GenAI Routing (`GeneratePlaylistUseCase`)**:
  - Introduce a `TravelCapsuleRequest` model that extends the standard `StyleRequestContext` to include array-based weather contexts.
  - Modify `StyleSimulatorEngine` to execute a multi-turn or batched generation, scoring candidate items heavily on "Versatility" (e.g., items that can be worn across multiple days/occasions).
- **Data Models**:
  - `CapsuleBlueprint`: Contains a subset `Set<ClothingItem>`, `Set<CosmeticItem>`, and a `Map<LocalDate, StyleBlueprint>` for daily outfit mapping.
- **UI/UX Integration**:
  - A new `TravelPlannerScreen` flow with a visual checklist. Users can "lock" specific items they want to bring and let the AI fill the rest of the suitcase.

---

## 2. Dynamic Skin Defense Alerts (Real-time UV & Weather Context)

### Overview
Proactive, hyper-personalized skincare push notifications based on real-time environmental shifts. If the UV Index spikes or humidity drops drastically, the app cross-references the user's vanity to suggest applying specific owned products.

### Technical Architecture
- **Background Processing (`WorkManager`)**:
  - Implement a `WeatherMonitorWorker` scheduled to run periodically (e.g., every 2 hours during daylight).
  - The worker invokes `AtmosphericRepository.fetchWeatherIfNeeded()`.
- **Trigger Logic Engine**:
  - Define threshold rules:
    - *High UV (UV Index > 7)* -> Query `CosmeticInventoryRepository` for `macroCategory == "PREP" && notes.contains("SPF")`.
    - *Low Humidity (< 30%)* -> Query for `macroCategory == "PREP" && notes.contains("Hydration")`.
- **Notification Dispatcher**:
  - Utilize `NotificationManagerCompat` to dispatch localized alerts: *"The UV Index just hit 8 in your area. Don't forget to reapply your KoColor Mineral SPF 50!"*
- **Permissions**:
  - `android.permission.POST_NOTIFICATIONS`
  - (Optional) `ACCESS_BACKGROUND_LOCATION` if dynamic geographical updates are strictly required, though using the last known static location minimizes battery drain and policy friction.

---

## 3. "Project Pan" & Smart Replenishment (Monetization Loop)

### Overview
Leveraging the beauty community's "Project Pan" trend (using up products completely to avoid waste). The app tracks product depletion and automatically suggests 1-tap restocks or seasonal upgrades from the KoColor Store when an item is running low or expiring.

### Technical Architecture
- **Inventory Telemetry (`CosmeticDao`)**:
  - Utilize existing fields: `usageCount`, `fillLevel`, and `estimatedExpiry`.
  - Create an SQL query/Flow: `SELECT * FROM cosmetics WHERE fillLevel < 0.1 OR estimatedExpiry < :thirtyDaysFromNow`.
- **UI/UX Integration (`VanityLandingScreen`)**:
  - Introduce a "Project Pan / Empties" section displaying near-empty or expired items.
- **Store Sync (`StoreViewModel` & `PackSyncRepository`)**:
  - For each nearly empty item, the app queries the `StarterPackRepository` manifest for a 1:1 replacement or a mathematically similar `CosmeticItemDto` based on the user's `FashionProfile` (e.g., suggesting a "Winter Cool" foundation if their current one is expiring in November).
  - Display a 1-tap "Restock" button that adds the remote product ID directly to the local Shopping Cart (`ShoppingCartItemEntity`).

---

## 4. "Style Aura" OOTD Social Export

### Overview
A viral marketing tool that allows users to export their daily AI-generated or manually curated look into a highly aesthetic, shareable graphic for Instagram, TikTok, or Pinterest.

### Technical Architecture
- **Rendering Engine (Compose `GraphicsLayer`)**:
  - Create a custom, off-screen Composable (`StyleAuraCard`) that aggregates the `StyleBlueprint`.
  - Incorporate the `WeatherMoodGradient` as the background, plot the exact hex color swatches in a minimalist row, and overlay the `FashionistaScore` gauge.
  - Utilize Compose 1.6+ `GraphicsLayer` or the `drawToBitmap()` extension to capture the entire composable node hierarchy into an Android `Bitmap`.
- **Media Storage & Sharing**:
  - Save the generated `Bitmap` to the app's `cacheDir` via `MediaStore` or `FileProvider`.
  - Trigger an Android `Intent.ACTION_SEND` (ShareSheet) with `type = "image/png"`.
- **Virality Injection**:
  - Automatically watermark the exported image with the KoColor logo and the user's `FashionProfile` signature (e.g., "Styled by KoColor AI • True Winter • 98% Harmony").