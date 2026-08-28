# Implementation Plan: FASHIONISTA Score Engine

This document details the architectural specifications, mathematical formulation, and implementation roadmap for KoColor's **FASHIONISTA Score Engine**.

---

## 1. System Architecture & Contract

FASHIONISTA is a standalone, purely deterministic computational evaluator. It translates any observed outfit (`FashionistaObservation`) into a **standardized, reference-calibrated aesthetic score** ($0.0\text{--}100.0$) using deterministic computational measurements of the observed visual system as a whole.

### Critical System Invariants

1. **Zero AI/LLM Dependency**: Executes 100% on-device, locally, and synchronously. Zero network calls, zero LLMs, zero retrieval, zero prompt assembly, and zero GenAI SDK dependencies.
2. **Reference-Free Evaluation**: Evaluates the outfit strictly as observed. The engine knows nothing of the user, their wardrobe, weather, occasion, or recommendation logic.
3. **Dynamic Evidence Normalization**: Missing data (e.g., no facial/biometric patch detected) does **not** default to arbitrary values like $0.5$. It uses a `FeatureValue(value, availability)` model ($\text{value} \in [0, 1], \text{availability} \in [0, 1]$), dynamically normalizing the final equation based strictly on available evidence.
4. **Evidence, Not Law**: Measurements like vertical symmetry, Matsuda color templates, or Birkhoff complexity are *features/evidence*, not absolute definitions of good fashion. Asymmetry, high contrast, and unusual proportions are valid stylistic choices. Penalties are strictly reserved for *unresolved chaos* ($P_{unresolved}$).
5. **Calibrated Parameters**: The engine uses a frozen set of reference-calibrated parameters ($w_i$, $w_{ij}$, $\mu$, $\tau$) representing the baseline calibration curve.
6. **Decoupled Confidence**: `confidence` is calculated as the average availability across all feature dimensions ($\frac{1}{N} \sum \text{availability}_i$). `confidence` reflects data completeness; it does **not** corrupt or artificially alter the raw aesthetic `score`.

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
│   ├── InteractionModel.kt
│   ├── DeterministicScorer.kt
│   ├── CalibrationCurve.kt
│   └── FashionistaScorerImpl.kt
└── FashionistaScorer.kt
```

---

## 3. Implementation Steps & Module Specifications

### Step 1: Domain API & Contracts (`domain` package)
- **`FashionistaObservation`**: Data class representing raw input (extracted garments, cosmetics, spatial mass maps, optional facial/skin biometric patches). Contains zero user or weather context.
- **`FeatureValue`**: `data class FeatureValue(val value: Double, val availability: Double)`. Encapsulates feature measurements ($\in [0.0, 1.0]$) alongside data presence.
- **`FashionistaFeatureVector`**: Holds the 6 dimensions as `FeatureValue` objects:
  - `composition: FeatureValue`
  - `colorHarmony: FeatureValue`
  - `silhouette: FeatureValue`
  - `textureHarmony: FeatureValue`
  - `visualHierarchy: FeatureValue`
  - `wearerIntegration: FeatureValue`
- **`FashionistaScore`**: `data class FashionistaScore(val score: Double, val confidence: Double, val breakdown: FashionistaFeatureVector)`. Note: `score` is internally $0.000\text{--}100.000$ (UI displays rounded integer); `confidence` reflects average `availability`.
- **`FashionistaScorer`**: `interface FashionistaScorer { fun score(outfit: FashionistaObservation): FashionistaScore }` (Synchronous contract).

### Step 2: Extraction & Normalization (`extraction`, `color`, `composition` packages)
- **Extraction Handoff**: Feature extractors (e.g., `ColorFeatureExtractor.kt`, `SilhouetteFeatureExtractor.kt`) bridge raw `FashionistaObservation` data into normalized `FeatureValue` structures.
- **Color Harmony (`color`)**: Converts RGB $\to$ CIELAB $\to$ $L^*C^*h^\circ$, computing CIEDE2000 ($\Delta E_{00}$) relationships, hue dispersion, chroma distribution, neutral proportion, and Matsuda template affinity without reducing color to a single centroid. Outputs normalized `colorHarmony`.
- **Composition (`composition`)**: Evaluates category relationships, proportion ratios, layering density, and structural semantic coherence. Outputs normalized `composition`.

### Step 3: Silhouette, Texture & Hierarchy (`silhouette`, `texture`, `hierarchy` packages)
- **Silhouette (`silhouette`)**: Uses native Android `Bitmap.createScaledBitmap` downsampling ($64 \times 128$ pixel buffer) in `VisualMassEngine.kt` to calculate Visual Center of Gravity ($\bar{x} = \frac{\sum m_i x_i}{\sum m_i}$, $\bar{y} = \frac{\sum m_i y_i}{\sum m_i}$) within milliseconds. Evaluates horizontal/vertical mass distribution, negative space, and silhouette ratio without penalizing intentional asymmetry. Outputs normalized `silhouette`.
- **Texture Harmony (`texture`)**: Extracts GLCM/Gabor features (frequency, contrast, entropy). Evaluates scale compatibility and material contrast safely without literal Birkhoff division ($M = O / C$). Outputs normalized `textureHarmony`.
- **Visual Hierarchy (`hierarchy`)**: Evaluates focal point isolation (Primary $\to$ Secondary $\to$ Tertiary elements). Highly complex patterns lacking clear hierarchy trigger unresolved chaos penalties. Outputs normalized `visualHierarchy`.

### Step 4: Integration & Dynamic Availability (`integration` package)
- **Wearer Integration**: Calculates Individual Typology Angle ($ITA = \left[\arctan\left(\frac{L^* - 50}{b^*}\right)\right] \times \frac{180}{\pi}$) and Michelson facial contrast ($C_f = \frac{L_{skin} - L_{feature}}{L_{skin} + L_{feature}}$).
- **Dynamic Availability**: If facial or skin biometric patches are entirely absent, returns `FeatureValue(value = 0.0, availability = 0.0)`. This completely bypasses $C_f$ execution on null data without polluting the score with arbitrary $0.5$ defaults, while setting `availability = 0.0` to reflect lower overall data completeness in `confidence`.

### Step 5: Bounded Monotonic Scoring Engine (`scoring` package)
- **`InteractionModel.kt`**: Computes explicit cross-feature interaction terms $I_{ij} = x_i \cdot x_j$ (e.g., $S_{color} \cdot S_{hierarchy}$) using **only** features where `availability > 0.0`.
- **`DeterministicScorer.kt`**: Computes the raw bounded monotonic interaction model:
  $$Q = \frac{\sum_i w_i x_i \cdot \text{avail}_i + \sum_{i<j} w_{ij} x_i x_j \cdot (\text{avail}_i \cdot \text{avail}_j)}{\sum_i w_i \text{avail}_i + \sum_{i<j} w_{ij} (\text{avail}_i \cdot \text{avail}_j)} - P_{unresolved}$$
  - **Calibrated Parameters**: Uses frozen baseline parameters ($w_i \in [0.25, 0.35]$, interaction weights $w_{ij} \approx +0.15$, $P_{unresolved} \approx -0.40$).
- **`CalibrationCurve.kt`**: Maps $Q$ to the $0\text{--}100$ scale using calibration center ($\mu$) and scale ($\tau$):
  $$F = 100 \cdot \sigma\left( \frac{Q - \mu}{\tau} \right) = 100 \cdot \left( \frac{1}{1 + e^{-\frac{Q-\mu}{\tau}}} \right)$$
  - **Semantic Interpretation Ranges (UI Constants)**:
    - `95–100: Exceptional / Editorial`
    - `90–94: Outstanding`
    - `80–89: Excellent`
    - `70–79: Strong`
    - `55–69: Competent`
    - `40–54: Weak`
    - `0–39: Visually Unsuccessful`
- **`FashionistaScorerImpl.kt`**: Master implementation orchestrating extractors, deterministic scorer, calibration curve, and `FashionistaScore` breakdown generation.

---

## 4. Verification & Testing Matrix

1. **Mathematical Invariant Test**: Assert that for any input `FashionistaObservation`, output `score` $\in [0.0, 100.0]$ and `confidence` $\in [0.0, 1.0]$.
2. **FeatureValue Availability Test**: Verify that when facial biometrics are missing (`availability = 0.0`), `wearerIntegration` does not contribute to the $Q$ denominator or numerator, and `confidence` decreases accordingly without penalizing `score`.
3. **Intentional Asymmetry Test**: Verify an avant-garde asymmetric silhouette with high focal hierarchy receives a strong `silhouette` score ($> 0.80$) without penalty.
4. **On-Device Downsampling Performance Test**: Verify `VisualMassEngine` completes $64 \times 128$ pixel buffer downsampling and Center of Gravity calculations in $< 5\text{ ms}$.
5. **Deterministic Replicability Test**: Assert that identical `FashionistaObservation` inputs always produce byte-for-byte identical `FashionistaScore` outputs across thread invocations.
