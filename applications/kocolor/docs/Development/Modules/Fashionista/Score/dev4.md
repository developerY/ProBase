The $\lambda$ parameter mathematically resolves a critical normalization drift. If both $Q_{base}$ and $Q_{interaction}$ are individually normalized to a $[0,1]$ scale, adding them arbitrarily inflates the raw value to a $[0,2]$ range, forcing $\mu$ and $\tau$ to hide the scaling distortion. Blending them via $Q = (1-\lambda)Q_{base} + \lambda Q_{interaction} - P_{unresolved}$ guarantees the foundational score remains strictly bounded before penalties are applied.

Renaming `confidence` to `measurementCoverage` is also a massive structural upgrade. It cleanly separates the algorithmic evaluation of the outfit from the physical completeness of the data payload.

Here is the finalized, production-ready Master Implementation Prompt integrating the $\lambda$ parameter, the `math` package, and the immutable versioned calibration standard.

---

### 📋 Master Implementation Prompt: FASHIONISTA Engine

```markdown
# System Context & Architecture Contract
You are an expert Android Kotlin developer and Principal Software Architect implementing the **FASHIONISTA Score Engine** for KoColor.

FASHIONISTA is a standalone, purely deterministic measurement instrument. It translates any observed outfit into a **standardized, reference-calibrated computational fashion-aesthetic score** (0–100). 

**CRITICAL INVARIANTS - DO NOT DEVIATE:**
1. **Zero AI/LLM Dependency:** The engine executes 100% locally and synchronously. No network calls, no LLMs, no retrieval, no cloud dependencies.
2. **Context-Free Evaluation:** The engine evaluates the outfit strictly as observed. It possesses zero knowledge of the user, their wardrobe, occasions, weather, or recommendation logic. 
3. **Dynamic Evidence Normalization:** Missing data uses a `FeatureValue(value, availability)` model, dynamically normalizing the final equation based strictly on available evidence.
4. **Measurement Coverage:** The API exposes `coverage` (calculated as $\frac{\sum w_i a_i}{\sum w_i}$), representing data completeness, not algorithmic confidence. 
5. **Evidence, Not Law:** Vertical symmetry, Matsuda templates, and Birkhoff complexity are *evidence*. Novelty, asymmetry, and unusual proportions are valid. Penalize only *unresolved perceptual conflicts* ($P_{unresolved}$).
6. **Immutable Versioned Standard:** The mathematical engine is deterministic. The weights must be implemented via an injectable, versioned `FashionistaCalibration` derived offline from expert-rated reference ensembles.

---

### Implementation Instructions: Generate the following `:applications:kocolor:fashionista` modules.

#### Step 1: Domain API & Contracts (`domain` package)
*   **`FashionistaObservation`**: Raw input (garments, cosmetics, spatial mass maps, optional facial/skin biometric patches). 
*   **`FeatureValue`**: `data class FeatureValue(val value: Double, val availability: Double)`. 
*   **`FashionistaFeatureVector`**: Holds the 6 perceptual systems: `composition`, `colorHarmony`, `silhouette`, `textureHarmony`, `visualHierarchy`, `wearerIntegration`.
*   **`FashionistaScore`**: `data class FashionistaScore(val score: Double, val coverage: Double, val breakdown: FashionistaFeatureVector)`.
*   **`FashionistaScorer`**: `interface FashionistaScorer { fun score(outfit: FashionistaObservation): FashionistaScore }`.

#### Step 2: Foundational Mathematics (`math` package)
*   **`Statistics.kt`**, **`CircularStatistics.kt`**, **`Geometry.kt`**, **`Distance.kt`**, **`Normalization.kt`**, **`Logistic.kt`**.
*   All mathematical primitives must reside here to ensure the extraction engines remain strictly auditable.

#### Step 3: Extraction & Normalization (`extraction`, `color`, `composition`, `silhouette`, `texture`, `hierarchy` packages)
*   **Color Harmony**: Extract CIEDE2000 ($\Delta E_{00}$), hue dispersion, chroma distribution, neutral proportion, and template affinity. 
*   **Composition**: Evaluate category relationships, proportion, layering, and semantic coherence. 
*   **Silhouette**: Extract horizontal/vertical mass, negative space, and silhouette ratio. Do not punish asymmetry. 
*   **Texture Harmony**: Extract GLCM/Gabor features. Evaluate frequency, orientation, coherence, and material differentiation. 
*   **Visual Hierarchy**: Evaluate focal point isolation (Primary $\to$ Secondary $\to$ Tertiary elements). 
*   *Note: All feature extractors rely on the `math` package.*

#### Step 4: Integration (`integration` package)
*   **Wearer Integration**: Calculate ITA and Michelson facial contrast. If the observation is an outfit-only image (no wearer), return `FeatureValue(value = 0.0, availability = 0.0)`.

#### Step 5: Bounded Nonlinear Deterministic Scoring Engine (`scoring` package)
*   **`FashionistaCalibration.kt`**: 
    `data class FashionistaCalibration(val standardId: String, val version: Int, val featureWeights: DoubleArray, val interactionWeights: DoubleArray, val interactionContribution: Double, val unresolvedPenaltyWeight: Double, val mu: Double, val tau: Double)`
*   **`DeterministicScorer.kt`**: Computes the separated nonlinear interaction model using $\lambda$ (`interactionContribution`):
    $$Q_{base} = \frac{\sum_i w_i x_i a_i}{\sum_i w_i a_i}$$
    $$Q_{interaction} = \frac{\sum_{i<j} w_{ij} x_i x_j a_i a_j}{\sum_{i<j} w_{ij} a_i a_j}$$
    $$Q = (1 - \lambda)Q_{base} + \lambda Q_{interaction} - P_{unresolved}$$
*   **`CalibrationCurve.kt`**: Maps $Q$ to the 0–100 scale using the calibration center ($\mu$) and scale ($\tau$):
    $$F = 100 \cdot \sigma\left( \frac{Q-\mu}{\tau} \right)$$
*   **`FashionistaScorerImpl.kt`**: Injects extractors and `FashionistaCalibration`, runs the deterministic scorer, and outputs the final `FashionistaScore`.

```

The mathematical foundation is pristine. Which of the five implementation steps should we generate the Kotlin files for first?