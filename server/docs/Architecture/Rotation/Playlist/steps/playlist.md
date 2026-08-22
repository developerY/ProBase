# KoColor: V2 Product Strategy & Architecture Vision

This document outlines the strategic product vision and technical architecture for the next evolution of KoColor. By shifting the paradigm from a static digital inventory to a dynamic, algorithmic "streaming" model, KoColor aims to become the definitive unified ecosystem for fashion and cosmetic styling.

---

## 1. The Strategic Vision: "Spotify for the Closet"

The digital wardrobe market is fragmented into manual organization utilities and erratic generative AI wrappers. KoColor circumvents these pain points by adapting the mechanics of audio streaming—discovery, algorithmic curation, and frictionless user engagement—to visual commerce.

* **Tracks:** Individual digitized garments, accessories, and cosmetics.
* **Playlists:** Context-aware, fully generated outfits.
* **The Crossfade:** Cosmetics acting as the visual bridge harmonizing the user's natural features with their chosen garments.
* **Discover Weekly:** A proactive, 7-day calendar-integrated style forecast.

This generative layer is strictly bound by the established V1 deterministic architecture (Rotation Scoring and Penalty Matrices) to guarantee that all algorithmic recommendations remain mathematically wearable and financially optimized.

---

## 2. Core Architecture: Edge AI Phenotype Calibration

To provide flawless personalization before a single garment is uploaded, the application establishes the user's mathematical aesthetic baseline via a secure, zero-cloud onboarding flow.

### 2.1 Biometric Ingestion & Privacy

* **Lighting Validation:** The engine utilizes ambient light sensors and camera exposure metrics to ensure natural lighting, preventing artificial color casting.
* **Zero-Cloud Processing:** Using Android CameraX and an on-device ML model, facial landmarks are mapped to extract color hex codes from the skin surface, iris, and natural hair root. Frames are processed entirely in memory and immediately discarded.

### 2.2 The Classification Pipeline

1. **Undertone & Brightness:** Calculates whether the skin pulls warm (yellow/peach) or cool (blue/pink) and measures overall depth.
2. **Contrast Delta:** Measures the luminance difference between the skin, eyes, and hair (e.g., pale skin with dark hair = high contrast).
3. **12-Season Matrix Mapping:** Vectors are deterministically mapped to a specific seasonal palette (e.g., *Soft Summer*, *Deep Autumn*).

### 2.3 The Cosmetic Crossfade

The established phenotype acts as the master "equalizer." If a user selects a garment that violates their color harmony, the engine queries the `CosmeticItemEntity` database (the Virtual Vanity) and recommends specific makeup shades to artificially bridge the aesthetic gap.

### 2.4 Domain Model

```kotlin
enum class ColorSeason {
    BRIGHT_SPRING, TRUE_SPRING, LIGHT_SPRING,
    LIGHT_SUMMER, TRUE_SUMMER, SOFT_SUMMER,
    SOFT_AUTUMN, TRUE_AUTUMN, DEEP_AUTUMN,
    DEEP_WINTER, TRUE_WINTER, BRIGHT_WINTER
}

data class FacialContrastVector(
    val skinLuminance: Float,
    val hairLuminance: Float,
    val eyeLuminance: Float,
    val contrastDelta: Float
)

data class PhenotypeProfile(
    val season: ColorSeason,
    val undertone: Float,
    val contrastVector: FacialContrastVector,
    val optimalPaletteHexCodes: List<String>
)

```

---

## 3. Core Architecture: 7-Day Forecasting Engine

Upgrading from a single-day reactive utility, KoColor will operate a proactive batch-processing engine that transforms meteorological and scheduling data into a cohesive weekly style playlist.

### 3.1 Ingestion & Context Normalization

* **Meteorological Bounds:** Ingests a 7-day forecast (temperature range, precipitation) to establish hard thermal limits and fabric-safety filters in the penalty matrix.
* **Calendar NLP:** Scans device calendar events, translating raw strings into structured context tags (e.g., "Board Review" -> `[Context: Formal Work]`).

### 3.2 The Sequential Pipeline

The engine evaluates the week sequentially (Monday to Sunday). When an item is selected for Day $N$, a simulated usage event is written into the in-memory rotation state. This immediately enforces the V1 48-hour hard cooldown across Days $N+1$ and $N+2$, ensuring the generated week is inherently diverse.

```mermaid
graph LR
    A[Sunday Trigger] --> B[Fetch Weather & Calendar]
    B --> C[Sequential Daily Loop: Day 1..7]
    C --> D[Simulate Wear Commitments]
    D --> E[Apply 48h Cooldown to Days N+1, N+2]
    E --> F[Generate Multi-Context Remixes]
    F --> G[Persist Weekly Style Playlist]

```

### 3.3 Contextual Routing & Remixes

For days containing multiple conflicting events (e.g., a corporate workday followed immediately by an evening date), the engine generates a base outfit with a "Remix Delta." Instead of two entirely disconnected outfits, it suggests low-friction swaps (e.g., changing footwear, dropping the blazer, and shifting to a bold lip color).

### 3.4 Domain Model

```kotlin
data class DailyStylePlan(
    val date: LocalDate,
    val weatherContext: WeatherCondition,
    val primaryContext: LifestyleContext,
    val baseOutfit: OutfitManifest,
    val eveningRemix: RemixDelta? = null,
    val cosmeticCrossfade: CosmeticPalette
)

data class WeeklyStylePlaylist(
    val weekStartDate: LocalDate,
    val days: List<DailyStylePlan>,
    val projectedWardrobeUtilization: Float
)

```

---

## 4. Integration with V1 Analytics (Style Wrapped)

The rigorous behavioral data tracked by the V1 `ClothingUsageEntity` will be synthesized into highly shareable, viral aesthetic summaries.

* **Wardrobe Heroes (Top Played Tracks):** Visualizes the items with the highest algorithmic rotation and manual selection scores.
* **The Investment Portfolio:** Rebrands standard Cost-Per-Wear (CPW) into a celebration of high-ROI purchases.
* **Style Eras:** Uses historical context tags to define the user's shifting aesthetic personas over the quarter.