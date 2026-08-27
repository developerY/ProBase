# Implementation Plan: Anchor-Driven Color & Deterministic-First Styling Engine

This document defines the architectural standard for KoColor's styling pipeline. It establishes a hard boundary between mathematical data processing and generative aesthetic synthesis.

> [!IMPORTANT]
> **Core Principle**: The AI is not the wardrobe search engine. KoColor's local mathematical engine is the wardrobe search engine.
>
> **Information Elimination**: Token optimization is achieved by reducing irrelevant information *before* it becomes tokens. The local system determines which garments are physically, contextually, and chromatically viable. AI receives only that constrained candidate set and performs the higher-order aesthetic reasoning that deterministic algorithms cannot reliably provide.

## 1. Deterministic-First Architecture

All objectively computable constraints, mathematical color analysis, candidate retrieval, and filtering are performed locally on the device. AI is strictly reserved for higher-order aesthetic synthesis, visual/multimodal reasoning, and stylistic rationale.

**The Golden Rules:**
1.  **Context Constriction**: AI never receives the user's entire wardrobe. AI receives only the locally retrieved, mathematically compatible reasoning set.
2.  **Dynamic Top-K**: The candidate pool size is governed by the active AI provider's policy (maximum 16), ensuring a high reasoning-per-token ratio.
3.  **The Privacy Invariant**: Raw images may be consumed by on-device multimodal AI (e.g., Gemini Nano) to analyze drape and texture, but are never included in cloud-tier requests. Cloud requests receive only semantic manifests and biometric telemetry.

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
- **Harmony Checks**: Compute geometric relationships on the color wheel:
    - `isComplementary`, `isAnalogous`, `isMonochromatic`.

---

## 3. Phase 2: Anchor Selection & Context Engine

### [NEW] `DeterministicContextEngine.kt`
- **Location**: `applications:kocolor:data:usecase`
- **Responsibility**: Orchestrates the retrieval pipeline to prune 300+ items down to a "Reasoning Set" (Top-K) items.

#### Hard Constraints (Elimination)
Before scoring, the engine immediately prunes the inventory based on binary criteria:
-   **Availability**: Exclude items where `isHidden: true` or status is unavailable.
-   **Weather Gating**: Filter by thermal weight vs. ambient temperature.
-   **Rotation Lockout**: Exclude items worn in the last 3 days.
-   **Occasion Mismatch**: Strict category eligibility for the selected intent.

#### Anchor Selection Policy
The engine establishes an "Anchor" garment (typically a top or bottom) that must first pass Hard Constraints, using this hierarchy:
1.  **User-Locked Item**: Strictly honored (bypasses constraints if forced).
2.  **User-Selected Item**: Recently tapped or focused.
3.  **Context-Eligible Garment**: Highest weather/occasion fit score.
4.  **Color-Profile Compatibility**: Aligns perfectly with user seasonal profile.
5.  **Freshness / Rotation Adherence**.
6.  **Stable ID Tie-breaker**.

#### Continuous Compatibility Scoring (Ranking)
The remaining eligible items are ranked relative to the Anchor using a weighted composite score:
- `Context Fit` + `Hue Harmony` + `ΔE Distance` + `Contrast Balance` + `Appearance Fit` + `Freshness Score`.

#### Role-Aware Candidate Diversity
Ensures the Reasoning Set contains useful combinations rather than just high-scoring colors. A 12-item set balances into:
-   3–4 Tops
-   3–4 Bottoms
-   2–3 Footwear
-   1–2 Outerwear/Accessories

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
- **Target**: Measure actual reduction through `countTokens()`.
- Use the `CompactManifestSerializer` to convert the Top-K items into dense, low-token tuples.

---

## 6. Verification Plan

### 1. Mathematical Accuracy
- Unit test `ColorHarmonyEngine` with known harmonic pairs and clashing thresholds.

### 2. Retrieval Invariants
- **Diversity Check**: Verify Reasoning Sets contain multiple garment roles (Tops, Bottoms, Shoes).
- **Anchor Stability**: Ensure the Anchor is always included at index 0 of the manifest.

### 3. Fault Tolerance
- Verify `HeuristicFallbackEngine` produces a coherent result using the same colorimetry logic when AI fails.
