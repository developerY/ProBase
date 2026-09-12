# The KoColor Fashion Intelligence System

KoColor operates beyond a standard digital closet. It functions as a **Personal Style Operating System**, separating raw inventory data from behavioral habits, and passing both through a deterministic engine to drive high-end, personalized AI recommendations.

This document details the complete architecture, data flow, and visual paradigm of the KoColor Fashion Intelligence System.

---

## 1. The Core Philosophy: Deterministic Math vs. AI Synthesis
The foundation of the analytics system is strict separation of concerns:
*   **Counting is Deterministic:** KoColor never uses LLMs (Gemini/Firebase AI) to count inventory, calculate cost-per-wear (CPW), or measure rotation frequency. The `WardrobeAnalyticsEngine` handles all raw math locally.
*   **Synthesis is AI:** The LLM is strictly used as the "Style Architect" to synthesize deterministic data (context, intent, rotation health) into a fluid, human-readable fashion rationale.

---

## 2. The Three-Act Analytics Architecture

The system is organized into a cohesive three-act story, presented across distinct editorial screens.

### ACT I: THE FOOTPRINT (What You Own) [file](applications/kocolor/features/inventory/src/main/java/com/zoewave/probase/kocolor/features/inventory/ui/analytics/WardrobeFootprintScreen.kt)
*   **Screen:** `WardrobeFootprintScreen` (formerly `StyleIntelligenceScreen`)
*   **Purpose:** Evaluates the user's physical inventory as a luxury asset portfolio.
*   **Key Metrics:**
    *   **Portfolio Performance:** Total financial investment and overall Average Cost Per Wear (CPW).
    *   **Chromatic Core:** A single-band, horizontally scrolling, interactive color spectrum of the user's inventory. Filterable by category (e.g., Wardrobe vs. Vanity) or specific item types (Tops, Bottoms).
    *   **Portfolio Composition:** A detailed, quantitative breakdown of strategic wardrobe diversity (e.g., "Eclectic: 27% Outerwear, 16% Activewear").

### ACT II: THE BEHAVIOR (How You Wear It) [file](applications/kocolor/features/inventory/src/main/java/com/zoewave/probase/kocolor/features/inventory/ui/analytics/WardrobeBehaviorScreen.kt)
*   **Screen:** `WardrobeBehaviorScreen` (formerly `WardrobeAnalyticsScreen`)
*   **Purpose:** A behavioral psychology dashboard measuring actual utility, habits, and wardrobe ROI.
*   **Key Metrics:**
    *   **Wardrobe DNA:** The aesthetic identity of the closet (e.g., *Neutral-led, Warm-biased*).
    *   **Rotation Health:** A 0-100 score analyzing the balance of garment usage, highlighting "Most Worn" and "Rarely Worn" items.
    *   **Versatility & Utility:** Deterministically calculates all compatible outfit combinations (e.g., *Universal Khaki Button-Down: 18 possible looks*).
    *   **Wardrobe Opportunities:** Actionable coverage gaps (e.g., *+ A saturated cool accent would expand your color coverage*).

### ACT III: THE SYNTHESIS (The KoColor Recommendation Engine) 
[file](applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/result/StyleCreationStoryScreen.kt)
*   **Screen:** `StyleCreationStoryScreen`
*   **Purpose:** Explains *how* and *why* an outfit was created for a specific day or event.
*   **Key Metrics:**
    *   **The Vision & Atmosphere:** Maps the user's requested intent to the current weather and UV conditions.
    *   **The Anchor:** The foundational item the outfit was built around and *why* it was selected.
    *   **Intent Fulfillment:** A precise 0-100 score on how perfectly the outfit satisfied the user's original request.
    *   **FASHIONISTA Score:** A deterministic aesthetic evaluation (Color Harmony, Silhouette, Contrast & Depth) of the final ensemble.

---

## 3. The Observation Window & Data Quality Layer

Analytics are meaningless without knowing the depth of the data backing them. KoColor employs a dedicated **`AnalyticsCoverage`** layer.

At the top of the Wardrobe Behavior screen, the system renders a **Data Coverage & Quality** section:
*   *Observation Period:* E.g., June 2026 – September 2026
*   *Total Records Analyzed:* E.g., 217 wear records
*   *Coverage Metrics:* Identifies if 100% of the wardrobe has color data, but only 65% has financial/price data attached. 

This guarantees absolute trust. The user knows the engine isn't hallucinating; it's grading its own homework.

---

## 4. The Recommendation Pipeline Data Flow

The Footprint and Behavior acts are not just visual reports—they are the **direct inputs** into the Act III recommendation engine.

When the user asks for a new outfit, the `StyleSimulatorViewModel` retrieves the user's `StyleContextSummary`. 
Recent updates securely pipe the latest deterministic analytics directly into this context:
1.  **Wardrobe DNA** (e.g., knowing the user is *Neutral-led*)
2.  **Rotation Health** (penalizing over-worn items, promoting zero-wear items)
3.  **Versatility** (leveraging high-compatibility items)

These metrics are fed into the prompt builder, allowing the AI to say: *"To address your neglected cool-toned items, I've anchored this outfit with your Cobalt Blue jacket..."*

---

## 5. Signature Custom Visualizations

To avoid looking like a generic corporate spreadsheet, KoColor abandons standard bar charts in favor of custom, native Compose `Canvas` visualizations.

### The Color History Thread (Visual Diary)
*   **What it is:** A pointillist timeline of colors worn over time.
*   **How it works:** Maps the `timestamp` of wear events across the X-axis (Oldest -> Today). Overlaps exact `colorHex` circles with `0.7f` alpha so multiple outfits in the same week blend into an organic, continuous color gradient revealing the user's fashion mood shifts.

### Interactive Wear Distribution Scatter Plot
*   **What it is:** An interactive plot showing the drop-off curve of wardrobe utility.
*   **How it works:** Renders garments sequentially on the X-axis (Least Worn -> Most Worn) and their frequency on the Y-axis. 
*   **Interactivity:** Users can tap any dot to reveal a floating, animated tooltip card showing the garment's thumbnail, name, and total wear count, with a one-tap deep link to the item's detail page.

### The Dynamic Chromatic Core
*   **What it is:** A horizontally scrolling, single-band spectrum of the user's entire inventory.
*   **How it works:** Tapping a color segment smoothly expands it using a spring animation, immediately listing every garment or cosmetic product that matches that specific shade.

---

## Appendix Inventory UI File Catalog & Breakdown

com/zoewave/probase/kocolor/features/inventory/ui/
│
├── landing/
│   ├── WardrobeLandingScreen.kt
│   └── CuratedClosetDashboard.kt
│
├── analytics/
│   ├── WardrobeFootprintScreen.kt
│   ├── WardrobeBehaviorScreen.kt
│   ├── StyleIntelligenceScreen.kt
│   ├── WardrobeAnalyticsScreen.kt
│   ├── StrategicDiversityScreen.kt
│   ├── UsageDistributionScreen.kt
│   └── UsageMetricsScreen.kt
│
├── management/
│   ├── WardrobeScreen.kt
│   ├── WardrobeDetailScreen.kt
│   ├── WardrobeEditScreen.kt
│   ├── WardrobeCategoryCoverScreen.kt
│   └── ColorVerificationScreen.kt
│
├── components/
│   └── [UI Component files...]
│
├── util/
│   └── [Utility files...]
│
└── WardrobeViewModel.kt

The `com.zoewave.probase.kocolor.features.inventory.ui` package is structured into distinct, feature-driven directories. Below is the detailed breakdown of the contents and responsibilities of every file.

### Root Package (`ui/`)
*   **`WardrobeViewModel.kt`**: Central ViewModel driving the inventory feature. Observes Room database flows (`WardrobeRepository`), maintains `WardrobeUiState` (total items, total investment, category metadata, glow scores, diversity index), and handles user events (sorting, filtering, category selection).

### Landing Directory (`ui/landing/`)
*   **`WardrobeLandingScreen.kt`**: The primary entry container for the closet tab. Hosts category cover carousels, action bars, taxonomy dialogs, and embeds the `CuratedClosetDashboard`.
*   **`CuratedClosetDashboard.kt`**: Executive dashboard UI component displaying top summary stat cards (`THE FOOTPRINT` and `THE BEHAVIOR`) and bottom action cards (`VIEW INTELLIGENCE` and `VIEW INVENTORY`).

### Analytics Directory (`ui/analytics/`)
*   **`WardrobeFootprintScreen.kt`**: Act I (The Footprint) master screen. Renders Wardrobe DNA, Profile Analysis / Chromatic Core single-band color spectrum (with category filters), Portfolio Performance ($ Value), Portfolio Composition progress bars, and Wardrobe Opportunities.
*   **`WardrobeBehaviorScreen.kt`**: Act II (The Behavior) master screen. Renders Data Coverage & Quality metrics, Engagement Snapshot (Rotation / Rarely / Never Worn), Wear Distribution Canvas scatter plot, Color History Canvas timeline thread, Rotation Health scores, Style Efficiency (CPW) collapsible list, and Versatility & Utility metrics.
*   **`StyleIntelligenceScreen.kt`**: Financial ROI and style intelligence screen rendering Total Investment Value, Average Cost Per Wear (CPW), and item-level CPW ranking cards with direct detail links.
*   **`WardrobeAnalyticsScreen.kt`**: Core behavioral analytics screen container housing interactive Canvas plots and rotation health sections.
*   **`StrategicDiversityScreen.kt`**: Standalone portfolio diversity screen showing portfolio composition progress bars per category and strategic diversity insights (`ProInsightCard`).
*   **`UsageDistributionScreen.kt`**: Standalone wear distribution screen featuring `UsageDistributionChart` and ranked `WearRankingRow` items.
*   **`UsageMetricsScreen.kt`**: Standalone rotation frequency screen showing wear distribution histograms and top-worn piece rankings.

### Management Directory (`ui/management/`)
*   **`WardrobeScreen.kt`**: Main digital closet item catalog list/grid view with category filtering, search bar, and item addition CTA.
*   **`WardrobeDetailScreen.kt`**: Detailed view for a single garment displaying thumbnail image, brand, category, formality, price, CPW, wear count, and notes.
*   **`WardrobeEditScreen.kt`**: Edit form for updating garment metadata (name, price, category, formality, color hex, image).
*   **`WardrobeCategoryCoverScreen.kt`**: Focused category cover view showing category-specific stats (top worn, best value, premium piece) and filtered item grid.
*   **`ColorVerificationScreen.kt`**: Interactive screen for verifying and adjusting detected color hex codes, undertone temperatures, and palettes for a garment.

### Shared Components Directory (`ui/components/`)
*   **`AnalyticsStatCard.kt`**: Reusable dashboard stat card rendering label, formatted value, and icon.
*   **`CategoryStatCard.kt`**: Small card component displaying category name, item count, and icon.
*   **`ClothingProductGridCard.kt`**: Grid card component for displaying clothing items with thumbnail images, names, and prices.
*   **`ColorVerificationItem.kt`**: Interactive row component for color code editing and verification.
*   **`DetailRow.kt`**: Key-value text row component for item detail views.
*   **`MetricItem.kt`**: Compact metric row component for stats display.
*   **`ProInsightCard.kt`**: Styled purple callout card displaying AI/analytics insights and diversity notes.
*   **`RankingStatCard.kt`**: Numbered ranking row component for top/least worn garments.
*   **`SectionHeader.kt`**: Standardized section header component with title and action button.
*   **`UsageDistributionChart.kt`**: Vertical bar chart component categorizing items by wear count buckets (Never, 1-5, 6-10, 11-20, 20+).
*   **`WardrobeCard.kt`**: Primary card component representing a garment in lists with image, name, brand, color badge, and CPW.
*   **`WardrobeComponents.kt`**: Collection of shared UI components including taxonomy dialogs, category chips, and filter bars.
*   **`WardrobeEfficiencyRow.kt`**: Row component displaying item name and calculated Cost-Per-Wear value.
*   **`WearRankingRow.kt`**: Row component rendering rank number, item name, wear count, and color swatch circle.

### Utilities Directory (`ui/util/`)
*   **`FreshnessLogic.kt`**: Utility calculating item freshness, recency decay, and PAO/rotation indicators.
