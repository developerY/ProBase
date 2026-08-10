# Manifest: The KoColor Hub Evolution

This document serves as the master record for the **Color Hub** architecture and product vision within KoColor.

## 🌟 The Vision
KoColor is evolving from a wardrobe tracking tool into a **Holistic Beauty & Styling Ecosystem**. The Color Hub is the analytical brain of this ecosystem, unifying the user's biological state (Skin Tone/Season) with their physical environment (Wardrobe/Vanity).

---

## 🏗️ Modular Architecture

We follow a strict **Modular Layered Architecture** to ensure cross-platform scalability (Mobile, Wear OS, XR).

### 1. The Intelligence Engine (`:features:colors`)
*   **Role**: Platform-agnostic domain logic.
*   **Pillars**:
    *   **Color Science**: Technical conversions (CIELAB, HSV, RGB) and Euclidean distance matching.
    *   **Stylist Engine**: Heuristic analysis of inventory "vibes" and seasonal gap detection.
    *   **Wellness Advisor**: Biological cross-pollination connecting color profiles to K-Beauty rituals.
*   **Models**: `ColorInfo`, `WellnessInsight`, `StylistEdit`.

### 2. The Platform Face (`:apps:mobile:features:color`)
*   **Role**: Android-specific UI and UX implementation.
*   **Key Components**:
    *   **Chromatic DNA Bar**: An interactive, harmonica-style visualization with perceptual hue sorting.
    *   **Editorial UI**: A serif-driven, luxury-style dashboard presenting dynamic insights.
    *   **Deep Integration**: Bridging analyzed gaps to the `features:routines` and `features:store` modules.

---

## 🛠️ Technical Pillars

### 🌈 Chromatic DNA & Perceptual Sorting
We utilize a **perceptual hue rotation** algorithm (shifting hues > 330°) to ensure that pinks and reds are grouped adjacent to each other, mimicking a professional retail planogram rather than a simple mathematical linear scale.

### 🪗 Harmonica Expansion
An interactive `LazyRow` that uses physics-based animations (`Spring`) to expand color segments and shrink neighbors, allowing users to drill down into high-density spectral data without leaving the context of the bar.

### 🤖 Stylist's Edit Engine
A narrative generator that analyzes the distribution of hues and saturation levels across the user's entire inventory to determine their "Vibe" (e.g., *The Minimalist* vs. *Cool Sophisticate*) and provides actionable, biological-anchored advice.

---

## 🗺️ The Roadmap (Coming Soon)

### 🛍️ AI-Curated Commerce
We are implementing an **AI Curator** that will:
- Generate personalized shortlists of products from the boutique matching your "Missing Colors."
- Verify product matches using the same engine that analyzes your wardrobe.

### 💄 Instant AR Lab
Direct deep-linking from the Color Hub into:
- **NailLab**: See recommended polish shades on your own hands.
- **FaceLab**: Virtual try-on for seasonal cosmetic recommendations (lip stains, illuminators).

---
**Status**: Active Development
**Lead Module**: `:applications:kocolor:features:colors`
