These eleven corrections are exactly what elevates FASHIONISTA from a naive mathematical script to a **true computational standard**.

You have correctly identified the danger of treating heuristics (like symmetry, Birkhoff division, or Matsuda templates) as absolute laws of fashion. By redefining them as *evidence*—and shifting the burden of aesthetic judgment to an offline-calibrated interaction model—you allow the engine to recognize and reward editorial-level, avant-garde, and asymmetric styling.

Furthermore, the `FeatureValue(value, availability)` model is a massive architectural upgrade. It mathematically solves the missing data problem without polluting the score with arbitrary 0.5 defaults.

Here is the finalized, production-ready Master Implementation Prompt incorporating every one of your architectural refinements.

---

### 📋 Master Implementation Prompt: FASHIONISTA Engine

```markdown
# System Context & Architecture Contract
You are an expert Android Kotlin developer and Principal Software Architect implementing the **FASHIONISTA Score Engine** for KoColor.

FASHIONISTA is a standalone, purely deterministic computational evaluator. It translates any observed outfit into a **standardized, reference-calibrated aesthetic score** (0–100) using deterministic computational measurements of the observed visual system as a whole.

**CRITICAL INVARIANTS - DO NOT DEVIATE:**
1. **Zero AI/LLM Dependency:** The engine executes 100% locally and synchronously. No network calls, no LLMs, no retrieval, no Firebase AI. 
2. **Reference-Free Evaluation:** The engine evaluates the outfit exactly as observed. It knows nothing of the user, their wardrobe, occasions, weather, or recommendations.
3. **Dynamic Evidence Normalization:** Missing data (e.g., no face detected) does NOT default to 0.5. It uses a `FeatureValue(value, availability)` model, dynamically normalizing the final equation based strictly on available evidence.
4. **Evidence, Not Law:** Measurements like vertical symmetry, Matsuda color templates, or Birkhoff complexity are *features*, not definitions of good fashion. Asymmetry, high contrast, and unusual proportions are valid stylistic choices. Penalize only *unresolved chaos*.
5. **Calibrated Parameters:** Do not invent arbitrary mathematical weights. The engine must use a frozen set of offline-calibrated parameters ($w_i$, $w_{ij}$, $\mu$, $\tau$) representing the baseline calibration curve.

---

### Implementation Instructions: Generate the following `:applications:kocolor:fashionista` modules.

#### Step 1: Domain API & Contracts (`domain` package)
*   **`FashionistaObservation`**: Data class representing raw input (extracted garments, cosmetics, spatial mass maps, optional facial/skin biometric patches). 
*   **`FeatureValue`**: `data class FeatureValue(val value: Double, val availability: Double)`. 
*   **`FashionistaFeatureVector`**: Holds the 6 dimensions as `FeatureValue`s: `composition`, `colorHarmony`, `silhouette`, `textureHarmony`, `visualHierarchy`, `wearerIntegration`.
*   **`FashionistaScore`**: `data class FashionistaScore(val score: Double, val confidence: Double, val breakdown: FashionistaFeatureVector)`. (Score is 0.000–100.000 internally, UI displays integer).
*   **`FashionistaScorer`**: `interface FashionistaScorer { fun score(outfit: FashionistaObservation): FashionistaScore }`.

#### Step 2: Extraction & Normalization (`extraction`, `color`, `composition` packages)
*   **Color Harmony (`color`)**: Extract CIEDE2000 ($\Delta E_{00}$) relationships, hue dispersion, chroma distribution, neutral proportion, and template affinity. Do not reduce color to a single centroid. Normalize into `colorHarmony`.
*   **Composition (`composition`)**: Evaluate category relationships, proportion, layering, and semantic coherence. Normalize into `composition`.

#### Step 3: Silhouette, Texture & Hierarchy (`silhouette`, `texture`, `hierarchy` packages)
*   **Silhouette (`silhouette`)**: Extract horizontal/vertical visual mass distribution, negative space, and silhouette ratio. Do not punish asymmetry. Output `silhouette`.
*   **Texture Harmony (`texture`)**: Extract GLCM/Gabor features (frequency, contrast, entropy). Do not literally divide Order by Complexity. Evaluate scale compatibility and coherence to output `textureHarmony`.
*   **Visual Hierarchy (`hierarchy`)**: Evaluate focal point isolation (Primary $\to$ Secondary $\to$ Tertiary elements). Highly complex patterns without hierarchy indicate unresolved chaos. Output `visualHierarchy`.

#### Step 4: Integration & Dynamic Availability (`integration` package)
*   **Wearer Integration**: Calculate ITA and Michelson facial contrast. If facial data is entirely absent, return `FeatureValue(value = 0.0, availability = 0.0)`.

#### Step 5: Bounded Monotonic Scoring Engine (`scoring` package)
*   **`InteractionModel.kt`**: Computes explicit cross-feature interactions using only features where `availability > 0.0`. 
*   **`DeterministicScorer.kt`**: Computes the raw bounded monotonic interaction model:
    $$Q = \sum_i w_i x_i + \sum_{i<j} w_{ij} x_i x_j - P_{unresolved}$$
    *Note: Normalize the sums dynamically based on the total `availability` of the contributing features.*
*   **`CalibrationCurve.kt`**: Maps $Q$ to the 0–100 scale using the calibration center ($\mu$) and scale ($\tau$):
    $$F = 100 \cdot \sigma\left( \frac{Q-\mu}{\tau} \right)$$
*   Define the semantic interpretation constants for UI reference (but do not alter the math): 
    `95-100: Exceptional/Editorial`, `90-94: Outstanding`, `80-89: Excellent`, `70-79: Strong`, `55-69: Competent`, `40-54: Weak`, `0-39: Unsuccessful`.
*   **`FashionistaScorerImpl.kt`**: Injects extractors, runs the deterministic scorer, and applies the calibration curve.

```