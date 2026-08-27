# Implementation Plan: Anchor-Driven Colorimetry & Deterministic-First Styling Engine

This document details the transition of KoColor from a simple AI retrieval model to a mathematically rigorous, **Deterministic-First Computational Styling Engine**.

## 1. Core Architectural Shift: Retrieval vs. Reasoning

We are moving away from having the AI "search" the wardrobe. Instead, the local engine handles all objectively computable work, leaving only subjective aesthetic coordination to the AI.

- **85% Local Deterministic Intelligence**: Pruning, filtering, and mathematical color harmony.
- **15% Generative AI Reasoning**: Aesthetic synthesis, fabric texture coordination, and rationale generation.

---

## 2. Phase 1: Mathematical Colorimetry Utilities

### [NEW] `ColorimetryEngine.kt`
- **Location**: `applications:kocolor:data:color`
- **Responsibility**: Color space conversions and geometric harmony calculations.
- **Logic**:
    - **HSL Conversion**: Map HEX colors to HSL (Hue, Saturation, Lightness).
    - **Harmony Checks**:
        - `isComplementary`: Hue distance $\approx 180^\circ$.
        - `isAnalogous`: Hue distance $\approx \pm 30^\circ$.
        - `isMonochromatic`: Hue $\approx$ same, Saturation/Lightness vary.
    - **Perceptual Distance**: Implement a stub for `CIEDE2000` ($\Delta E_{00}$) to prevent "color clashing" (colors too close but not matching).
    - **Contrast Balancing**: Verify candidate Lightness satisfies the user's `AppearanceTelemetry` (e.g., pale skin/dark hair needs high contrast).

---

## 3. Phase 2: Anchor-Driven Deterministic Context Engine

### [NEW] `DeterministicContextEngine.kt`
- **Location**: `applications:kocolor:data:usecase`
- **Responsibility**: The "Workhorse" that prunes 300 items down to a "Reasoning Set" of 8–16 items.
- **Pipeline**:
    1.  **Hard Pruning**: Filter by weather temperature, availability (`!isHidden`), and rotation rules (e.g., exclude if worn in the last 3 days).
    2.  **Anchor Selection**:
        - If the user has manually "locked" an item in the UI, that item is the **Anchor**.
        - Otherwise, programmatically select the "cleanest/freshest" high-scored item as the starting point.
    3.  **Geometric Filtering**: Use the `ColorimetryEngine` to find items that mathematically harmonize with the **Anchor**.
    4.  **Top-K Selection**: Return a ranked list of the best 8–16 compatible items.

---

## 4. Phase 3: Adaptive Capability Router (AI Waterfall)

### [MODIFY] `StyleSimulatorEngine.kt`
- **Integration**: Move the `DeterministicContextEngine` *before* the AI Router.
- **Provider Priority**:
    1.  Local Multimodal AI (NPU-accelerated, private, free).
    2.  BYOK (User pays, data-only).
    3.  Firebase AI Logic (Enterprise managed cloud).
    4.  Deterministic Fallback (Heuristic rules).

---

## 5. Phase 4: Token Optimization & Privacy

### Context-Aware Serialization
- Use the `CompactManifestSerializer` to convert the Top-K items into dense, low-token tuples.
- **Example**: `[w55|Top|Khaki Trench|#B8A992|Warm|Deep|Cotton]`
- **Impact**: Reduces token footprint by **~80%** compared to JSON.

### Type-Safe Privacy Boundary
- Enforce that **no raw pixels** leave the device for cloud tiers.
- Cloud prompts receive only the `StyleTelemetry` vector and the compact manifest.

---

## 6. Verification Plan

### 1. Mathematical Accuracy
- Unit test the `ColorimetryEngine` with known harmonic pairs (e.g., Red and Green should be `isComplementary: true`).
- Verify `CIEDE2000` correctly flags "clashing" muddy colors.

### 2. Retrieval Density
- Assert that for a 300-item wardrobe, the AI never receives more than 16 candidates.
- Verify the **Anchor** is always included at the top of the manifest.

### 3. Fault Tolerance
- Simulate AI model failure and verify the `HeuristicFallbackEngine` produces a coherent result based on the same colorimetry logic.
