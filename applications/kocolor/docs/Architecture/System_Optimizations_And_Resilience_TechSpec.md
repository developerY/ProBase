# KoColor Technical Specification: System Resilience & Edge Cache Optimizations

This document details the architecture, data flows, and resilience mechanisms implemented across KoColor for production readiness, edge-cache pre-warming, security attestation, and data minimization policy compliance.

---

## 1. Edge-Cache Pre-Warming System

### Overview
To overcome backend cold-start delays on free/tier cloud hosting services, KoColor implements an **Edge-Cache Pre-Warming System**. This system wakes up CDN/edge cache nodes prior to user rendering without wasting mobile data or local device memory.

### Architecture & Mechanics

```
┌────────────────────────┐      HTTP HEAD Request (Headers Only)       ┌────────────────────────┐
│                        ├────────────────────────────────────────────►│                        │
│   KoColor Android App  │                                             │   Backend Edge Server  │
│   (OkHttpClient)       │◄────────────────────────────────────────────┤   / CDN Cache Node     │
│                        │       HTTP 200 OK (Tiny Header Payload)     │                        │
└────────────────────────┘                                             └───────────┬────────────┘
                                                                                   │
                                                                       Fetch & Cache Asset
                                                                       from Cold Storage
                                                                                   │
                                                                                   ▼
                                                                       ┌────────────────────────┐
                                                                       │   Cold Cloud Storage   │
                                                                       └────────────────────────┘
```

#### 1. HTTP `HEAD` Request Strategy
Unlike standard HTTP `GET` requests which download the entire binary payload (multi-megabyte high-resolution images), `StarterPackRepository.prefetchUrls()` issues **HTTP `HEAD` requests**.
- **Data Minimization:** Requests only response headers (~200 bytes per ping) instead of binary image bodies.
- **Server Cache Trigger:** Forces the server/CDN to fetch the resource from cold storage and cache it at the edge node.
- **Zero Local Waste:** The HTTP body is empty; no local storage or memory allocations are created on the user's phone.

#### 2. "Smart Math" Above-The-Fold Selection
To prevent network spam and respect device resources, pre-warming utilizes a **top-N slice heuristic**:
- Limits pre-warming to the **top 12 items per category** (`take(12)`).
- Targets the exact items that render "above the fold" in the user's initial screen viewport.
- Subsequent items are lazy-loaded naturally via Coil as the user scrolls.

#### 3. Lifecycle-Driven Pre-Warming Stages
The pre-warming engine triggers at strategic navigation junctures:
1. **Home Screen Startup (`HomeViewModel`)**:
   - `prewarmStoreData()` fetches the manifest on app launch and pre-pings top Store products in the background.
2. **Store Transition (`StoreViewModel`)**:
   - Intercepts `StoreEvent.EnterStore` during slide-in animations to pre-warm store catalog thumbnails.
3. **Starter Pack Catalog (`StarterPackViewModel`)**:
   - Pings pack `heroImageUrl`s immediately upon receiving new package manifests.
4. **Pack Preview (`PackPreviewViewModel`)**:
   - Pings the top 12 items in a specific pack on screen initialization.

---

## 2. Firebase App Check & Play Integrity Integration

### Overview
KoColor uses Firebase App Check to secure AI and Cloud endpoints (Vertex AI / Firebase AI Logic).

### Dual Provider Resolution Strategy (`AppCheckInitializer`)

```kotlin
val providerFactory = if (isEmulator && BuildConfig.DEBUG) {
    DebugAppCheckProviderFactory.getInstance()
} else {
    PlayIntegrityAppCheckProviderFactory.getInstance()
}
```

- **Debug Emulators:** Uses `DebugAppCheckProviderFactory` for local development on Android emulators.
- **Physical Real Devices & Release Builds:** Uses `PlayIntegrityAppCheckProviderFactory`.

### Benefits
- **Eliminates 403 Attestation Failures:** Real physical debug devices interact directly with Google Play Services to obtain Play Integrity attestations rather than failing on unregistered debug UUID tokens (`App attestation failed`).
- **Production Ready:** Play Store distribution automatically verifies app binary authenticity, Play Store provenance, and device integrity without manual token registration in the Firebase Console.

---

## 3. Zero-Item Wardrobe Resilience

### Overview
Ensures KoColor operates smoothly on fresh app installs where the user's wardrobe/vanity contains zero items.

### Architecture (`StyleSimulatorEngine`)
- **Exception Elimination:** Removed strict `IllegalStateException` throws when candidate selection yields 0 eligible items.
- **Graceful Fallback:** `adaptContextToProvider` logs a warning and returns `null` when selection pools are empty.
- **Deterministic Engine Routing:** Seamlessly routes 0-item requests to `DeterministicStyleEngine` (`HeuristicStyleEngine`), outputting a safe "Local Architect" baseline style blueprint without crashing or throwing exceptions.

---

## 4. Health Connect Data Minimization Policy Compliance

### Overview
To comply with Google Play's Health Connect Minimum Scope / Data Minimization policy, KoColor enforces strict scope alignment between manifest declarations, runtime permissions, and user-facing UI.

### Enforced Permissions
KoColor explicitly requests **ONLY** the 5 permissions corresponding to active UI features:
1. **`android.permission.health.READ_HYDRATION`** & **`WRITE_HYDRATION`**: Powers water intake tracking on Home & Settings.
2. **`android.permission.health.READ_STEPS`**: Powers physical movement telemetry for footwear suggestions.
3. **`android.permission.health.READ_SLEEP`**: Displays sleep duration telemetry on the main dashboard.
4. **`android.permission.health.READ_ACTIVE_CALORIES_BURNED`**: Correlates exertion with thermal & skin response.
5. **`android.permission.health.READ_TOTAL_CALORIES_BURNED`**: Displays daily metabolic energy expenditure.

### Stripped Permissions
All unneeded permissions from transitive dependencies are explicitly removed in `applications/kocolor/apps/mobile/src/main/AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.health.READ_EXERCISE" tools:node="remove" />
<uses-permission android:name="android.permission.health.READ_DISTANCE" tools:node="remove" />
<uses-permission android:name="android.permission.health.READ_HEART_RATE" tools:node="remove" />
<uses-permission android:name="android.permission.health.READ_NUTRITION" tools:node="remove" />
```

---

## 5. Weather & Atmospheric Fallback Pipeline

### Overview
Provides transparent fallback state handling when GPS or network weather endpoints time out or fail.

### Pipeline Flow
1. `AtmosphericRepository` requests location with a 5-second timeout.
2. If GPS is unavailable/timed out, it fetches Santa Barbara fallback weather and sets `isFallback = true`.
3. `LayeredWeatherMapper` sets `locationName = "Location could not be found"`.
4. `AtmosphericHeaderCard` checks `uiState.isLocationFallback` and explicitly renders `"Location could not be found"` on the expanded atmospheric header card.
