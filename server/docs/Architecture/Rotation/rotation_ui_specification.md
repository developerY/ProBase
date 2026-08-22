# KoColor Rotation & Inventory: UI/UX Specification

This document provides a detailed technical and functional specification for the four primary screens that comprise the KoColor wardrobe rotation and analytics experience.

---

## 1. Curated Closet (Dashboard)
**Filename**: `WardrobeLandingScreen.kt` / `CuratedClosetDashboard.kt`

### Description
The central entry point and "Portfolio View" of the user's wardrobe. It frames clothing as a curated collection and financial investment rather than just a list of items.

### Functional Requirements
- **Cold Start Awareness**: Detects if the user has < 5 outfits logged and transitions metrics into an "Initializing" or "Calculating" state.
- **Metric Navigation**: Serves as a routing hub where clicking metrics leads to deeper analytical views.
- **Dynamic Indicators**: Surfaces "Freshness" logic where items are visually flagged as RESTING, FRESH, or IN ROTATION.

### UI Components (What is Displayed)
- **Top Bar**: "Style Archive" title with global search and archive/storage action icons.
- **Glow Score Card**: 0–100% utilization gauge. Displays "∞" if in cold start.
- **Diversity Index Card**: A qualitative label (e.g., "Strategic", "Eclectic") derived from category distribution entropy.
- **View Intelligence Card**: Large vertical card displaying total piece count with a holographic iridescent action footer.
- **View Inventory Card**: Large vertical card displaying Total Value (USD) with a deep forest green action footer.
- **Verticals Section**: A "body-zone" mapped list of categories (e.g., Tops, Bottoms) with quick-access navigation.
- **Floating Action Button**: "Add Clothing" trigger for the capture flow.

---

## 2. Strategic Diversity
**Filename**: `StrategicDiversityScreen.kt`

### Description
A quantitative deep-dive into the "Architecture" of the wardrobe. It answers the question: *“Where is my investment concentrated?”*

### Functional Requirements
- **Concentration Analysis**: Calculates the percentage share of each category relative to the total portfolio.
- **AI Synthesis**: Provides a `ProInsightCard` that interprets the distribution (e.g., "Balanced distribution across 8 verticals").

### UI Components (What is Displayed)
- **Portfolio Composition List**: 
    - **Category Name**: (e.g., Outerwear, Shoes).
    - **Piece Count**: Number of garments in that vertical.
    - **Percentage Gauge**: A `LinearProgressIndicator` showing the vertical's share of the total wardrobe.
- **Pro Insight Card**: A translucent, stylized text block at the bottom providing AI-driven summary advice.
- **Stat Summary Row**: (Redundant high-level stats for context while scrolling).

---

## 3. Usage Metrics
**Filename**: `UsageDistributionScreen.kt`

### Description
The behavioral analytics view. It exposes the actual rotation frequency of garments, identifying "Wardrobe Heroes" and underutilized assets.

### Functional Requirements
- **Frequency Grouping**: Aggregates usage data into buckets: Never, 1–5, 6–10, 11–20, 20+.
- **Hero Ranking**: Sorts garments by `useCount` to show the most frequently worn items.
- **ROI Framing**: Links underutilization to "Unused Investment" (dollar value of items with 0 wears).

### UI Components (What is Displayed)
- **Usage Distribution Chart**: A custom bar or distribution visualization showing garment counts per frequency bucket.
- **Most Worn Section**: A ranked list (1-5) of garments with wear counts and a visual bar representing usage relative to the top item.
- **Glow Score Description**: Educational text block explaining how rotation improves "Wardrobe ROI."

---

## 4. Style Intelligence (Wardrobe Analytics)
**Filename**: `WardrobeAnalyticsScreen.kt`

### Description
The most advanced "Style DNA" view, focusing on financial performance and the user's "Chromatic Core."

### Functional Requirements
- **Financial Performance**: Calculates Average Cost Per Wear (CPW) by dividing total garment price by total wear events.
- **Chromatic Extraction**: Groups the collection by dominant `colorHex` to visualize the user's color signature.
- **Efficiency Sorting**: Identifies "Best Investments" (lowest CPW) vs. "Worst Investments" (highest CPW).

### UI Components (What is Displayed)
- **Performance Row**: Cards for Total Value and Avg CPW.
- **Wardrobe Palette (Chromatic Core)**: 
    - **Spectral Bar**: A continuous horizontal bar of colors extracted from the wardrobe, weighted by item count.
    - **Neutral Separation**: Neutrals (Black/White/Grey) are grouped at the end of the spectrum for visual clarity.
    - **Interaction**: Tapping a color segment reveals the specific garments contributing to that hue.
- **Style Efficiency List**: A list of garments sorted by their CPW metric, emphasizing the "Utility" of each item.
- **Taxonomy Info Action**: Information icon leading to a "Wardrobe Architecture" educational dialog.
