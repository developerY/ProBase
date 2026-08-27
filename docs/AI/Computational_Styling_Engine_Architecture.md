# Architectural Blueprint: The KoColor Computational Styling Engine

This document provides a comprehensive technical breakdown of the KoColor **Computational Styling Engine**. This system moves beyond simple AI-retrieval, using mathematical colorimetry and deterministic constraints to create a high-density "Reasoning Set" for on-device and cloud AI.

---

## 1. The "Deterministic-First" Philosophy

We enforce a strict boundary between **Retrieval** (Mathematics/Logic) and **Reasoning** (Aesthetics/Synthesis).

*   **Information Elimination**: We do not "prompt compress." We **eliminate irrelevant data** locally before it ever becomes a token.
*   **The Search Engine Invariant**: The AI is *not* a search engine. KoColor's local engine performs 100% of the wardrobe and vanity search work. The AI is an **Aesthetic Coordinator**.

---

## 2. The Anchor-Driven Pipeline

The engine operates as a state machine that establishes a foundation and computes outward.

### Phase 1: Anchor Selection Policy
The "Anchor" (typically a Top or Bottom) sets the mathematical center of the outfit.
1.  **FORCED**: User explicitly forced this item. Must be included even if it violates a normal constraint (recorded as a constraint violation in provenance).
2.  **LOCKED**: User explicitly locked this item in UI as an immutable anchor.
3.  **SELECTED**: User is actively exploring or focused on this item.
4.  **Automatic**: Highest context-fit + freshness match.

### Phase 2: The Color Harmony Engine
Located in [`ColorHarmonyEngine.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/color/ColorHarmonyEngine.kt), this handles perceptual color math:
*   **Space Conversion**: RGB $\to$ HSL $\to$ CIELAB.
*   **Chroma-Weighted Circular Hues**: Uses circular statistics ($x = \Sigma (\text{chroma} \times \text{weight} \times \cos(\theta))$, $y = \Sigma (\text{chroma} \times \text{weight} \times \sin(\theta))$, $\text{meanHue} = \text{atan2}(y, x)$) to prevent hue wrap-around errors and prevent low-chroma grays from skewing dominant hues.
*   **Geometry**: Calculates Complementary, Analogous, and Monochromatic distances.
*   **Perceptual Shield**: Uses CIEDE2000 ($\Delta E_{00}$) as a continuous feature to evaluate perceptual relationships and contrast.
*   **Contrast Balancing**: Ensures candidates align with the user's contrast requirement (`ColorTelemetry.contrastScore`).

### Phase 3: Role-Aware Candidate Allocations
The engine ensures the AI receives a solvable, structurally complete puzzle by distributing the provider's `maxCandidateBudget` across missing role requirements (`RoleRequirement` with min/max bounds):
*   **Wardrobe Allocations**: e.g., 3–4 Tops, 3–4 Bottoms, 2–3 Shoes, 1–2 Outerwear.
*   **Cosmetics Allocations**: e.g., 1–2 Eyes, 1 Cheek, 1–2 Lips, 1 Nail.
Candidates are selected from the highest-scoring items *within* each role bucket rather than taking global top scores.

---

## 3. The Adaptive "Step-Down" Fitting Loop

Every request passes through an **Adaptive Fit Engine** in the [`StyleSimulatorEngine`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt):

1.  **Assemble Prompt**: Construct the exact final prompt string.
2.  **Audit Tokens**: Call `provider.countTokens()`.
3.  **Iterative Step-Down**:
    -   **Step 1**: Strip metadata (Expanded $\to$ Balanced $\to$ Minimal).
    -   **Step 2**: Reduce $K$ (e.g., $16 \to 14 \to 12$).
    -   **Step 3**: Repeat until the request fits the specific provider's budget.

---

## 4. Privacy & Security Invariants

### Type-Safe Data Bifurcation
*   **Cloud Tier**: Strictly typed to accept only `AiInput.TextOnly` (containing `ColorTelemetry` / `AppearanceProfile` vectors and text manifests). Transmission of raw pixels to the cloud is compile-time impossible.
*   **Local Tier (Multimodal)**: On-device providers (Gemini Nano) accept `AiInput.Multimodal` for texture/drape analysis because the data never leaves the NPU.

### Multi-Tier Deterministic Caching
The [`PromptCacheRepository`](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/local/src/main/java/com/zoewave/probase/features/ai/local/data/PromptCacheRepository.kt) stores results indexed by a SHA-256 fingerprint derived from post-retrieval deterministic state:
`Selected IDs + missingRoleRequirements + occasion + weatherTempC + uvIndex + telemetry + providerId`.
This guarantees that different occasions (e.g., casual morning vs formal evening) or weather conditions generating different role gaps produce distinct cache keys even when locking the same shirt.

---

## 5. UI Rehydration & Mapping

To solve the "Last Mile" problem, we implemented **Greedy Rehydration** in [`VisualBlueprintModels.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/graphics/VisualBlueprintModels.kt):

*   **Anchor Fail-Safe**: If the Generative AI accidentally omits a `LOCKED` or `FORCED` anchor (clothing or cosmetic) from its final JSON blueprint array, the rehydrator intercepts the result and manually re-injects the locked item back into the `VisualBlueprintData`, ensuring the "user is always correct" invariant holds at the presentation layer.
*   **Keyword Fallback**: If a product's primary category is generic (e.g., `ACTIVEWEAR`), the mapper scans the name for "tank", "pant", "eye", etc.
*   **No Item Left Behind**: Any item returned by the AI that doesn't fit a primary slot is greedily assigned to an available empty slot to ensure 100% visualization.
*   **Outerwear Support**: Added a dedicated `OUTER` slot and anchor point to the body silhouette for complex layering.

---

## 6. Observability: `KoColor_Telemetry`

Every simulation logs a structured metric set to aid NPU tuning:
*   `execution_tier_used`: (e.g., `local_nano`, `firebase_ai`, `cache_firebase`).
*   `retrieval_k_limit`: The $K$ used in the fit-loop.
*   `serialization_strategy`: The metadata level used.
*   `latency_ms`: Total execution time.
*   `tokens_used`: Exact prompt cost.
