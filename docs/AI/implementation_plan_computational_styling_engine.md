# Implementation Plan: Anchor-Driven Color & Deterministic-First Styling Engine

This document defines the architectural standard for KoColor's styling pipeline. It establishes a hard boundary between mathematical data processing and generative aesthetic synthesis.

> [!IMPORTANT]
> **Core Principle**: The AI is not the wardrobe search engine. KoColor's local mathematical engine is the wardrobe search engine.
>
> The local system determines which garments are physically, contextually, and chromatically viable. AI receives only that constrained candidate set and performs the higher-order aesthetic reasoning that deterministic algorithms cannot reliably provide.

## 1. Deterministic-First Architecture

All objectively computable constraints, mathematical color analysis, candidate retrieval, and filtering are performed locally on the device. AI is strictly reserved for higher-order aesthetic synthesis, spatial reasoning, and stylistic rationale.

**The Golden Rules:**
1.  **Context Constriction**: AI never receives the user's entire wardrobe. AI receives only the locally retrieved, mathematically compatible reasoning set (Top 8–16 candidates).
2.  **The Privacy Invariant**: Raw images may be consumed by on-device multimodal AI (e.g., Gemini Nano) to analyze drape and texture, but are never included in cloud-tier requests. Cloud requests receive only semantic manifests and biometric telemetry.

---

## 2. Phase 1: Mathematical Color & Harmony Engine

### [NEW] `ColorHarmonyEngine.kt`
- **Location**: `applications:kocolor:data:color`
- **Responsibility**: Color space conversions, geometric harmony calculations, and perceptual distance.
- **Pipeline**:
    1.  `HEX / RGB` (Digital format) $\to$
    2.  `HSL / HSV` (Hue relationships) $\to$
    3.  `CIELAB` (Human vision space) $\to$
    4.  `ΔE00` (Perceptual distance/clash prevention) $\to$
    5.  **Harmony Score**
- **Harmony Checks**:
    - `isComplementary`: Hue distance $\approx 180^\circ$.
    - `isAnalogous`: Hue distance $\approx \pm 30^\circ$.
    - `isMonochromatic`: Hue $\approx$ same, Saturation/Lightness vary.

---

## 3. Phase 2: Anchor Selection & Context Engine

### [NEW] `DeterministicContextEngine.kt`
- **Location**: `applications:kocolor:data:usecase`
- **Responsibility**: Orchestrates the retrieval pipeline to prune 300+ items down to a "Reasoning Set" of 8–16 items.

#### Anchor Selection Policy
The engine establishes an "Anchor" garment (typically a top or bottom) using a strict, deterministic fallback sequence:
1.  **User-Locked Item**: Explicitly pinned in the UI.
2.  **User-Selected Item**: Manually chosen or focused.
3.  **Highest Context-Fit**: Best thermal match for weather and occasion.
4.  **Highest Color-Profile Compatibility**: Aligns perfectly with user seasonal profile.
5.  **Best Freshness Score**: Based on rotation/wear history (exclude if worn < 3 days).
6.  **Deterministic Tie-breaker**: Alphanumeric ID fallback.

#### Continuous Compatibility Scoring
Rather than binary elimination, every candidate garment receives a weighted composite score:
- `Context Fit` (Weather/Occasion)
- `Hue Harmony` (Analogous/Complementary/Monochromatic)
- `ΔE / Perceptual Distance` (Clash prevention)
- `Contrast Balance` (Aligns with user contrast telemetry)
- `Appearance Fit` (Complements user skin undertone)
- `Freshness Score` (Rotation adherence)

---

## 4. Phase 3: Adaptive Capability Router (AI Waterfall)

### [MODIFY] `StyleSimulatorEngine.kt`
- **Integration**: The `DeterministicContextEngine` executes *before* the AI Router.
- **Provider Priority**:
    1.  Local Multimodal AI (NPU-accelerated, private, free).
    2.  BYOK (User pays, data-only).
    3.  Firebase AI Logic (Enterprise managed cloud).
    4.  Deterministic Fallback (Heuristic rules).

---

## 5. Phase 4: Token Optimization & Privacy

### Context-Aware Serialization
- **Target**: Substantially reduce token footprint versus the existing JSON representation; measure the actual reduction through `countTokens()`.
- Use the `CompactManifestSerializer` to convert the Top-K items into dense, low-token tuples.
- **Example**: `[w55|Top|Khaki Trench|#B8A992|Warm|Deep|Cotton]`

### Type-Safe Privacy Boundary
- **Invariant**: No raw pixels leave the device for cloud tiers.
- Cloud prompts receive only the `StyleTelemetry` vector and the compact manifest.

---

## 6. Verification Plan

### 1. Mathematical Accuracy
- Unit test the `ColorHarmonyEngine` with known harmonic pairs (e.g., Red and Green should be `isComplementary: true`).
- Verify `CIEDE2000` correctly flags "clashing" muddy colors.

### 2. Retrieval Invariants
- **Candidate Invariant**: Ensure the AI never receives more than the policy maximum (e.g., 16 candidates).
- Verify the **Anchor** is always included at the top of the manifest.

### 3. Fault Tolerance
- Simulate AI model failure and verify the `HeuristicFallbackEngine` produces a coherent result based on the same mathematical color logic.
