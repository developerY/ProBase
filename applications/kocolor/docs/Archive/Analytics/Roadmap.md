Executing this architecture requires a four-phase rollout, beginning with the local Room database schema and ending with the Gemini LLM pipeline injection.

### Phase 1: The Observation & Data Layer (Room)

Before drawing any UI, the deterministic foundation must be strict and typed.

* **Schema Expansion:** Update your `ClothingItem` Room entity to support the financial and temporal metrics: `purchasePrice: Double`, `purchaseDate: Long`, and a relational `WearEvent` table to track the exact `timestamp` of every usage.
* **Coverage Engine:** Build an `AnalyticsCoverage` repository that queries the total records and identifies gaps (e.g., calculating what percentage of the wardrobe has a `purchasePrice` assigned).
* **Deterministic Services:** Create dedicated Kotlin domain services (`PortfolioEngine`, `WardrobeAnalyticsEngine`) injected via Hilt to calculate CPW, rotation health, and category distributions on a background thread.

### Phase 2: Act I & II UI Implementation (Jetpack Compose)

Translate the data into the premium editorial aesthetic using Jetpack Compose, strictly avoiding Material `Card` components.

* **Custom Canvas Components:** Build the three signature visualizations (`ColorHistoryThread`, `WearDistributionPlot`, `DynamicChromaticCore`) using native Compose `Canvas` to ensure smooth rendering of hundreds of data points without UI thread lag.
* **Interactive Drill-Downs:** Implement the `detectTapGestures` logic on the Canvas elements, tying the touch coordinates back to the underlying `ClothingItem` to reveal the transient detail overlay.
* **The Dashboards:** Assemble `StyleIntelligenceScreen` (Portfolio/Footprint) and `WardrobeAnalyticsScreen` (Behavior) using `FontFamily.Serif` for headers, generous whitespace, and delicate horizontal dividers.

### Phase 3: Act III LLM Context Injection (Gemini)

The final step wires the deterministic math directly into your AI generative pipeline.

* **Context Serialization:** Update the `StyleContextSummary` to accept the new `WardrobeDNA`, `RotationHealth`, and `WardrobeOpportunities` data classes.
* **Prompt Architecture:** Modify the prompt template sent to the Gemini API so the LLM is explicitly instructed to penalize over-worn items and prioritize the actionable gaps identified by the deterministic engine.

---

**AI IDE Prompt:**

```text
Execute the "Three-Act" KoColor Fashion Intelligence System architecture. 

1. Domain & Data Layer (Room & Hilt):
   - Update `ClothingItem` entity with `purchasePrice: Double?` and `purchaseDate: Long?`. 
   - Create a `WearEvent` entity (timestamp, clothingItemId, colorHex) to track historical usage.
   - Build an `AnalyticsCoverage` data class and service to calculate the observation window (oldest vs. newest wear event) and data completeness (percentage of items with price data).

2. Act I: The Footprint (`StyleIntelligenceScreen.kt`):
   - Build a Jetpack Compose screen using a premium editorial aesthetic (pure white background, `FontFamily.Serif` headers, 1f `HorizontalDivider`).
   - Implement a "Portfolio Performance" section calculating Total Wardrobe Value and Average Cost Per Wear (CPW).
   - Implement a `DynamicChromaticCore` composable: a horizontally scrolling, interactive single-band color spectrum of all wardrobe hex codes.

3. Act II: The Behavior (`WardrobeAnalyticsScreen.kt`):
   - Display the `AnalyticsCoverage` metrics at the very top to establish data trust.
   - Implement the `WearDistributionChart` using a Compose `Canvas`. Plot garments by frequency. Map X to frequency rank, Y to total wears, and draw dots using the garment's exact hex color.
   - Implement the `ColorHistorySection` using a `Canvas` to plot `WearEvent` timestamps chronologically on a single horizontal axis with overlapping, translucent dots (`alpha = 0.7f`).

4. Act III: The Synthesis (LLM Context):
   - Update `StyleContextSummary` to include `rotationHealthScore`, `dnaProfile`, and `wardrobeGaps`. Ensure this deterministic data is formatted cleanly into the prompt string passed to the Gemini API for outfit generation.

```