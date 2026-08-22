# Walkthrough: V1 Clothing Rotation System & Curated Closet UI

I have implemented the foundational architecture and premium UI for the KoColor "Curated Closet" rotation system. This full-stack scaffolding connects behavioral data (wear history) to intelligent styling penalties and a data-driven dashboard.

## 1. Data & Domain Layer (Room & Clean Architecture)

I established a robust data model that separates canonical product data from user-specific behavioral metrics.

### Key Components:
- **Room Entities**:
    - `GlobalRotationMetricsEntity`: Manages the global outfit counter to drive the "Cold Start" rule.
    - `ClothingUsageEntity`: Tracks `useCount` and `lastUsedTimestamp` per item.
    - `GarmentWithUsage`: A relation model that joins catalog data with usage metrics to eliminate N+1 queries.
- **Atomic Transactions**: The `RotationRepository` implements `commitOutfitUsage` inside a database transaction, ensuring that global and individual metrics remain synchronized.
- **Penalty Matrix (`RotationScoringUseCase`)**: Implemented the core logic for the AI engine:
    - **Cold Start**: Penalties are ignored until 5 outfits are logged.
    - **Hard Cooldown**: A 1.0 penalty is applied to any item worn within the last 48 hours.
    - **Frequency Penalty**: A 0.85 penalty is applied if an item's share of its category usage exceeds 35%.

## 2. Premium Compose UI (The 4-Screen Model)

I built a suite of four screens using a premium fashion-tech aesthetic characterized by minimalist backgrounds, heavy rounded corners, and editorial typography.

### Curated Closet (The Hub)
The central dashboard provides high-level "Portfolio" metrics.
- **Glow Score**: Intelligently switches from a percentage to "CALCULATING" during the Cold Start phase.
- **Visual Delineation**: Uses a holographic gradient for the "INTELLIGENCE" action and a deep forest green for "INVENTORY".

### Strategic Diversity (The Footprint)
Focuses on wardrobe architecture.
- Displays concentration bars showing where the user's financial investment and piece count are concentrated across categories.

### Usage Metrics (The Behavior)
A deep dive into garment utility.
- **Frequency Buckets**: Groups items by wear counts (Never, 1-5, etc.).
- **Resting State**: Explicitly highlights items currently in the 48-hour cooldown period.

### Style Intelligence (The Analysis)
The most advanced view, focusing on value and signature colors.
- **CPW Logic**: Implements the "Cost Per Wear" formula. Unused items are gracefully labeled as **"NOT DEPLOYED"** to avoid mathematical errors.
- **Chromatic Core**: Visualizes the user's most owned colors as a signature palette.

## 3. Styling & Integration
- **Visual Language**: All cards use `RoundedCornerShape(24.dp)` with subtle elevation on an off-white (`0xFFF9F9F9`) background.
- **Typography**: Uses `FontFamily.Serif` for piece counts and currency values to convey a high-end, editorial feel.
- **Navigation**: Fully integrated into the `KoColorNavEntryProvider` and accessible from the `WardrobeLandingScreen`.

---

### Artifacts Created/Modified:
- **Entities**: `GlobalRotationMetricsEntity.kt`, `ClothingUsageEntity.kt`, `GarmentWithUsage.kt`
- **Repository**: `RotationRepository.kt`, `RotationRepositoryImpl.kt`
- **Use Case**: `RotationScoringUseCase.kt`
- **Screens**: `CuratedClosetDashboard.kt`, `StrategicDiversityScreen.kt`, `UsageMetricsScreen.kt`, `StyleIntelligenceScreen.kt`
- **Integration**: `WardrobeLandingScreen.kt`, `KoColorNavEntryProvider.kt`
