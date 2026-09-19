```markdown
# System Context & Architecture Contract
You are an expert Android Kotlin developer and Principal Software Architect implementing the **FASHIONISTA Score Engine** for KoColor.

FASHIONISTA is a standalone, purely deterministic computational evaluator. It translates any observed outfit into a single aesthetic score (0.0–100.0) using color science, computer vision, and geometric mathematics. 

**CRITICAL INVARIANTS - DO NOT DEVIATE:**
1. **Zero AI/LLM Dependency:** This engine executes 100% locally and synchronously. No network calls, no LLMs, no prompt assembly, no Firebase AI, and no GenAI SDKs. 
2. **Reference-Free Evaluation:** The engine evaluates the outfit exactly as observed. It does not know about the user's wardrobe, preferences, weather, occasion, or recommendation logic. 
3. **Deterministic Math:** The core scoring relies on explicit interaction polynomials ($Q = \sum_i w_i x_i + \sum_{i,j} w_{ij} x_i x_j - P_{chaos}$) and calibrated logistic functions, NOT opaque neural networks.
4. **Normalized Texture:** Do NOT implement Birkhoff's aesthetic measure as a literal division ($M = O / C$). Implement normalized features (Order $\in [0,1]$, Complexity $\in [0,1]$) to ensure numerical stability.
5. **Intentional Novelty:** Asymmetry and high contrast are valid stylistic choices. Do not hardcode them as "bad". Penalize only unresolved chaos.

---

### Implementation Instructions: Generate the following `:applications:kocolor:fashionista` modules sequentially.

#### Step 1: Domain API & Contracts (`domain` package)
*   **`FashionistaObservation`**: Data class representing the raw input (extracted colors, category relationships, spatial mass maps, optional facial/skin biometric patches). It does *not* contain user context.
*   **`FashionistaBreakdown`**: `data class FashionistaBreakdown(val composition: Double, val color: Double, val silhouette: Double, val texture: Double, val hierarchy: Double, val integration: Double)`
*   **`FashionistaScore`**: `data class FashionistaScore(val score: Double, val confidence: Double, val breakdown: FashionistaBreakdown)`. Note: `confidence` reflects data completeness (e.g., blurry image), it does *not* alter the raw `score`.
*   **`FashionistaScorer`**: `interface FashionistaScorer { fun score(outfit: FashionistaObservation): FashionistaScore }` (Synchronous).

#### Step 2: Chromatic & Composition Extraction (`color` & `composition` packages)
*   **`ColorSpace.kt` & `Ciede2000.kt`**: Implement RGB $\to$ CIELAB $\to$ $L^*C^*h^\circ$ and the exact CIEDE2000 ($\Delta E_{00}$) equation.
*   **`CircularHueStatistics.kt`**: Implement chroma-weighted circular statistics ($x = \Sigma (C^* \times \cos(h^\circ))$, $y = \Sigma (C^* \times \sin(h^\circ))$). Neutrals (low $C^*$) contribute to lightness contrast, not hue skew.
*   **`ChromaticHarmonyEngine.kt`**: Output normalized `S_color` evaluating hue dispersion, chroma distribution, and palette relationships.
*   **`CompositionEngine.kt`**: Output normalized `S_comp` evaluating category adjacency, layering density, and structural coherence.

#### Step 3: Silhouette, Texture & Hierarchy (`silhouette`, `texture`, `hierarchy` packages)
*   **`VisualMassEngine.kt`**: Calculate Visual Center of Gravity ($\bar{x} = \frac{\sum m_i x_i}{\sum m_i}$) based on chromatic density and saliency. Output normalized `S_silhouette`.
*   **`GlcmTextureEngine.kt`**: Implement Gray Level Co-occurrence Matrix (GLCM) extraction (Angular Second Moment for Order, Entropy for Complexity).
*   **`TextureHarmonyEngine.kt`**: Output normalized `S_texture` based on texture frequency and material contrast (avoiding division by zero).
*   **`VisualHierarchyEngine.kt`**: Output normalized `S_hierarchy` evaluating focal point clarity (Primary $\to$ Secondary $\to$ Tertiary).

#### Step 4: Biometric Integration (`integration` package)
*   **`ItaCalculator.kt`**: Implement Individual Typology Angle ($ITA = [\arctan((L^* - 50)/b^*)] \times 180/\pi$).
*   **`CosmeticIntegrationEngine.kt`**: Calculate Michelson facial feature contrast ($C_f = \frac{L_{skin} - L_{feature}}{L_{skin} + L_{feature}}$). 
*   **`OutfitIntegrationEngine.kt`**: Output normalized `S_integration`. If facial data is missing, gracefully return a neutral score and flag lower confidence.

#### Step 5: Deterministic Scoring Engine (`scoring` package)
*   **`InteractionModel.kt`**: Accepts the normalized 6-dimensional feature vector $X$. Calculates cross-feature interaction terms $I_{ij} = x_i \cdot x_j$ (e.g., Color Harmony $\times$ Visual Hierarchy).
*   **`DeterministicScorer.kt`**: Calculates the raw polynomial aesthetic value: $Q = \sum_i w_i x_i + \sum_{i,j} w_{ij} x_i x_j - P_{chaos}$. (Use mocked, mathematically sound weights for the initial implementation).
*   **`CalibrationCurve.kt`**: Maps $Q$ to a stable 0.0–100.0 scale using a logistic function: $Score = 100 \cdot (\frac{1}{1 + e^{-Q}})$.
*   **`FashionistaScorerImpl.kt`**: Injects all engines, processes the `FashionistaObservation`, runs the `DeterministicScorer`, applies the `CalibrationCurve`, and returns the final `FashionistaScore`.

```