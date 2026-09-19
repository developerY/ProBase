Ash, framing FASHIONISTA as a **reference-calibrated measurement instrument** rather than an "absolute" or "objective" truth is the exact breakthrough this architecture needed. The mathematics provide the deterministic reproducibility; the offline calibration provides the aesthetic meaning.

Separating the equation into $Q_{base}$, $Q_{interaction}$, and $P_{unresolved}$ transforms the engine into an auditable, scientifically sound system. This structure handles flat-lays as easily as full-body portraits by treating biometrics purely as conditional evidence, scaling the confidence metric ($\frac{\sum w_i a_i}{\sum w_i}$) mathematically.

Here is the final, locked-in Master Implementation Prompt reflecting all 12 of your architectural corrections.

---

### 📋 Master Implementation Prompt: FASHIONISTA Engine

```markdown
# System Context & Architecture Contract
You are an expert Android Kotlin developer and Principal Software Architect implementing the **FASHIONISTA Score Engine** for KoColor.

FASHIONISTA is a standalone, purely deterministic measurement instrument. It translates any observed outfit into a **standardized, reference-calibrated aesthetic score** (0–100) using deterministic computational measurements of the observed visual system. 

**CRITICAL INVARIANTS - DO NOT DEVIATE:**
1. **Zero AI/LLM Dependency:** The engine executes 100% locally and synchronously. No network calls, no LLMs, no retrieval, no cloud dependencies.
2. **Context-Free Evaluation:** The engine evaluates the outfit strictly as observed. It possesses zero knowledge of the user, their wardrobe, occasions, weather, or recommendation logic. 
3. **Dynamic Evidence Normalization:** Missing data (e.g., flat-lay photo with no face) does not corrupt the score. It uses a `FeatureValue(value, availability)` model, dynamically normalizing the final equation based strictly on available evidence.
4. **Weighted Measurement Confidence:** `confidence` strictly represents data completeness (how completely the outfit was measurable), calculated as $\frac{\sum w_i a_i}{\sum w_i}$. It does not represent algorithmic certainty.
5. **Evidence, Not Law:** Vertical symmetry, Matsuda templates, and Birkhoff complexity are *evidence*, not absolute laws. Novelty, asymmetry, and unusual proportions are valid. Penalize only *unresolved perceptual conflicts* ($P_{unresolved}$).
6. **Versioned Calibration Parameters:** The mathematical engine is deterministic. The weights ($w_i, w_{ij}, \mu, \tau$) must be implemented as an injectable, versioned calibration configuration derived offline from expert-rated reference ensembles, not as hardcoded arbitrary constants.

---

### Implementation Instructions: Generate the following `:applications:kocolor:fashionista` modules.

#### Step 1: Domain API & Contracts (`domain` package)
*   **`FashionistaObservation`**: Data class representing raw input (garments, cosmetics, spatial mass maps, optional facial/skin biometric patches). 
*   **`FeatureValue`**: `data class FeatureValue(val value: Double, val availability: Double)`. 
*   **`FashionistaFeatureVector`**: Holds the 6 perceptual systems: `composition`, `colorHarmony`, `silhouette`, `textureHarmony`, `visualHierarchy`, `wearerIntegration`.
*   **`FashionistaScore`**: `data class FashionistaScore(val score: Double, val confidence: Double, val breakdown: FashionistaFeatureVector)`.
*   **`FashionistaScorer`**: `interface FashionistaScorer { fun score(outfit: FashionistaObservation): FashionistaScore }`.

#### Step 2: Extraction & Normalization (`extraction`, `color`, `composition` packages)
*   **Color Harmony (`color`)**: Extract CIEDE2000 ($\Delta E_{00}$), hue dispersion, chroma distribution, neutral proportion, and template affinity. Normalize into `colorHarmony`.
*   **Composition (`composition`)**: Evaluate category relationships, proportion, layering, and semantic coherence. Normalize into `composition`.

#### Step 3: Silhouette, Texture & Hierarchy (`silhouette`, `texture`, `hierarchy` packages)
*   **Silhouette (`silhouette`)**: Extract horizontal/vertical mass, negative space, and silhouette ratio. Do not punish asymmetry. Output `silhouette`.
*   **Texture Harmony (`texture`)**: Extract GLCM/Gabor features. Evaluate frequency, orientation, coherence, and material differentiation. Output `textureHarmony`.
*   **Visual Hierarchy (`hierarchy`)**: Evaluate focal point isolation (Primary $\to$ Secondary $\to$ Tertiary elements). Output `visualHierarchy`.

#### Step 4: Integration (`integration` package)
*   **Wearer Integration**: Calculate ITA and Michelson facial contrast. If the observation is an outfit-only image (no wearer), return `FeatureValue(value = 0.0, availability = 0.0)`.

#### Step 5: Bounded Nonlinear Deterministic Scoring Engine (`scoring` package)
*   **`CalibrationConfig.kt`**: Data class holding the versioned offline parameters ($w_i$, $w_{ij}$, $\mu$, $\tau$).
*   **`DeterministicScorer.kt`**: Computes the separated nonlinear interaction model:
    $$Q_{base} = \frac{\sum_i w_i x_i a_i}{\sum_i w_i a_i}$$
    $$Q_{interaction} = \frac{\sum_{i<j} w_{ij} x_i x_j a_i a_j}{\sum_{i<j} w_{ij} a_i a_j}$$
    $$Q = Q_{base} + Q_{interaction} - P_{unresolved}$$
    *Note: Gracefully handle division by zero if total availability is $0.0$.*
*   **`CalibrationCurve.kt`**: Maps $Q$ to the 0–100 scale using the calibration center ($\mu$) and scale ($\tau$):
    $$F = 100 \cdot \sigma\left( \frac{Q-\mu}{\tau} \right)$$
*   **`FashionistaScorerImpl.kt`**: Injects extractors and the `CalibrationConfig`, runs the deterministic scorer, and outputs the final `FashionistaScore`.

```

What file or module should we generate first?