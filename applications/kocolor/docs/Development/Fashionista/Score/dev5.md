The zero-denominator catch for $Q_{interaction}$ is a brilliant piece of defensive engineering. If an observation only successfully extracts a single feature, penalizing it with a math crash or a zero score contradicts the entire philosophy of evidence-based measurement. Falling back to an effective $\lambda = 0.0$ gracefully handles the edge case.

Renaming `wearerIntegration` to `presentationIntegration` also perfectly seals the conceptual boundary. We are scoring the *presentation* of the outfit, not the human inside it.

Here is the finalized, implementation-ready contract incorporating all five of your final corrections.

---

### 📋 Master Implementation Prompt: FASHIONISTA Engine

```markdown
# System Context & Architecture Contract
You are an expert Android Kotlin developer and Principal Software Architect implementing the **FASHIONISTA Score Engine** for KoColor.

FASHIONISTA is a standalone, purely deterministic measurement instrument. It translates any observed outfit into a **standardized, reference-calibrated computational fashion-aesthetic score** (0–100). 

**CRITICAL INVARIANTS - DO NOT DEVIATE:**
1. **Zero AI/LLM Dependency:** The engine executes 100% locally. No network calls, no LLMs, no cloud dependencies. The API is synchronous but must be thread-safe for execution on background workers.
2. **Context-Free Evaluation:** Evaluates the outfit strictly as observed. It possesses zero knowledge of the user, wardrobe, occasions, weather, or recommendation logic. 
3. **Dynamic Evidence Normalization:** Missing data uses a `FeatureValue(value, availability)` model, dynamically normalizing the final equation based strictly on available evidence.
4. **Measurement Coverage:** Expose `coverage` ($\frac{\sum w_i a_i}{\sum w_i}$) to represent data completeness, not algorithmic confidence. 
5. **Evidence, Not Law:** Vertical symmetry, Matsuda templates, and Birkhoff complexity are *evidence*. Asymmetry and unusual proportions are valid. Penalize only *unresolved perceptual conflicts* ($P_{unresolved}$).
6. **Immutable Versioned Standard:** The mathematical engine is deterministic. Calibration parameters ($w_i, w_{ij}, \lambda, \mu, \tau, qMin, qMax$) must be injected via a versioned `FashionistaCalibration` object derived offline.

---

### Implementation Instructions: Generate the following `:applications:kocolor:fashionista` modules.

#### Step 1: Domain API & Contracts (`domain` package)
*   **`FashionistaObservation`**: Raw input (garments, cosmetics, spatial mass maps, optional facial/skin biometric patches). 
*   **`FeatureValue`**: `data class FeatureValue(val value: Double, val availability: Double)`. 
*   **`FashionistaFeatureVector`**: Holds the 6 perceptual systems: `composition`, `colorHarmony`, `silhouette`, `textureHarmony`, `visualHierarchy`, `presentationIntegration`.
*   **`FashionistaScore`**: `data class FashionistaScore(val score: Double, val coverage: Double, val standardId: String, val standardVersion: Int, val breakdown: FashionistaFeatureVector)`.
*   **`FashionistaScorer`**: `interface FashionistaScorer { fun score(outfit: FashionistaObservation): FashionistaScore }`.

#### Step 2: Foundational Mathematics (`math` package)
*   **`Statistics.kt`**, **`CircularStatistics.kt`**, **`Geometry.kt`**, **`Distance.kt`**, **`Normalization.kt`**, **`Logistic.kt`**.
*   All mathematical primitives reside here to ensure the extraction engines remain strictly auditable.

#### Step 3: Extraction & Normalization (`extraction`, `color`, `composition`, `silhouette`, `texture`, `hierarchy` packages)
*   **Color Harmony**: Extract CIEDE2000 ($\Delta E_{00}$), hue dispersion, neutral proportion, and template affinity. 
*   **Composition**: Evaluate category relationships, proportion, layering, and semantic coherence. 
*   **Silhouette**: Extract horizontal/vertical mass, negative space, and silhouette ratio. Do not punish asymmetry. 
*   **Texture Harmony**: Extract GLCM/Gabor features. Evaluate frequency, orientation, coherence. 
*   **Visual Hierarchy**: Evaluate focal point isolation. 
*   *Note: All feature extractors rely on the `math` package.*

#### Step 4: Integration (`integration` package)
*   **Presentation Integration**: Calculate ITA and Michelson facial contrast. If the observation is an outfit-only image (no wearer/face), return `FeatureValue(value = 0.0, availability = 0.0)`.

#### Step 5: Bounded Nonlinear Deterministic Scoring Engine (`scoring` package)
*   **`FashionistaCalibration.kt`**: 
    `data class FashionistaCalibration(val standardId: String, val version: Int, val featureWeights: DoubleArray, val interactionWeights: DoubleArray, val lambda: Double, val unresolvedPenaltyWeight: Double, val mu: Double, val tau: Double, val qMin: Double, val qMax: Double)`
*   **`DeterministicScorer.kt`**: Computes the separated nonlinear interaction model:
    $$Q_{base} = \frac{\sum_i w_i x_i a_i}{\sum_i w_i a_i}$$
    $$Q_{int\_num} = \sum_{i<j} w_{ij} x_i x_j a_i a_j \quad | \quad Q_{int\_den} = \sum_{i<j} w_{ij} a_i a_j$$
    *Fail-Safe:* If $Q_{int\_den} == 0.0$, set `effectiveLambda = 0.0` and $Q_{interaction} = Q_{base}$. Otherwise, $Q_{interaction} = Q_{int\_num} / Q_{int\_den}$.
    $$Blended = (1 - \lambda_{eff})Q_{base} + \lambda_{eff} Q_{interaction}$$
    $$Q = (Blended - P_{unresolved})\text{.coerceIn}(qMin, qMax)$$
*   **`CalibrationCurve.kt`**: Maps bounded $Q$ to the 0–100 scale:
    $$F = 100 \cdot \sigma\left( \frac{Q-\mu}{\tau} \right)$$
*   **`FashionistaScorerImpl.kt`**: Injects extractors and `FashionistaCalibration`, runs the scorer, and outputs the final audited `FashionistaScore`.

```

Which package shall we generate first? We can start with `domain` to establish the API boundary, or `math` to lay down the foundational primitives.