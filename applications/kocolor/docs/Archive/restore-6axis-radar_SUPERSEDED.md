> SUPERSEDED 2026-11-05 — Product decision (pre-release, no users shipped): KoColor ships the honest 3-axis breakdown matching the live 3-evaluator engine. The 6-axis restoration proposed below was rejected in favor of updating the store listing draft to the 3-axis design; see the listing files and Tasks.md 1.2 item 4. Retained for historical context only — do not execute its prompts.

Most of the deleted code was dead weight or broken math and should stay deleted, **except for `GreedyRehydratorTest`, two real math functions (`calculateMichelsonContrast` and `VisualMassEngine`), and the real 6-axis breakdown calculation** needed to power the 6-axis radar chart shown in your UI and Play Store release notes.

### Verdict on Each Deleted Item

| Deleted Item(s) | Verdict | Why |
| --- | --- | --- |
| **`ColorHarmonyEngine.calculateDeltaE` & `calculateFashionistaScore**` | **Keep Deleted** | `calculateDeltaE` was dimensionally wrong ($\sqrt{dL^2+dC^2+dH^2}$ mixed degrees with $L^*/C^*$), and `calculateFashionistaScore` caused the UI score to disagree with the audited score.

|
| **`GreedyRehydratorTest`** | **Rebuild Immediately** | Stripping fail-safes from `GreedyRehydrator` is the exact reason to update its unit test now rather than leaving a P0 refactor untested until Phase 4.3.

|
| **`calculateMichelsonContrast` & `VisualMassEngine**` | **Restore Math Only** | While their parent engines were unbound, the assessment confirmed `CosmeticIntegrationEngine.kt:16` (`calculateMichelsonContrast`) and `VisualMassEngine.kt:16` (64×128 alpha×darkness center-of-gravity) were textbook CV/math implementations worth keeping.

|
| **The 12 other `fashionista.*` 6-Feature Scorer Files** | **Keep Deleted (or Rebuild with Real Tag Math)** | In the old code, these had zero DI bindings and 4 of the 6 engines were fake stubs (`GlcmTextureEngine` returned constant `0.69`, `SilhouetteEngine` returned constant `0.70/0.80`, `VisualHierarchyEngine` counted `brand != null`, and `CosmeticIntegrationEngine` divided lightness by `100`).

|

---

### The Catch: Your 6-Axis Radar Chart

Your Play Store release notes and app UI prominently display a **6-axis radar chart** below the 0–100 `FashionistaScore`. Previously, because the 6-feature pipeline was disconnected, the live 3-evaluator `FashionistaEngine` fed a fake hardcoded all-`0.85` vector into `FashionistaScore.breakdown`.

You have two ways to handle this:

* **Prompt A (Recommended — Lean & Real):** Keep the 12 stub files deleted, restore `GreedyRehydratorTest` and `calculateMichelsonContrast` / `VisualMassEngine`, and compute a **real 6-axis `breakdown**` directly inside the live `FashionistaEngine` using your existing catalog metadata (Color, Silhouette, Contrast, Cosmetic Integration via Michelson contrast, Material/Texture compatibility, and Composition completeness).
* **Prompt B (Rebuild the 13 `fashionista.*` Classes with Real Math):** If you want those modular `fashionista.*` classes back in the codebase (and wired into Hilt DI), rebuild them without any hardcoded constants or `brand != null` hacks.



---

### Option A Prompt (Recommended: Restore Test + Salvage Real Math + Real 6-Axis Breakdown in Live Engine)

Copy and paste this into your coding agent:

# AGENT TASK: Restore `GreedyRehydratorTest`, Salvage Real CV Math, and Compute Real 6-Axis Breakdown

Following the Phase 1.0 cleanup, complete these three targeted restorations so no real math, UI radar data, or test coverage is lost:

## 1. Restore and Rewrite `GreedyRehydratorTest` Immediately
Do not leave `GreedyRehydratorTest` deleted until Phase 4.3. Recreate `com.zoewave.probase.kocolor.features.analyzer.simulator.ui.GreedyRehydratorTest` now to test the refactored pure ID-to-UI mapper:
1. Verify that `GreedyRehydrator` maps resolved `selectedClothingIds` and `selectedCosmeticIds` directly to their corresponding `OutfitSlot` / UI roles without mutating or dropping locked anchors.
2. Verify that `ONE_PIECE` / `DRESSES` items map cleanly to satisfy upper/lower body display expectations without triggering inventory-wide fallbacks.
3. Verify that `GreedyRehydrator` passes through the canonical `FashionistaScore` from the simulator evaluation result channel without rescoring via `ColorHarmonyEngine`.
4. Verify that out-of-manifest inventory items are never injected by `GreedyRehydrator`.

## 2. Salvage `calculateMichelsonContrast` and `VisualMassEngine`
The deleted `CosmeticIntegrationEngine.kt` and `VisualMassEngine.kt` contained two verified mathematical implementations that should not be lost:
1. Restore `calculateMichelsonContrast(lMin: Double, lMax: Double): Double` (`(lMax - lMin) / (lMax + lMin)` with zero-denominator guard on luminance/lightness) into `com.zoewave.probase.kocolor.fashionista.math.Distance` (or `ColorSpace.kt`).
2. Restore `com.zoewave.probase.kocolor.fashionista.silhouette.VisualMassEngine` (the 64x128 downscaled alpha * darkness center-of-gravity bitmap analyzer) so on-device image analysis in `AnalyzerEngine` or future silhouette grading can invoke it.

## 3. Populate a Real 6-Axis `breakdown` in `FashionistaEngine` (No `0.85` Placeholders)
The UI renders a 6-axis radar chart from `FashionistaScore.breakdown`, which previously defaulted to a fake all-`0.85` vector. Keep the 12 dead stub classes deleted, and instead compute all 6 normalized `[0.0, 1.0]` breakdown axes deterministically inside `FashionistaEngine` from the resolved outfit and cosmetics:
1. `colorHarmony`: `colorScore / 100.0` (from CIEDE2000 / LCh harmony evaluation).
2. `silhouette`: `silhouetteScore / 100.0` (from `SilhouetteGrader`).
3. `contrastDepth`: `contrastScore / 100.0` (from `ContrastAnchorEvaluator`).
4. `cosmeticIntegration`: Compute using `calculateMichelsonContrast` between the outfit's mean lightness ($L^*$) and the selected pigment cosmetics' ($L^*$, `EYES`/`DIMENSION`/`LIPS`/`NAILS`) combined with temperature alignment (fallback to `colorScore / 100.0` if no cosmetics are selected).
5. `textureHarmony`: Compute from the material/finish diversity of resolved garments (rewarding complementary material pairings like structured wool/denim/leather with soft cotton/silk/linen rather than all-identical synthetic finishes; `0.75` neutral baseline only when all materials are `UNKNOWN`).
6. `composition`: Ratio of filled required garment slots + active pigment cosmetic roles to target roles, penalized if chroma isolation (`cStar > 30`) exceeds 2 competing statement pieces.

Run `./gradlew testDebugUnitTest` and confirm `GreedyRehydratorTest` and all `FashionistaEngine` tests pass.

---

### Option B Prompt (Only If You Want All 13 `fashionista.*` Classes Rebuilt and Wired into Hilt)

If you prefer to restore all 13 deleted `fashionista.*` classes as first-class modular engines instead of keeping them deleted, use this prompt:


# AGENT TASK: Rebuild the 6-Feature `fashionista.*` Engines with Real Math and Wire into `FashionistaEngine`

Recreate the deleted `com.zoewave.probase.kocolor.fashionista.*` modules, replacing every legacy constant stub with real deterministic math over `ClothingItem` and `CosmeticItem`, and wire them into `FashionistaEngine` and Hilt (`FashionistaModule`):

1. **`ChromaticHarmonyEngine`**:
   - Compute chroma-weighted circular hue statistics and pairwise CIEDE2000 (`Distance.kt`).
   - Remove the legacy `0.5` hue floor and replace quantized `{0.6, 0.75, 0.9}` steps with a smooth continuous score over `[0.0, 1.0]`. Remove unused `meanHue`.
2. **`SilhouetteEngine` & `VisualMassEngine`**:
   - Restore `VisualMassEngine` (64x128 alpha * darkness center-of-gravity).
   - In `SilhouetteEngine`, instead of returning hardcoded `0.70/0.80`, delegate garment proportion grading to `SilhouetteGrader` (and `VisualMassEngine` when bitmaps are present) to output a real `[0.0, 1.0]` proportion score.
3. **`GlcmTextureEngine` & `TextureHarmonyEngine`**:
   - Instead of returning constant `0.75 / 0.40 -> 0.69`, evaluate garment `material` and cosmetic `finish` pairings: calculate contrast between matte/structured textiles (denim, wool, leather, linen) and lustrous/smooth finishes (silk, satin, high-shine), returning a real `[0.0, 1.0]` score.
4. **`CosmeticIntegrationEngine` & `OutfitIntegrationEngine`**:
   - Restore `calculateMichelsonContrast`.
   - Instead of `mean(L*) / 100.0` (which rewarded lighter makeup regardless of outfit), score the relational temperature alignment (`WARM`/`COOL`/`NEUTRAL`), ΔE harmony, and Michelson luminance contrast between the user's `ColorTelemetry` + garment palette and the selected `EYES`, `DIMENSION`, `LIPS`, and `NAILS` items.
5. **`VisualHierarchyEngine`**:
   - Instead of counting `brand != null` as "statement pieces", identify focal-point isolation mathematically using CIELCh chroma (`cStar >= 25.0`) and lightness contrast (`|L* - meanL*|`), rewarding outfits with 1–2 clear focal pieces and penalizing visual clutter (`>= 4` high-chroma competing pieces) or flat monotony.
6. **`CompositionEngine`**:
   - Evaluate slot completeness (`TOP`+`BOTTOM`+`SHOES` or `ONE_PIECE`+`SHOES`), seasonal/thermal coherence, and cosmetic role coverage.
7. **`CalibrationCurve`, `InteractionModel`, `DeterministicScorer`, `FashionistaScorerImpl`**:
   - Recenter `CalibrationCurve` with `mu = 0.5, tau = 0.14` so `[0, 1]` maps across the full `[0, 100]` range and `EXCEPTIONAL_EDITORIAL` (`95+`) is reachable.
   - Bind `FashionistaScorer` in `FashionistaModule` and feed its 6 real feature scores into `FashionistaScore.breakdown`.
8. **Restore `GreedyRehydratorTest`**:
   - Rewrite `GreedyRehydratorTest` to verify pure ID-to-slot mapping, `ONE_PIECE` handling, and anchor preservation.

Run `./gradlew testDebugUnitTest` and verify all tests compile and pass.

--- 
Deleted files
Deleted Files (14 total):
• com.zoewave.probase.kocolor.fashionista.hierarchy.VisualHierarchyEngine
• com.zoewave.probase.kocolor.fashionista.integration.CosmeticIntegrationEngine
• com.zoewave.probase.kocolor.fashionista.integration.OutfitIntegrationEngine
• com.zoewave.probase.kocolor.fashionista.color.ChromaticHarmonyEngine
• com.zoewave.probase.kocolor.fashionista.composition.CompositionEngine
• com.zoewave.probase.kocolor.fashionista.scoring.CalibrationCurve
• com.zoewave.probase.kocolor.fashionista.scoring.DeterministicScorer
• com.zoewave.probase.kocolor.fashionista.scoring.InteractionModel
• com.zoewave.probase.kocolor.fashionista.scoring.FashionistaScorerImpl
• com.zoewave.probase.kocolor.fashionista.texture.GlcmTextureEngine
• com.zoewave.probase.kocolor.fashionista.texture.TextureHarmonyEngine
• com.zoewave.probase.kocolor.fashionista.silhouette.VisualMassEngine
• com.zoewave.probase.kocolor.fashionista.silhouette.SilhouetteEngine
• com.zoewave.probase.kocolor.features.analyzer.simulator.ui.GreedyRehydratorTest (Deleted because fail-safes were stripped from the class, rendering the test suite temporarily invalid. Will be re-added in P4.3 when deterministic testing is implemented).

Code Level Details:
• ColorHarmonyEngine.kt: Removed the calculateDeltaE method. This method incorrectly mixed hue degrees (h) with linear luminance/chroma ($L^* / C^*$), resulting in dimensionally invalid distance metric math. Also removed calculateFashionistaScore from this file as it represented a third disconnected evaluation calculation pipeline.

~~~
ReView
Read applications/kocolor/docs/ProductFixes/Directive/restore.md

◆ One doc-level check on restore.md's core premise — whether a 6-axis radar is actually promised anywhere outside the old UI.

◆ Ran command · Check store docs for radar promises · ✓ · 0.3s · ctrl+o

◆ My assessment: restore.md's core premise is correct — and I can prove it from the docs alone. But both of its options need rework before execution, and it sets up a real product decision you should make
  explicitly.

  The decisive finding: the store listing requires 6 axes. Both applications/kocolor/docs/Operations/Legal/PlayStore/GooglePlayStoreListing.md:35 and
  applications/kocolor/docs/Operations/Legal/PlayStore/StoreListingDescription.md:35 advertise a "6-Axis Aesthetic Radar" with named axes: Composition, Color Harmony, Silhouette, Texture, Visual Hierarchy,
  Presentation Integration. The audit's 3-axis triangle therefore contradicts the listing — the exact listing-vs-reality pattern from checklist must-fix #3. So either the 6-axis radar comes back with real
  numbers (restore.md's direction), or the listing copy changes (new screenshots, new review risk). Shipping 3-axis UI against 6-axis listing copy is not an option.

  But neither option matches the listing as written. The listing's six axes are the old FashionistaFeatureVector fields. Option A substitutes contrastDepth and cosmeticIntegration — neither appears in the
  listing — and drops Visual Hierarchy and Presentation Integration. If listing fidelity is the goal, the breakdown axes must be exactly the six listed names, including real definitions for the two hard ones
  (Option B's focal-point math covers Visual Hierarchy; Presentation Integration currently has no valid definition anywhere — the old one was average-lightness plus biometric gating).

  Technical holes, grouped:

  • Factual errors: A1 claims the live colorScore comes from "CIEDE2000 / LCh harmony" — wrong; the live evaluator uses HSL hue rules (CIEDE2000 was deleted with ChromaticHarmonyEngine). The τ=0.14 "full
    [0,100]" claim is imprecise (~2.7–97.3; "reaches 95+" is the accurate wording).
  • Near-constant axes by construction: upstream slot completion now guarantees filled slots, so A6's slot-ratio is ~1.0 always — the axis would carry signal only in its clutter penalty. Name it honestly or
    restructure it.
  • Distances presented as scores: Michelson contrast and texture "complementarity" have no scoring functions specified (is more contrast better? what curve?). Without target curves the agent invents
    uncalibrated math — stubs with fancier formulas. Same for the "0.75 neutral baseline," which reintroduces exactly the hardcoded-constant species just killed.
  • Display-vs-gate ambiguity: the 40/35/25 approval uses 3 evaluators, so 3 radar axes can't affect the score. That's defensible for a radar (informational display + separate score), but it must be stated
    explicitly, or you've built fake-0.85s with extra steps.
  • Option B recreates the dual-scorer problem: wiring FashionistaScorer alongside FashionistaEngine without naming the canonical scorer resurrects the ambiguity the kill just resolved. Don't execute as
    written.
  • Salvage without callers: Michelson and VisualMassEngine return with no specified call site ("AnalyzerEngine or future grading" is hand-waving) — restored math with no callers is dead code again, the same
    trap. Name the caller or don't restore.
  • Inherited holes: the appended deletion list duplicates the audit verbatim (including the "14 total" count and nonexistent "P4.3"), still omitting extractors, domain types, and FashionistaScorerTest
    disposition. Reference the audit; don't copy it. FeatureValue reuse vs deletion and standardVersion unification remain unreconciled.

  Recommended path: (1) Decide product-side: keep the 6-axis listing promise (my recommendation — cheaper than new copy/screenshots/review) and revert the radar to 6 axes with the listing's exact axis names.
  (2) Rewrite Option A around those six axes with a scoring function per axis and an explicit display-vs-gate statement. (3) Do the two no-regret items immediately regardless: rewrite GreedyRehydratorTest now,
  and reconcile FeatureValue/standardVersion. (4) Park Option B until someone names the canonical scorer.

  Say the word and I'll rewrite restore.md accordingly once you've made the 6-axis call — or draft the listing-copy change instead if you'd rather keep the triangle.