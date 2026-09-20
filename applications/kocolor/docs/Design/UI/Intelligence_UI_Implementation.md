# Implementation Plan: Intelligence UI Phase (The Digital Counter)

This document outlines the technical execution path for surfacing the **Compute-at-Compile-Time (CCT)** intelligence in the KoColor mobile application. Now that the Sync Engine is successfully persisting enriched data into Room, we are "awakening" that data in the presentation layer.

---

## 🏗️ 1. Zero-Latency Visuals (BlurHash)

We will eliminate the "gray box" state during image loading by using the pre-calculated BlurHash strings.

### Tasks:
*   **Coil Integration**: Update the `AsyncImage` loaders in the Boutique and Detail screens.
*   **Instant Placeholders**: Pass the decoded BlurHash bitmap into the `placeholder` parameter of the Coil `ImageRequest`.
*   **Result**: The user sees a color-accurate blurred version of the product the millisecond the screen opens.

---

## 🎨 2. The Chromatic DNA Bar (CIELAB Visualization)

We will replace standard color swatches with a mathematically accurate 2D map of the product's color profile.

### Tasks:
*   **CIELAB Mapping**: Map the `cielab[1]` ($a^*$) and `cielab[2]` ($b^*$) coordinates onto a 2D coordinate system.
*   **The DNA Component**: Build a custom Compose `@Composable` that renders a crosshair on a professional color-undertone spectrum (Warm/Cool vs. Red/Green).
*   **Luminosity Axis**: Visualize the `cielab[0]` ($L^*$) value as a vertical "Depth" bar.

---

## ⚗️ 3. The Interfacial Chemistry Engine (Pilling Warnings)

We will use the pre-calculated thermodynamic phases to actively protect users from incompatible product layering (e.g., applying water-based serums over silicone barriers).

### Tasks:
*   **Rules Engine**: Implement the `ChemistryCompatibility` matrix in Kotlin to evaluate layering logic (Like-dissolves-Like vs. Repulsion).
*   **Routine Builder Integration**: Wire the rules engine to the Routine Builder ViewModel.
*   **The UI Badge**: Render a non-intrusive `CompatibilityBadge` that alerts users to high pilling risks with actionable resolution steps.

---

## 🧪 4. Atelier Intelligence Badges

We will surface the complex ingredient analysis as simple, luxury-grade status badges.

### Tasks:
*   **Safety Badging**: Render "Silicone-Free," "Fragrance-Free," and "Paraben-Free" badges based on the pre-calculated safety flags.
*   **Active Spotlight**: Extract and display the `heroActives` array (e.g., Retinol, Niacinamide) as high-priority "Actives" chips.
*   **Pricing Intelligence**: Use the `calculatedUnitPrice` to show "Value per 10ml" metrics on the detail screen.

---

## 💾 5. Personalization: "Make it Mine"

Wiring the UI to the sovereign cloning engine.

### Tasks:
*   **The Action**: Connect the **"MAKE IT MINE"** button on the `CosmeticDetailScreen` to the `cloneToPersonalArchive` DAO function.
*   **Visual Confirmation**: Provide immediate feedback that the item has been detached from the collection and added to the user's permanent archive.

---

## 🔍 6. Value & Search Filtering

Utilizing the compiler-provided index for zero-latency Boutique operations.

### Tasks:
*   **Tokenized Search**: Update the Boutique's search logic to query the `search_tokens` array in Room.
*   **Value Analysis**: Implement sorting by the normalized `calculated_unit_price` field.

- [x] **Step 1: BlurHash Wiring**
    - Integrated `BlurHashDecoder` and `rememberBlurHashPainter` into `PackPreviewItemRow` and `CosmeticDetailScreen`.
- [x] **Step 2: Chromatic DNA Component**
    - Built the `ChromaticDnaBar` in `:core:ui`.
    - Integrated it into the "Color Hue Map" section of the detail screen.
- [x] **Step 3: Interfacial Chemistry Engine**
    - Implemented the `ChemistryCompatibility` object.
    - Wire the `CompatibilityBadge` into the Routine Builder UI.
- [x] **Step 4: Intelligence Badging**
    - Updated `CosmeticDetailScreen` to render the Actives and Safety chips.
- [x] **Step 5: Sovereign Ingestion Wiring**
    - Hooked up the "Make it Mine" button to the transactional cloning logic and implemented luxury state animations.
- [x] **Step 6: Value & Search Filtering**
    - Wired the Boutique filters to utilize the `search_tokens` index for instant results and added value-based sorting.

---
**Status**: ✅ **INTELLIGENCE AWAKENED**
**Data Source**: KCPS v1 (Enriched)
**Target**: Zero-Latency Intelligent UI
