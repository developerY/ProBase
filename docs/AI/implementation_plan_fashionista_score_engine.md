# Implementation Plan: FASHIONISTA Score Engine

This document details the architectural specifications, mathematical formulation, and implementation roadmap for KoColor's **FASHIONISTA Score Engine**.

---

## 1. System Architecture & Contract

FASHIONISTA is a standalone, purely deterministic measurement instrument. It translates any observed outfit (`FashionistaObservation`) into a **standardized, reference-calibrated computational fashion-aesthetic score** ($0.0\text{--}100.0$) using deterministic computational measurements of the observed visual system as a whole.

### Critical System Invariants

1. **Zero AI/LLM Dependency**: Executes 100% on-device, locally, and synchronously (thread-safe for execution on background workers). Zero network calls, zero LLMs, zero retrieval, zero prompt assembly, and zero GenAI SDK dependencies.
2. **Context-Free Evaluation**: Evaluates the outfit strictly as observed. The engine possesses zero knowledge of the user, their wardrobe, weather, occasion, or recommendation logic.
3. **Immutable Versioned Calibration Standard**: The mathematical engine is deterministic. The parameters ($w_i$, $w_{ij}$, $\lambda$, $P_{unresolved}$, $\mu$, $\tau$, $qMin$, $qMax$) are implemented via an injectable, versioned `FashionistaCalibration` object derived offline from expert-rated reference ensembles.
4. **Dynamic Evidence Normalization**: Missing data (e.g., flat-lay photo with no face) does **not** corrupt the score. It uses a `FeatureValue(value, availability)` model ($\text{value} \in [0, 1], \text{availability} \in [0, 1]$), dynamically normalizing the final equation based strictly on available evidence.
5. **Weighted Measurement Coverage**: `coverage` strictly represents data completeness (how completely the outfit was measurable), calculated as $\text{coverage} = \frac{\sum w_i a_i}{\sum w_i}$. It does not represent algorithmic confidence or corrupt the raw aesthetic `score`.
6. **Evidence, Not Law**: Measurements like vertical symmetry, Matsuda color templates, or Birkhoff complexity are *features/evidence*, not absolute definitions of good fashion. Asymmetry, high contrast, and unusual proportions are valid stylistic choices. Penalties are strictly reserved for *unresolved perceptual conflicts* ($P_{unresolved}$).

---

## 2. Package Architecture (`:applications:kocolor:fashionista`)

```text
:applications:kocolor:fashionista
├── domain
│   ├── FashionistaObservation.kt
│   ├── FeatureValue.kt
│   ├── FashionistaFeatureVector.kt
│   ├── FashionistaScore.kt
│   └── FashionistaBreakdown.kt
├── math
│   ├── Statistics.kt
│   ├── CircularStatistics.kt
│   ├── Geometry.kt
│   ├── Distance.kt
│   ├── Normalization.kt
│   └── Logistic.kt
├── extraction
│   ├── ColorFeatureExtractor.kt
│   ├── CompositionFeatureExtractor.kt
│   ├── SilhouetteFeatureExtractor.kt
│   ├── TextureFeatureExtractor.kt
│   ├── PatternFeatureExtractor.kt
│   └── HierarchyFeatureExtractor.kt
├── color
│   ├── ColorSpace.kt
│   ├── Ciede2000.kt
│   ├── CircularHueStatistics.kt
│   ├── PaletteDistribution.kt
│   ├── MatsudaTemplates.kt
│   └── ChromaticHarmonyEngine.kt
├── composition
│   ├── CategoryRelationshipEngine.kt
│   ├── ProportionEngine.kt
│   ├── LayeringEngine.kt
│   └── CompositionEngine.kt
├── silhouette
│   ├── VisualMassEngine.kt
│   ├── CenterOfGravityEngine.kt
│   ├── VisualMomentEngine.kt
│   ├── NegativeSpaceEngine.kt
│   └── SilhouetteEngine.kt
├── texture
│   ├── GlcmTextureEngine.kt
│   ├── GaborTextureEngine.kt
│   ├── PatternComplexityEngine.kt
│   └── TextureHarmonyEngine.kt
├── hierarchy
│   ├── SaliencyEngine.kt
│   ├── FocalPointEngine.kt
│   └── VisualHierarchyEngine.kt
├── integration
│   ├── FaceIntegrationEngine.kt
│   ├── CosmeticIntegrationEngine.kt
│   └── OutfitIntegrationEngine.kt
├── scoring
│   ├── FashionistaCalibration.kt
│   ├── InteractionModel.kt
│   ├── DeterministicScorer.kt
│   ├── CalibrationCurve.kt
│   └── FashionistaScorerImpl.kt
└── presentation
    ├── FashionistaExplanation.kt
    ├── FashionistaScoreMapper.kt
    └── FashionistaVisualizations.kt
```

---

## 3. Implementation Steps & Module Specifications

### Step 1: Domain API & Contracts (`domain` package)
- **`FashionistaObservation`**: Data class representing raw input (extracted garments, cosmetics, spatial mass maps, optional facial/skin biometric patches). Contains zero user or weather context.
- **`FeatureValue`**: `data class FeatureValue(val value: Double, val availability: Double)`. Encapsulates feature measurements ($\in [0.0, 1.0]$) alongside data presence ($\in [0.0, 1.0]$).
- **`FashionistaFeatureVector`**: Holds the 6 perceptual systems as `FeatureValue` objects:
  - `composition: FeatureValue`
  - `colorHarmony: FeatureValue`
  - `silhouette: FeatureValue`
  - `textureHarmony: FeatureValue`
  - `visualHierarchy: FeatureValue`
  - `presentationIntegration: FeatureValue`
- **`FashionistaScore`**: `data class FashionistaScore(val score: Double, val coverage: Double, val standardId: String, val standardVersion: Int, val breakdown: FashionistaFeatureVector)`. Note: `score` is internally $0.000\text{--}100.000$ (UI displays rounded integer); `coverage` reflects weighted data completeness ($\frac{\sum w_i a_i}{\sum w_i}$).
- **`FashionistaScorer`**: `interface FashionistaScorer { fun score(outfit: FashionistaObservation): FashionistaScore }` (Synchronous, thread-safe contract).

### Step 2: Foundational Mathematics (`math` package)
- **`Statistics.kt`**, **`CircularStatistics.kt`**, **`Geometry.kt`**, **`Distance.kt`**, **`Normalization.kt`**, **`Logistic.kt`**: Contains all pure mathematical primitives (vector addition, circular mean/variance, distance metrics, logistic sigmoid) so feature extractors and scoring engines use shared, auditable utilities rather than re-implementing math logic.

### Step 3: Extraction & Normalization (`extraction`, `color`, `composition` packages)
- **Extraction Handoff**: Feature extractors (e.g., `ColorFeatureExtractor.kt`, `SilhouetteFeatureExtractor.kt`) bridge raw `FashionistaObservation` data into normalized `FeatureValue` structures relying on `math` utilities.
- **Color Harmony (`color`)**: Converts RGB $\to$ CIELAB $\to$ $L^*C^*h^\circ$, computing CIEDE2000 ($\Delta E_{00}$) relationships, hue dispersion, chroma distribution, neutral proportion, and Matsuda template affinity. Outputs normalized `colorHarmony`.
- **Composition (`composition`)**: Evaluates category relationships, proportion ratios, layering density, and structural semantic coherence. Outputs normalized `composition`.

### Step 4: Silhouette, Texture & Hierarchy (`silhouette`, `texture`, `hierarchy` packages)
- **Silhouette (`silhouette`)**: Uses native Android `Bitmap.createScaledBitmap` downsampling ($64 \times 128$ pixel buffer) in `VisualMassEngine.kt` to calculate Visual Center of Gravity ($\bar{x} = \frac{\sum m_i x_i}{\sum m_i}$, $\bar{y} = \frac{\sum m_i y_i}{\sum m_i}$) within milliseconds. Evaluates horizontal/vertical mass distribution, negative space, and silhouette ratio without penalizing intentional asymmetry. Outputs normalized `silhouette`.
- **Texture Harmony (`texture`)**: Extracts GLCM/Gabor features (frequency, contrast, entropy). Evaluates scale compatibility and material contrast safely without literal Birkhoff division ($M = O / C$). Outputs normalized `textureHarmony`.
- **Visual Hierarchy (`hierarchy`)**: Evaluates focal point isolation (Primary $\to$ Secondary $\to$ Tertiary elements). Highly complex patterns lacking clear hierarchy trigger unresolved chaos penalties ($P_{unresolved}$). Outputs normalized `visualHierarchy`.

### Step 5: Integration (`integration` package)
- **Presentation Integration**: Calculates Individual Typology Angle ($ITA = \left[\arctan\left(\frac{L^* - 50}{b^*}\right)\right] \times \frac{180}{\pi}$) and Michelson facial contrast ($C_f = \frac{L_{skin} - L_{feature}}{L_{skin} + L_{feature}}$).
- **Dynamic Availability**: If the observation is a flat-lay or outfit-only image (no wearer/face), returns `FeatureValue(value = 0.0, availability = 0.0)`. This completely bypasses $C_f$ execution on null data without polluting the score with arbitrary $0.5$ defaults, while setting `availability = 0.0` to accurately reflect lower data completeness in `coverage`.

### Step 6: Bounded Nonlinear Deterministic Scoring Engine (`scoring` package)
- **`FashionistaCalibration.kt`**: Data class holding versioned, reference-calibrated parameters:
  `data class FashionistaCalibration(val standardId: String, val version: Int, val featureWeights: DoubleArray, val interactionWeights: DoubleArray, val lambda: Double, val unresolvedPenaltyWeight: Double, val mu: Double, val tau: Double, val qMin: Double, val qMax: Double)`
- **`InteractionModel.kt`**: Computes explicit cross-feature interaction terms $I_{ij} = x_i \cdot x_j$ using **only** features where `availability > 0.0`.
- **`DeterministicScorer.kt`**: Computes the separated nonlinear interaction model with interaction blending parameter $\lambda$ (`lambda`):
  $$Q_{base} = \frac{\sum_i w_i x_i a_i}{\sum_i w_i a_i}$$
  $$Q_{int\_num} = \sum_{i<j} w_{ij} x_i x_j a_i a_j \quad | \quad Q_{int\_den} = \sum_{i<j} w_{ij} a_i a_j$$
  - **Interaction Denominator Fail-Safe**: If $Q_{int\_den} == 0.0$, sets `effectiveLambda = 0.0` and $Q_{interaction} = Q_{base}$. Otherwise, $Q_{interaction} = \frac{Q_{int\_num}}{Q_{int\_den}}$ and `effectiveLambda = lambda`.
  - **Blended Equation**:
    $$\text{Blended} = (1 - \text{effectiveLambda}) Q_{base} + \text{effectiveLambda} \cdot Q_{interaction}$$
    $$Q = (\text{Blended} - P_{unresolved})\text{.coerceIn}(qMin, qMax)$$
  - **Zero-Availability Fail-Safe**: If total available base weight ($\sum_i w_i a_i$) equals `0.0` (e.g., an entirely empty `FashionistaObservation` is passed), the scorer immediately short-circuits to return `FashionistaScore(score = 0.0, coverage = 0.0, standardId = ..., standardVersion = ..., breakdown = ...)` to prevent division-by-zero or `NaN` crashes.
- **`CalibrationCurve.kt`**: Maps bounded $Q$ to the $0\text{--}100$ scale using calibration center ($\mu$) and scale ($\tau$):
  $$F = 100 \cdot \sigma\left( \frac{Q - \mu}{\tau} \right) = 100 \cdot \left( \frac{1}{1 + e^{-\frac{Q-\mu}{\tau}}} \right)$$
  - **Semantic Interpretation Ranges (UI Reference Constants)**:
    - `95–100: Exceptional / Editorial`
    - `90–94: Outstanding`
    - `80–89: Excellent`
    - `70–79: Strong`
    - `55–69: Competent`
    - `40–54: Weak`
    - `0–39: Visually Unsuccessful`
- **`FashionistaScorerImpl.kt`**: Master implementation orchestrating extractors, `FashionistaCalibration`, deterministic scorer, calibration curve, and `FashionistaScore` breakdown generation.

### Step 7: Presentation & Explainability Layer (`presentation` package)
- **`FashionistaExplanation.kt`**: UI model holding `score` (rounded Int), `coveragePercentage`, `interpretation`, and `features: List<FeatureExplanation>`.
- **`FashionistaScoreMapper.kt`**: Maps domain `FashionistaScore` to UI `FashionistaExplanation`. If a feature's `availability == 0.0`, maps to `AvailabilityStatus.NOT_MEASURABLE` with `value = null` and explanation *"Not measurable from this image."*.
- **`FashionistaDecompositionBar`**: Horizontal stacked waterfall bar chart component ([`FashionistaVisualizations.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/graphics/FashionistaVisualizations.kt#L430-L510)) that visually breaks down the deterministic math for the user:
  - **$Q_{base}$ Foundational Evidence**: Solid dark bar representing base feature weights.
  - **$Q_{interaction}$ Synergy (+)**: Green appended block representing positive cross-feature interactions.
  - **$P_{unresolved}$ Chaos Penalty (-)**: Red subtracted block representing unresolved perceptual complexity penalties.

---

## 4. Verification & Testing Matrix

1. **Mathematical Invariant Test**: Assert that for any input `FashionistaObservation`, output `score` $\in [0.0, 100.0]$ and `coverage` $\in [0.0, 1.0]$.
2. **Weighted Evidence Completeness Test**: Verify that when facial biometrics are missing (`availability = 0.0`), `presentationIntegration` does not contribute to $Q_{base}$ or $Q_{interaction}$ numerators or denominators, and `coverage` = $\frac{\sum w_i a_i}{\sum w_i}$ decreases accordingly without penalizing `score`.
3. **Single-Feature Interaction Denominator Test**: Verify that when only 1 feature is available ($Q_{int\_den} = 0.0$), `effectiveLambda` falls back to `0.0`, avoiding division-by-zero errors without reducing the base score.
4. **Zero-Availability Fail-Safe Test**: Verify that an empty `FashionistaObservation` with zero availability across all 6 features returns `FashionistaScore(score = 0.0, coverage = 0.0)` without producing `NaN` or throwing `ArithmeticException`.
5. **Lambda Blending Invariant Test**: Assert that $Q_{base}$ and $Q_{interaction}$ are blended via $(1 - \lambda)Q_{base} + \lambda Q_{interaction}$, keeping $Q$ bounded before penalties.
6. **Waterfall Math Decomposition Test**: Verify that `FashionistaDecompositionBar` visually maps $Q_{base}$, $Q_{interaction}$, and $P_{unresolved}$ into proportion-accurate stacked segments in the Compose UI.
7. **Intentional Asymmetry Test**: Verify an avant-garde asymmetric silhouette with high focal hierarchy receives a strong `silhouette` score ($> 0.80$) without penalty.
8. **On-Device Downsampling Performance Test**: Verify `VisualMassEngine` completes $64 \times 128$ pixel buffer downsampling and Center of Gravity calculations in $< 5\text{ ms}$.
9. **Deterministic Replicability Test**: Assert that identical `FashionistaObservation` inputs always produce byte-for-byte identical `FashionistaScore` outputs across thread invocations.
