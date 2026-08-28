The FASHIONISTA engine is a purely deterministic, reference-free computational evaluator. It translates any observed outfit into a single aesthetic score (0.0–100.0) using color science, computer vision, and geometric mathematics, operating entirely offline without LLMs, cloud APIs, or wardrobe context.

**I. Core Architectural Invariants**

* **Deterministic Math Over Black-Box ML:** The scoring engine replaces opaque Graph Neural Networks (GCNs) and Multi-Layer Perceptrons (MLPs) with deterministic feature extraction, interaction polynomials, and calibrated logistic functions.
* **Reference-Free Evaluation:** The input is an isolated `FashionistaObservation`. The engine does not care if the items exist in a wardrobe, nor does it factor in user preferences, weather, or occasions.
* **Absolute Scale:** The output is an absolute measurement of computational aesthetic quality, not a percentile ranking against a global population.
* **Decoupled Confidence:** Missing data (e.g., a photo too blurry for GLCM texture analysis) reduces the `confidence` score (0.0–1.0) but does not artificially penalize the core aesthetic `score`.
* **Intentional Novelty:** Asymmetry, oversized volumes, and high contrast are treated as features, not errors. Penalties are strictly reserved for *unresolved chaos*, rewarding intentional editorial styling.

---

**II. The 6-Pillar Mathematical Formulation**

**1. Composition ($S_{comp}$)**
Evaluates structural logic without relying on heuristic recommendation databases.

* **Metrics:** Category adjacency (e.g., outerwear over tops), proportion ratios, layering density, and structural semantic coherence.

**2. Color ($S_{color}$)**
Evaluates chromatic harmony across the entire palette distribution, avoiding reliance on a single dominant hue.

* **Mathematics:** Maps RGB to CIELAB and computes pairwise perceptual distances via the CIEDE2000 formula:

$$\Delta E_{00} = \sqrt{ \left(\frac{\Delta L'}{k_L S_L}\right)^2 + \left(\frac{\Delta C'}{k_C S_C}\right)^2 + \left(\frac{\Delta H'}{k_H S_H}\right)^2 + R_T \left(\frac{\Delta C'}{k_C S_C}\right) \left(\frac{\Delta H'}{k_H S_H}\right) }$$


* **Metrics:** Hue dispersion, chroma/lightness distribution, neutral grounding (low $C^*$ contributes to lightness contrast, not hue skew), and spatial fitting to Matsuda geometric templates.

**3. Silhouette & Proportion ($S_{silhouette}$)**
Evaluates visual shape and spatial equilibrium.

* **Mathematics:** Segments the outfit to assign visual mass ($m_i$) and calculates the Visual Center of Gravity (CoG):

$$\bar{x} = \frac{\sum m_i x_i}{\sum m_i}, \quad \bar{y} = \frac{\sum m_i y_i}{\sum m_i}$$


* **Metrics:** Vertical/horizontal visual mass distribution, negative space, and resolution of deliberate asymmetry (Visual Moment Equilibrium).

**4. Material & Texture ($S_{texture}$)**
Evaluates surface composition via normalized algorithmic information theory, explicitly avoiding the unstable $M = O / C$ division.

* **Mathematics:** Utilizes Gray Level Co-occurrence Matrices (GLCM) and Gabor filters to map normalized features: Order $\in [0,1]$ (Angular Second Moment) and Complexity $\in [0,1]$ (GLCM Entropy).
* **Metrics:** Texture frequency, material contrast, visual noise, and pattern regularity.

**5. Visual Hierarchy ($S_{hierarchy}$)**
Evaluates whether the outfit reads as a structured focal system (Primary $\rightarrow$ Secondary $\rightarrow$ Tertiary) rather than a visually chaotic flat plane.

* **Metrics:** Saliency peak isolation, accent dominance, and focal point clarity.

**6. Integration ($S_{integration}$)**
Evaluates the unification of clothing, accessories, and biometrics (when available).

* **Mathematics:** Calculates skin constitutive pigmentation via the Individual Typology Angle (ITA):

$$ITA = \left[ \arctan\left(\frac{L^* - 50}{b^*}\right) \right] \times \frac{180}{\pi}$$


* Computes cosmetic facial contrast integration via the Michelson formula:

$$C_f = \frac{L_{skin} - L_{feature}}{L_{skin} + L_{feature}}$$


* **Metrics:** Cross-domain harmony (footwear grounding, makeup/apparel chromatic linking).

---

**III. The Deterministic Scoring Model**

The six normalized sub-scores ($x_i \in [0,1]$) are passed into a deterministic interaction polynomial.

1. **Feature Vector:** $X = [S_{comp}, S_{color}, S_{silhouette}, S_{texture}, S_{hierarchy}, S_{integration}]$
2. **Interaction Terms:** Calculates critical aesthetic relationships (e.g., high color harmony $\times$ high visual hierarchy = strong composition). $I_{ij} = x_i \cdot x_j$
3. **Polynomial Evaluation:** Computes the raw aesthetic value ($Q$), actively penalizing unresolved complexity ($P_{chaos}$):

$$Q = \sum_i w_i x_i + \sum_{i,j} w_{ij} x_i x_j - P_{chaos}$$


4. **Calibration:** Maps $Q$ to a stable 0.0–100.0 scale using a calibrated logistic function:

$$Score = 100 \cdot \left( \frac{1}{1 + e^{-Q}} \right)$$



---

**IV. Public API & Module Architecture**

**The API Contract**

```kotlin
interface FashionistaScorer {
    fun score(outfit: FashionistaObservation): FashionistaScore
}

data class FashionistaScore(
    val score: Double,          // e.g., 87.4126 (UI displays '87')
    val confidence: Double,     // e.g., 0.92 (Data completeness)
    val breakdown: FashionistaBreakdown
)

data class FashionistaBreakdown(
    val composition: Double,
    val color: Double,
    val silhouette: Double,
    val texture: Double,
    val hierarchy: Double,
    val integration: Double
)

```

**Module Structure (`:applications:kocolor:fashionista`)**

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