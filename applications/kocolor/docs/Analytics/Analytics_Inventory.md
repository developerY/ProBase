# Comprehensive Inventory of KoColor Analytics

This document serves as the complete, centralized inventory of all data analytics, telemetry, and intelligence metrics actively computed and rendered within the KoColor ecosystem. 

KoColor separates its analytics into three distinct pillars: **Wardrobe Behavior (How you wear)**, **Portfolio Intelligence (What you own)**, and **Aesthetic Calibration (How it looks)**. All metrics listed here are computed deterministically (without LLM hallucinations) before being passed to the presentation layer.

---

## 1. Wardrobe Analytics (Behavior & Utility)
*Located in: `WardrobeAnalyticsScreen.kt` & `WardrobeAnalyticsEngine.kt`*

This engine processes historical wear data to generate behavioral insights.

### 1.1 Wardrobe Snapshot
*   **Total Pieces:** Absolute count of all items in the inventory.
*   **Active in Rotation:** Count of items worn frequently (≥5 wears).
*   **Rarely Worn:** Count of items barely worn (1-4 wears).
*   **Never Worn:** Count of items with 0 logged wears.

### 1.2 Wardrobe DNA
*   **Primary Identity:** Overall palette classification (e.g., "Neutral-led", "Color-led").
*   **Temperature Bias:** Aggregate temperature (e.g., "Warm-biased", "Cool-biased").
*   **Depth:** Average color value/lightness (e.g., "Medium depth", "Light depth").
*   **Contrast:** Internal variance of the wardrobe (e.g., "Balanced contrast").
*   **Chroma:** Overall saturation levels (e.g., "Low-to-medium chroma").

### 1.3 The Color Story (Distribution)
*   **Percentage Breakdowns:** % Neutrals, % Warm Tones, % Cool Accents.
*   **Top Colors in Rotation:** Ranked list of specific color families (e.g., Khaki, Black, Crimson) accompanied by exact item counts and percentage shares.

### 1.4 The Collection
*   **Category Breakdown:** Absolute item counts for Tops, Bottoms, Dresses, Shoes, Outerwear, and Activewear.

### 1.5 Temporal & Behavioral Visualizations
*   **Wear Distribution (Scatter Plot):** A fully interactive 2D plot mapping every item in the wardrobe. Y-Axis = Wear Count; X-Axis = Sequential Rank (Least to Most Worn). Highlights the drop-off curve of wardrobe utility.
*   **Color History (Timeline Thread):** A chronological timeline plotting individual wear events. Renders actual garment hex colors as translucent, overlapping dots to show color moods over time.

### 1.6 Rotation Health
*   **Rotation Score:** A 0-100 metric evaluating how evenly the wardrobe is utilized.
*   **Most Worn Garments:** Ranked list of the highest-frequency items.
*   **Least Worn Garments:** Ranked list of the lowest-frequency items (highlighting neglected pieces).

### 1.7 Versatility & Utility
*   **Total Possible Combinations:** Deterministic mathematical combination of all compatible Tops × Bottoms × Shoes × Outerwear.
*   **Most Versatile Piece:** Identifies the single garment that unlocks the highest number of valid outfit combinations.

### 1.8 Wardrobe Opportunities
*   **Coverage Gaps:** Identifies missing foundational elements based on existing coverage metrics (e.g., Missing warm neutrals, missing bright accents) and outputs actionable insights.

---

## 2. Style Intelligence (Financials & Chromatic Core)
*Located in: `StyleIntelligenceScreen.kt`*

This engine focuses on the financial efficiency and color composition of the physical inventory.

### 2.1 Portfolio Performance
*   **Total Value:** Aggregate monetary value of all logged inventory items.
*   **Average Cost Per Wear (CPW):** Aggregate efficiency metric calculated by dividing the total investment by the total number of wears across all deployed items.

### 2.2 Chromatic Core (Spectrum Analysis)
*   **Single-Band Color Spectrum:** A unified, horizontally scrolling visualization of every distinct color hex in the user's inventory.
*   **Category Filtering:** Ability to filter the color spectrum by "All Items", "Tops", "Bottoms", "Shoes", etc.
*   **Density Mapping:** Color band width dynamically scales based on the exact number of garments owned in that specific shade.

### 2.3 Portfolio Composition (Strategic Diversity / Footprint)
*   **Diversity Index:** A qualitative classification of wardrobe concentration (e.g., "Eclectic", "Strategic", "Focused").
*   **Investment Distribution:** Percentage and dollar-value breakdown of investment *per category* (e.g., Outerwear: 9 Items | $1,700 | 27%).

### 2.4 Style Efficiency (Item-Level CPW)
*   **Ranked Efficiency:** A descending list of all garments ranked by their individual Cost Per Wear, highlighting the highest ROI pieces vs. "Not Deployed" pieces.

---

## 3. Aesthetic Calibration (Generation & Validation)
*Located in: `StyleSimulatorEngine.kt`, `FashionJourneyScreen.kt` & `StyleResultScreen.kt`*

These analytics are generated "Just-In-Time" when an outfit is requested, evaluating the specific generated ensemble.

### 3.1 The FASHIONISTA Score
A 0-100 deterministic scoring algorithm measuring the aesthetic success of a compiled outfit.
*   **Color Harmony (0-100):** Uses CIEDE2000 math to score the relational harmony between the selected top, bottom, and shoes.
*   **Silhouette Proportion (0-100):** Scores the structural balance of the items.
*   **Contrast & Depth (0-100):** Scores visual interest and separation.
*   **Overall Score:** The blended final output (e.g., 92.7 -> "APPROVED").

### 3.2 User Intent Fulfillment
*   **Intent Status:** Whether the user provided a specific prompt (e.g., "SPECIFIED: colorful outfit").
*   **Fulfillment Score:** 0-100 metric measuring how closely the resulting outfit matched the requested parameters.
*   **Observed Colorfulness:** Float mapping the actual saturation of the ensemble.
*   **Observed Color Contrast:** Float mapping the actual variance of the ensemble.

### 3.3 Style Architecture Audit Log
*   **Execution Tier:** Identifies the hardware/cloud routing (e.g., `AI_CLOUD` vs `DETERMINISTIC_FALLBACK`).
*   **Latency:** Millisecond response time for the generation.
*   **Step-by-Step Pruning Log:** Exact records of how many items survived weather filtering, rotation penalties, and mathematical scoring before reaching the AI.
