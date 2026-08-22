# KoColor Rotation & Inventory: UI/UX Specification (V1)

This document provides the technical and functional specification for the four primary screens of the KoColor rotation and analytics experience.

---

## 1. Curated Closet (Dashboard)
**Filename**: `CuratedClosetDashboard.kt`

### Description
The central entry point and "Portfolio View" of the user's wardrobe. It frames clothing as a curated collection and financial investment.

### Functional Requirements
- **Cold Start Awareness**: Detects if `totalOutfitsCommitted < 5`. If true, Glow Score displays "∞".
- **Holographic vs. Grounded UX**: Differentiates between AI-driven intelligence (iridescent gradients) and physical inventory management (forest green).
- **Premium Typography**: piece counts and currency values must use `FontFamily.Serif` for a high-end, editorial feel.

### UI Components
- **Glow Score Card**: 0–100% utilization. Displays "∞" in cold start.
- **Diversity Index Card**: Textual label (e.g., "Strategic").
- **Intelligence Vertical Card**: Large card with piece count and "VIEW INTELLIGENCE" holographic footer.
- **Inventory Vertical Card**: Large card with total USD value and "VIEW INVENTORY →" forest green footer.

---

## 2. Strategic Diversity
**Filename**: `StrategicDiversityScreen.kt`

### Description
Quantitative analysis of the wardrobe's architecture. Answers: *“Where is my investment concentrated?”*

### Functional Requirements
- **Vertical Analysis**: Displays the percentage share of pieces and investment per category.
- **AI Synthesis**: Includes a `ProInsightCard` providing contextual summary advice.

### UI Components
- **Composition List**: Category names with piece counts and `LinearProgressIndicator` gauges.
- **Pro Insight Card**: Stylized text block providing AI-driven summary of the wardrobe balance.

---

## 3. Usage Metrics
**Filename**: `UsageDistributionScreen.kt`

### Description
The behavioral analytics view. Exposes rotation frequency and identifies "Wardrobe Heroes."

### Functional Requirements
- **Frequency Grouping**: Buckets garments into: Never, 1–5, 6–10, 11–20, 20+ wears.
- **Resting Indicators**: Displays "RESTING NOW: X pieces" for items in the 48-hour cooldown window.

### UI Components
- **Usage Distribution Chart**: Custom visualization of counts per wear-frequency bucket.
- **Wardrobe Heroes**: Top 3 garments by `useCount` with relative usage bars.
- **ROI Framing**: Links 0-wear items to "Unused Investment" value.

---

## 4. Style Intelligence (Wardrobe Analytics)
**Filename**: `WardrobeAnalyticsScreen.kt` / `StyleIntelligenceScreen.kt`

### Description
The "Style DNA" view, focusing on value efficiency and the "Chromatic Core."

### Functional Requirements
- **Cost Per Wear (CPW)**: Calculated as `price / useCount`. 
- **Null Safety**: If `useCount == 0`, CPW returns null. UI displays **"NOT DEPLOYED"**.
- **Chromatic Extraction**: Visualizes the dominant color signature of the collection.

### UI Components
- **Performance Row**: Total Value and Avg CPW metrics.
- **Chromatic Core**: A weighted horizontal spectrum bar of wardrobe colors. Neutrals are moved to the far right for visual clarity.
- **Style Efficiency List**: Garments sorted by CPW (Utility), emphasizing the most efficient investments.
