# Architectural Reference: FASHIONISTA Score Engine

This document provides a comprehensive technical overview and architectural breakdown of the KoColor **FASHIONISTA Score Engine** (`com.zoewave.probase.kocolor.fashionista`).

---

## 1. System Philosophy & Contract

FASHIONISTA is a standalone, purely deterministic computational evaluator. It translates any observed outfit (`FashionistaObservation`) into a **standardized, reference-calibrated aesthetic score** ($0.0\text{--}100.0$) using deterministic computational measurements of the observed visual system as a whole.

```text
                     OBSERVED OUTFIT
                            │
                            ▼
                  FashionistaObservation
                            │
            ┌───────────────┼───────────────┐
            ▼               ▼               ▼
          COLOR         STRUCTURE        SURFACE
            │               │               │
            ▼               ▼               ▼
       Colorimetry     Composition     Texture
       CIEDE2000       Proportion      GLCM
       Hue Geometry    Layering        Order/Complexity
            │               │               │
            └───────────────┼───────────────┘
                            ▼
                    VISUAL HIERARCHY
                            │
                            ▼
                 PRESENTATION INTEGRATION
                   (Conditional/Optional)
                            │
                            ▼
                    FEATURE VECTOR
             (6x FeatureValue(value, avail))
                            │
                            ▼
                  INTERACTION MODEL
              Q_base + Q_interaction - P_unresolved
                            │
                            ▼
                    CALIBRATION CURVE
               100 * σ((Q - μ) / τ)
                            │
                            ▼
                     FASHIONISTA SCORE
                        0.0 - 100.0
```

### Critical Architectural Invariants

1. **Zero AI/LLM/Network Dependency**:
   - Executes 100% locally, on-device, and synchronously.
   - Zero network calls, zero LLMs, zero prompt assembly, zero cloud dependencies, and zero GenAI SDKs.
2. **Context-Free & Reference-Free Evaluation**:
   - Evaluates the outfit strictly as observed.
   - Possesses zero knowledge of user identity, wardrobe history, personal preferences, weather, occasion, or recommendation logic.
3. **Immutable Versioned Calibration Standard**:
   - All parameters ($w_i$, $w_{ij}$, $\lambda$, $P_{unresolved}$, $\mu$, $\tau$, $qMin$, $qMax$) are encapsulated in a versioned `FashionistaCalibration` object (`standardId = "FASHIONISTA"`, `version = 1`).
   - Derived offline from expert-rated reference ensembles, ensuring the Android runtime code remains frozen and reproducible.
4. **Dynamic Evidence Normalization**:
   - Missing data (e.g., flat-lay photo without a face/wearer) does **not** corrupt the score or default to arbitrary constants like $0.5$.
   - Uses a `FeatureValue(value, availability)` model ($\text{value} \in [0.0, 1.0], \text{availability} \in [0.0, 1.0]$), dynamically scaling the final equation based strictly on available evidence.
5. **Weighted Measurement Coverage**:
   - Exposes `coverage` ($\frac{\sum w_i a_i}{\sum w_i} \in [0.0, 1.0]$), strictly representing data completeness (how completely the outfit was measurable).
   - `coverage` does **not** alter or artificially deflate the raw aesthetic `score`.
6. **Evidence, Not Law**:
   - Measurements like vertical symmetry, Matsuda color templates, or GLCM complexity are treated as *evidence/features*, not definitions of good fashion.
   - Asymmetry, high contrast, and unusual proportions are valid stylistic choices. Penalties are strictly reserved for *unresolved perceptual conflicts* ($P_{unresolved}$).
7. **Thread Safety**:
   - The engine provides a synchronous, thread-safe Kotlin contract suited for execution on background coroutine workers (off the main UI thread).

---

## 2. Package Architecture (`com.zoewave.probase.kocolor.fashionista`)

```text
:applications:kocolor:fashionista
├── domain
│   ├── FashionistaObservation.kt
│   ├── FeatureValue.kt
│   ├── FashionistaFeatureVector.kt
│   ├── FashionistaScore.kt
│   └── FashionistaScorer.kt
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
│   └── ChromaticHarmonyEngine.kt
├── composition
│   └── CompositionEngine.kt
├── silhouette
│   ├── VisualMassEngine.kt
│   └── SilhouetteEngine.kt
├── texture
│   ├── GlcmTextureEngine.kt
│   └── TextureHarmonyEngine.kt
├── hierarchy
│   └── VisualHierarchyEngine.kt
├── integration
│   ├── ItaCalculator.kt
│   ├── CosmeticIntegrationEngine.kt
│   └── OutfitIntegrationEngine.kt
└── scoring
    ├── FashionistaCalibration.kt
    ├── InteractionModel.kt
    ├── DeterministicScorer.kt
    ├── CalibrationCurve.kt
    └── FashionistaScorerImpl.kt
```

---

## 3. Subsystem Breakdown & Implementation

### A. Domain Contracts (`domain` package)
- **`FashionistaObservation`**: Data class representing raw input (extracted garments, cosmetics, spatial mass maps, optional facial/skin biometric patches).
- **`FeatureValue`**: `data class FeatureValue(val value: Double, val availability: Double)` with $[0.0, 1.0]$ boundary validation.
- **`FashionistaFeatureVector`**: Holds the 6 perceptual system measurements as `FeatureValue` objects:
  - `composition: FeatureValue`
  - `colorHarmony: FeatureValue`
  - `silhouette: FeatureValue`
  - `textureHarmony: FeatureValue`
  - `visualHierarchy: FeatureValue`
  - `presentationIntegration: FeatureValue`
- **`FashionistaScore`**: `data class FashionistaScore(val score: Double, val coverage: Double, val standardId: String, val standardVersion: Int, val breakdown: FashionistaFeatureVector)`.
- **`FashionistaScorer`**: Synchronous interface contract (`fun score(outfit: FashionistaObservation): FashionistaScore`).

### B. Pure Mathematics Primitives (`math` package)
- **`Statistics.kt`**: Mean, weighted mean, variance, and standard deviation.
- **`CircularStatistics.kt`**: Chroma-weighted circular mean ($x = \Sigma (C^* \times \cos(h^\circ))$, $y = \Sigma (C^* \times \sin(h^\circ))$, $\bar{h} = \operatorname{atan2}(y, x)$) and circular variance. Low-chroma neutrals ($C^* < 10.0$) contribute to lightness contrast without distorting dominant hue angles.
- **`Geometry.kt`**: Visual Center of Gravity ($\bar{x} = \frac{\sum m_i x_i}{\sum m_i}$, $\bar{y} = \frac{\sum m_i y_i}{\sum m_i}$) and point distance calculations.
- **`Distance.kt`**: Exact CIEDE2000 ($\Delta E_{00}$) perceptual color distance equation.
- **`Normalization.kt`**: Safe division and min-max feature scaling.
- **`Logistic.kt`**: Calibrated logistic sigmoid function: $F(Q, \mu, \tau) = 100 \cdot \left( \frac{1}{1 + e^{-\frac{Q-\mu}{\tau}}} \right)$.

### C. Feature Engines & Extractors (`color`, `composition`, `silhouette`, `texture`, `hierarchy`, `integration`, `extraction`)
1. **Chromatic Harmony Engine (`color`)**:
   - Converts RGB $\to$ CIELAB $\to$ $L^*C^*h^\circ$.
   - Evaluates CIEDE2000 ($\Delta E_{00}$) pairwise distances, chroma distribution, and hue dispersion via `CircularStatistics`.
2. **Composition Engine (`composition`)**:
   - Evaluates category adjacency (e.g. outerwear over tops) and outfit completeness (Top + Bottom + Shoes or Dress + Shoes).
3. **Silhouette & Visual Mass Engine (`silhouette`)**:
   - Uses native Android `Bitmap.createScaledBitmap` ($64 \times 128$ pixel buffer) in `VisualMassEngine.kt` to calculate Visual Center of Gravity ($\bar{x}, \bar{y}$) in $< 5\text{ ms}$ without heavy CV dependencies.
4. **Texture Harmony Engine (`texture`)**:
   - `GlcmTextureEngine.kt` extracts Gray Level Co-occurrence Matrix (GLCM) features: Order $\in [0, 1]$ (Angular Second Moment) and Complexity $\in [0, 1]$ (GLCM Entropy) safely without literal Birkhoff division ($M = O / C$).
5. **Visual Hierarchy Engine (`hierarchy`)**:
   - Evaluates focal point isolation (Primary $\to$ Secondary $\to$ Tertiary statement pieces). High complexity without clear focal hierarchy triggers unresolved chaos penalties ($P_{unresolved}$).
6. **Presentation Integration Engine (`integration`)**:
   - Calculates Individual Typology Angle ($ITA = \left[\arctan\left(\frac{L^* - 50}{b^*}\right)\right] \times \frac{180}{\pi}$) and Michelson facial contrast ($C_f = \frac{L_{skin} - L_{feature}}{L_{skin} + L_{feature}}$).
   - **Null-Safe Biometric Bypass**: Flat-lay or outfit-only observations return `FeatureValue(value = 0.0, availability = 0.0)`, completely bypassing $C_f$ execution without polluting the score or crashing on null data.

### D. Bounded Nonlinear Scoring Engine (`scoring` package)
- **`FashionistaCalibration`**: Injected configuration (`standardId = "FASHIONISTA"`, `version = 1`, `featureWeights`, `interactionWeights`, `lambda = 0.20`, `unresolvedPenaltyWeight = 0.40`, `mu = 0.50`, `tau = 0.20`, `qMin = 0.0`, `qMax = 1.0`).
- **`InteractionModel`**: Generates cross-feature interaction terms $I_{ij} = x_i \cdot x_j$ for available features ($a_i > 0.0$).
- **`DeterministicScorer`**:
  - Calculates base score:
    $$Q_{base} = \frac{\sum_i w_i x_i a_i}{\sum_i w_i a_i}$$
  - Calculates interaction terms:
    $$Q_{int\_num} = \sum_{i<j} w_{ij} x_i x_j a_i a_j, \quad Q_{int\_den} = \sum_{i<j} w_{ij} a_i a_j$$
  - **Interaction Denominator Fail-Safe**: If $Q_{int\_den} == 0.0$ (e.g. single feature input), sets `effectiveLambda = 0.0` and $Q_{interaction} = Q_{base}$. Otherwise, $Q_{interaction} = \frac{Q_{int\_num}}{Q_{int\_den}}$ and `effectiveLambda = lambda`.
  - **Blended Score**:
    $$\text{Blended} = (1 - \text{effectiveLambda}) Q_{base} + \text{effectiveLambda} \cdot Q_{interaction}$$
    $$Q = (\text{Blended} - P_{unresolved})\text{.coerceIn}(qMin, qMax)$$
  - **Zero-Availability Fail-Safe**: If $\sum_i w_i a_i == 0.0$ (empty observation), short-circuits to return $Q = 0.0$ and $\text{coverage} = 0.0$.
- **`CalibrationCurve`**: Maps $Q$ to $0.0\text{--}100.0$ scale and defines UI interpretation constants:
  - `95–100`: Exceptional / Editorial
  - `90–94`: Outstanding
  - `80–89`: Excellent
  - `70–79`: Strong
  - `55–69`: Competent
  - `40–54`: Weak
  - `0–39`: Visually Unsuccessful
- **`FashionistaScorerImpl`**: Master orchestrator implementing `FashionistaScorer`.

---

## 4. Verification Matrix & Test Coverage

The subsystem is thoroughly tested in [`FashionistaScorerTest.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/test/java/com/zoewave/probase/kocolor/fashionista/FashionistaScorerTest.kt) (35 passing unit tests project-wide):

1. **Mathematical Invariants**: Asserts that `score` $\in [0.0, 100.0]$ and `coverage` $\in [0.0, 1.0]$.
2. **Dynamic Evidence Normalization**: Asserts that flat-lay observations without face biometrics set `presentationIntegration.availability = 0.0`, lowering `coverage` without corrupting the raw aesthetic `score`.
3. **Single-Feature Interaction Denominator Test**: Asserts that single-feature inputs ($Q_{int\_den} == 0.0$) fall back to `effectiveLambda = 0.0` without division-by-zero errors.
4. **Zero-Availability Fail-Safe Test**: Asserts that empty `FashionistaObservation` inputs return `score = 0.0` and `coverage = 0.0` without throwing exceptions or returning `NaN`.
5. **Deterministic Replicability**: Asserts that identical `FashionistaObservation` inputs produce byte-for-byte identical `FashionistaScore` outputs across thread invocations.
