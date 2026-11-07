`Tasks_2.md` is **95% ready to execute** and includes critical codebase-specific refinements—specifically deferring `fashionista/math/*` and `ColorSpace.kt` in Phase 1.0, introducing the shared `AiExecutionResult` carrier in Phase 2.3, and distinguishing `ClothingItem.formality` (`Formality`) from `FormalityLevel` in Phase 3.2.

However, cross-checking the phases against each other reveals **three surgical issues** (one cross-phase contradiction and two Checkpoint 1 compile traps) plus stray wrapper text that you should fix before running the agent:

### 1. Phase 2.2.3 vs. Phase 4.3.1 Contradiction (`slotsPerCategory` in `adaptContextToProvider`)

* **The Conflict:** Phase 2.2 item 3 instructs the agent to update `slotsPerCategory` inside `adaptContextToProvider` so the floor steps down to `1` when `K = 4`. Then Phase 4.3 item 1 instructs the agent to **delete** the `categorizedPool`/`slotsPerCategory` budget logic in `adaptContextToProvider` *and* delete `DeterministicContextEngine.enforceRoleDiversity`.


* **Why It Breaks:** `RoleGapAnalyzer` only computes *which* roles are missing from the anchors (`missingRoleRequirements`); it does not partition the `K` candidate budget across categories for the prompt manifest. Deleting both `slotsPerCategory` and `enforceRoleDiversity` removes role-diversity partitioning from the prompt manifest entirely and undoes Phase 2.2 item 3.


* **The Fix in 4.3.1:** Instead of deleting `categorizedPool`/`slotsPerCategory` in `adaptContextToProvider`, instruct the agent to filter `categorizedPool` by `selectionState.missingRoleRequirements` (wiring `RoleGapAnalyzer` into the manifest builder while preserving the Phase 2.2 floor-of-1 logic) and delete only the dead `DeterministicContextEngine.enforceRoleDiversity` method.



### 2. Checkpoint 1 Trap: `GreedyRehydratorTest` Deletion & Dead `calculateFashionistaScore` (Phase 1.2.5)

* **The Trap:** Phase 1.2 item 5 changes `GreedyRehydrator.mapToVisualBlueprintData` to require the pre-computed `FashionistaScore` parameter, which immediately breaks `GreedyRehydratorTest` at **Checkpoint 1**. Because `GreedyRehydrator` tests aren't mentioned until Phase 4.3 item 3, the agent previously passed Checkpoint 1 by deleting `GreedyRehydratorTest`. Additionally, removing the call to `ColorHarmonyEngine.calculateFashionistaScore` leaves the method itself dead in `ColorHarmonyEngine.kt`.


* **The Fix in 1.2.5:** Explicitly instruct the agent to delete `ColorHarmonyEngine.calculateFashionistaScore` from `ColorHarmonyEngine.kt` and update `GreedyRehydratorTest` in place during Phase 1.2.5 (never deleting the test file).

### 3. Checkpoint 1 Trap: UI Consumers of `FashionistaScore.breakdown` (Phase 1.2.4) & Wrapper Text

* **The Trap:** Phase 1.2 item 4 replaces the 6-field `FashionistaFeatureVector` on `FashionistaScore.breakdown` with a 3-field snapshot. Any Compose UI component (such as the radar chart or score breakdown view) referencing the old 6-feature properties will fail to compile at Checkpoint 1 unless explicitly included in 1.2.4.


* **Stray Meta-Text:** Lines 1–2 (`Copy and paste the prompt block below...`) and the final paragraph (`If you want to run all four phases...`) are conversational chat wrappers that should be stripped from the markdown file.



---

### Exact Replacements for `Tasks_2.md`

Replace the corresponding bullets in **1.2 (items 4 & 5)** and **4.3 (item 1)** with these blocks, and delete the first and last conversational paragraphs of the file:


4. **Reshape `FashionistaScore`:**
   - Remove the fake all-`0.85` default `breakdown` vector and the 6-feature `FashionistaFeatureVector` breakdown type. Replace it with a 3-field breakdown snapshot populated from the evaluated sub-scores (`colorHarmonyScore/100`, `silhouetteScore/100`, `contrastScore/100`, each availability `1.0`). Unify on single constants `standardId="FASHIONISTA"`, `standardVersion="v1"` (deleting the `"v1.1"` default). Update any UI/Compose radar or breakdown call sites referencing `FashionistaScore.breakdown` to read the 3-axis snapshot so Phase 1 compiles cleanly.
5. **Turn `GreedyRehydrator` into a Pure ID-to-UI Mapper:**
   - Delete all 5 legacy fallback/rescoring steps from `GreedyRehydrator.kt`:
     1. Keyword recategorization (`applyClothingKeywordFallbacks` at line ~75, eliminating `"top" ⊂ "stop"`).
     2. Anchor fail-safe append + `distinctBy { it.category }` (line ~44).
     3. Inventory-wide slot backfill (`assignToSlots` `inventory.find` fallbacks).
     4. Name-based cosmetic fallbacks (lines ~129–136, eliminating `"lip" ⊂ "eclipse"`).
     5. `ColorHarmonyEngine.calculateFashionistaScore` rescoring — delete `calculateFashionistaScore` from `ColorHarmonyEngine.kt`, change `mapToVisualBlueprintData` to accept the pre-computed `FashionistaScore` as a parameter (no internal scoring), update its callers (`StyleSimulatorViewModel`, `BlueprintDetailContent`) to pass the result-channel score through to `koColorScore`, and update `GreedyRehydratorTest` in place to match the new signature (do NOT delete `GreedyRehydratorTest`).



And in **Phase 4.3 item 1**:


1. **Preflight Gate & Role Diversity:**
   - Port the preflight slot-completeness gate concept from `DeterministicCatalogEngine` onto `StyleSimulatorEngine`'s `ClothingItem` pool (aborting before AI spend if required slots cannot be filled), then delete the unused `DeterministicCatalogEngine` class.
   - Wire `selectionState.missingRoleRequirements` (from `RoleGapAnalyzer`) into `adaptContextToProvider`'s `categorizedPool`/`slotsPerCategory` partitioning (preserving the Phase 2.2 floor-of-1 step-down logic) so candidate slots only allocate budget to unfilled roles, and delete the dead `DeterministicContextEngine.enforceRoleDiversity` method.
   - Delete dead methods/fields: `WardrobeCandidateFilter.getCandidates`, `WardrobeCandidateFilter.getCosmeticCandidates`, `FirebaseAiClient.getStyleAdvice`, `FirebaseAiClient.estimateTokens`, and unused `rotationScoringUseCase`/`repository` constructor injections in `DeterministicContextEngine`.

