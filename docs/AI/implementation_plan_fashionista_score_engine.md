# Implementation Plan: FASHIONISTA Score Engine

This document details the architectural specifications, mathematical formulation, and implementation roadmap for KoColor's **FASHIONISTA Score Engine**.

---

## 1. System Architecture & Contract

FASHIONISTA is a standalone, purely deterministic computational evaluator. It translates any observed outfit (`FashionistaObservation`) into a single, absolute aesthetic score ($0.0\text{--}100.0$) using color science, computer vision, and geometric mathematics.

### Critical System Invariants

1. **Zero AI/LLM Dependency**: Executes 100% on-device, locally, and synchronously. Zero network calls, zero LLMs, zero prompt assembly, and zero GenAI SDK dependencies.
2. **Reference-Free Evaluation**: Evaluates the outfit strictly as observed. The engine has no concept of user wardrobe history, personal preferences, weather, occasion, or recommendation logic.
3. **Deterministic Polynomial Scoring**: Core scoring relies on explicit interaction polynomials ($Q = \sum_i w_i x_i + \sum_{i,j} w_{ij} x_i x_j - P_{chaos}$) and calibrated logistic functions, rather than opaque black-box neural networks.
4. **Normalized Birkhoff Measure Stability**: Replaces unstable literal division ($M = O / C$) with normalized Order ($\in [0, 1]$) and Complexity ($\in [0, 1]$) features to guarantee numerical stability.
5. **Intentional Novelty Preservation**: Asymmetry, oversized volumes, and high contrast are evaluated as valid stylistic choices. Penalties are strictly reserved for *unresolved visual chaos*.
6. **Decoupled Confidence**: Data completeness (e.g., missing biometric patches or texture maps) lowers the `confidence` score ($0.0\text{--}1.0$) without corrupting or artificially penalizing the core aesthetic `score`.

---

## 2. Package Architecture (`:applications:kocolor:fashionista`)

```text
:applications:kocolor:fashionista
├── domain
│   ├── FashionistaObservation.kt
│   ├── FashionistaScore.kt
│   ├── FashionistaBreakdown.kt
│   └── FashionistaFeatureVector.kt
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
│   └── FashionistaScoreEngine.kt
└── FashionistaScorer.kt
```

---

## 3. Implementation Steps & Module Specifications

### Step 1: Domain API & Contracts (`domain` & `extraction` packages)
- **`FashionistaObservation`**: Data class representing raw input (extracted garments, cosmetics, spatial mass maps, optional facial/skin biometric patches). Contains zero user or weather context.
- **`FashionistaBreakdown`**: `data class FashionistaBreakdown(val composition: Double, val color: Double, val silhouette: Double, val texture: Double, val hierarchy: Double, val integration: Double)`. Holds normalized 6-dimensional sub-scores ($\in [0.0, 1.0]$).
- **`FashionistaScore`**: `data class FashionistaScore(val score: Double, val confidence: Double, val breakdown: FashionistaBreakdown)`. Note: `score` is on a $0.0\text{--}100.0$ scale (UI displays rounded integer); `confidence` reflects data completeness.
- **`FashionistaScorer`**: `interface FashionistaScorer { fun score(outfit: FashionistaObservation): FashionistaScore }` (Synchronous contract).
- **Extraction Pipeline Handoff**: Feature extractors (e.g., `ColorFeatureExtractor.kt`, `SilhouetteFeatureExtractor.kt`) act as the bridge between `FashionistaObservation` and downstream engines, flattening raw bitmaps, color metadata, and category tags into exact normalized structures ($X \in [0.0, 1.0]^6$).

### Step 2: Chromatic & Composition Extraction (`color` & `composition` packages)
- **`ColorSpace.kt` & `Ciede2000.kt`**: Converts RGB $\to$ CIELAB $\to$ $L^*C^*h^\circ$ and computes exact pairwise perceptual color distances using the CIEDE2000 ($\Delta E_{00}$) equation:
  $$\Delta E_{00} = \sqrt{ \left(\frac{\Delta L'}{k_L S_L}\right)^2 + \left(\frac{\Delta C'}{k_C S_C}\right)^2 + \left(\frac{\Delta H'}{k_H S_H}\right)^2 + R_T \left(\frac{\Delta C'}{k_C S_C}\right) \left(\frac{\Delta H'}{k_H S_H}\right) }$$
- **`CircularHueStatistics.kt`**: Implements chroma-weighted circular statistics ($x = \Sigma (C^* \times \cos(h^\circ))$, $y = \Sigma (C^* \times \sin(h^\circ))$). Neutrals (low $C^*$) contribute to lightness contrast without distorting dominant hue angles.
- **`ChromaticHarmonyEngine.kt`**: Outputs normalized $S_{color} \in [0.0, 1.0]$ evaluating hue dispersion, chroma distribution, palette relationships, and Matsuda template fit.
- **`CompositionEngine.kt`**: Outputs normalized $S_{comp} \in [0.0, 1.0]$ evaluating category adjacency (e.g., outerwear over tops), layering density, and structural coherence.

### Step 3: Silhouette, Texture & Hierarchy (`silhouette`, `texture`, `hierarchy` packages)
- **`VisualMassEngine.kt`**: Uses native Android `Bitmap.createScaledBitmap` downsampling ($64 \times 128$ pixel buffer) to synchronously calculate Visual Center of Gravity ($\bar{x} = \frac{\sum m_i x_i}{\sum m_i}$, $\bar{y} = \frac{\sum m_i y_i}{\sum m_i}$) within milliseconds without external dependencies, returning normalized $S_{silhouette} \in [0.0, 1.0]$.
- **`GlcmTextureEngine.kt`**: Extracts Gray Level Co-occurrence Matrix (GLCM) features: Order $\in [0, 1]$ (Angular Second Moment) and Complexity $\in [0, 1]$ (GLCM Entropy).
- **`TextureHarmonyEngine.kt`**: Computes $S_{texture} \in [0.0, 1.0]$ from texture frequency and material contrast safely without division by zero.
- **`VisualHierarchyEngine.kt`**: Computes $S_{hierarchy} \in [0.0, 1.0]$ evaluating focal point clarity (Primary $\to$ Secondary $\to$ Tertiary).

### Step 4: Biometric & Cosmetic Integration (`integration` package)
- **`ItaCalculator.kt`**: Implements Individual Typology Angle ($ITA = \left[\arctan\left(\frac{L^* - 50}{b^*}\right)\right] \times \frac{180}{\pi}$).
- **`CosmeticIntegrationEngine.kt`**: Calculates Michelson facial feature contrast ($C_f = \frac{L_{skin} - L_{feature}}{L_{skin} + L_{feature}}$).
- **`OutfitIntegrationEngine.kt`**: Outputs normalized $S_{integration} \in [0.0, 1.0]$. **Null-Safe Biometric Bypass**: If facial or skin biometric patches are missing, immediately returns a neutral baseline score ($0.5$) with a `confidence` modifier of $0.8$ (lowering confidence score by 0.2), preventing execution of the Michelson contrast equation on null data.

### Step 5: Deterministic Polynomial Scoring Engine (`scoring` package)
- **`InteractionModel.kt`**: Generates normalized 6D feature vector $X$ and computes cross-feature interaction terms $I_{ij} = x_i \cdot x_j$ (e.g., $S_{color} \cdot S_{hierarchy}$).
- **`DeterministicScorer.kt`**: Calculates raw polynomial aesthetic value $Q$:
  $$Q = \sum_i w_i x_i + \sum_{i,j} w_{ij} x_i x_j - P_{chaos}$$
  - **Baseline Polynomial Weights**:
    - Primary linear weights $w_i \in [0.25, 0.35]$.
    - Synergistic cross-feature interaction terms $w_{ij} \approx +0.15$ (e.g., $S_{color} \cdot S_{hierarchy}$, $S_{comp} \cdot S_{silhouette}$).
    - Chaos penalty $P_{chaos} \approx -0.40$ triggered when high texture complexity or high category density coincides with low visual hierarchy ($S_{hierarchy} < 0.3$), strictly punishing unresolved visual chaos.
- **`CalibrationCurve.kt`**: Maps $Q$ to a stable $0.0\text{--}100.0$ scale using a calibrated logistic function:
  $$Score = 100 \cdot \left( \frac{1}{1 + e^{-Q}} \right)$$
- **`FashionistaScorerImpl.kt`**: Synchronous master implementation orchestrating feature extractors, deterministic polynomial evaluation, logistic scaling, and audit breakdown construction.

---

## 4. Verification & Testing Matrix

1. **Mathematical Invariant Test**: Assert that for any input `FashionistaObservation`, output `score` $\in [0.0, 100.0]$ and `confidence` $\in [0.0, 1.0]$.
2. **Circular Hue Neutral Stability Test**: Verify gray/neutral garments (chroma $C^* < 10$) do not distort dominant hue angles across color inputs.
3. **On-Device Downsampling Performance Test**: Verify `VisualMassEngine` completes $64 \times 128$ pixel buffer downsampling and Center of Gravity calculations in $< 5\text{ ms}$.
4. **Null-Safe Biometric Bypass Test**: Verify missing facial/skin patches trigger immediate neutral return ($S_{integration} = 0.5$, `confidence = 0.8`) without throwing `NullPointerException` or executing $C_f$.
5. **Deterministic Polynomial Test**: Verify synergistic interactions ($S_{color} \cdot S_{hierarchy}$) boost $Q$ by $+0.15$, while unresolved chaos ($S_{hierarchy} < 0.3$ with high texture complexity) triggers $P_{chaos} = -0.40$.
6. **Deterministic Replicability Test**: Assert that identical `FashionistaObservation` inputs always produce byte-for-byte identical `FashionistaScore` outputs across thread invocations.
