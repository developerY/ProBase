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

### ACT I: THE FOOTPRINT (What You Own) [file](applications/kocolor/features/inventory/src/main/java/com/zoewave/probase/kocolor/features/inventory/ui/StyleIntelligenceScreen.kt)
*   **Screen:** `StyleIntelligenceScreen`
*   **Purpose:** Evaluates the user's physical inventory as a luxury asset portfolio.
*   **Key Metrics:**
    *   **Portfolio Performance:** Total financial investment and overall Average Cost Per Wear (CPW).
    *   **Chromatic Core:** A single-band, horizontally scrolling, interactive color spectrum of the user's inventory. Filterable by category (e.g., Wardrobe vs. Vanity) or specific item types (Tops, Bottoms).
    *   **Portfolio Composition:** A detailed, quantitative breakdown of strategic wardrobe diversity (e.g., "Eclectic: 27% Outerwear, 16% Activewear").

### ACT II: THE BEHAVIOR (How You Wear It) [file](applications/kocolor/features/inventory/src/main/java/com/zoewave/probase/kocolor/features/inventory/ui/WardrobeAnalyticsScreen.kt)
*   **Screen:** `WardrobeAnalyticsScreen`
*   **Purpose:** A behavioral psychology dashboard measuring actual utility, habits, and wardrobe ROI.
*   **Key Metrics:**
    *   **Wardrobe DNA:** The aesthetic identity of the closet (e.g., *Neutral-led, Warm-biased*).
    *   **Rotation Health:** A 0-100 score analyzing the balance of garment usage, highlighting "Most Worn" and "Rarely Worn" items.
    *   **Versatility & Utility:** Deterministically calculates all compatible outfit combinations (e.g., *Universal Khaki Button-Down: 18 possible looks*).
    *   **Wardrobe Opportunities:** Actionable coverage gaps (e.g., *+ A saturated cool accent would expand your color coverage*).

### ACT III: THE SYNTHESIS (The KoColor Recommendation Engine) 
[file](applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/result/StyleCreationStoryScreen.kt)
[file](applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/result/FashionJourneyScreen.kt)
*   **Screens:** `FashionJourneyScreen` & `StyleCreationStoryScreen`
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
