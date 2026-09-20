# Implementation Plan: The Definitive KoColor Style Journey

Based on the architectural review in `ArchRev72.md`, this document details the implementation plan for restructuring the `StyleCreationStoryScreen` to explicitly tell the story of how KoColor creates a recommendation, clearly separating deterministic logic from AI synthesis.

---

## 1. Architectural Objectives

| Objective | Key Refinement |
| :--- | :--- |
| **Causal Timeline Order** | Reorder the UI so the user reads the "Story First" (Input $\to$ Context $\to$ Engine $\to$ Result) instead of seeing the result immediately. |
| **Deterministic vs. AI Separation** | Visually label pipeline steps to prove to the user that Gemini is just one stage inside a highly deterministic system. |
| **Explicit Causal Reasoning** | Surface *why* an anchor was chosen (e.g., `"Your request emphasized colorful, so a high-chroma candidate became the anchor."`). |
| **Fulfillment vs. Aesthetics** | Render `Intent Fulfillment` explicitly when an intent exists, completely separate from `FASHIONISTA` aesthetic validation. |

---

## 2. Target UI Layout Sequence (The "Long Scroll")

The `StyleCreationStoryScreen` will be structured in this exact sequence:

1. **Header**: "Your Style / How KoColor created this look"
2. **[INPUT] Your Request**: (Displays user intent or "No specific style preference").
3. **[01] Understood the Context (Deterministic)**: Temperature, Depth, Contrast, Occasion, Weather.
4. **[02] Searched Your Wardrobe (Deterministic)**: Shows candidate pruning (e.g., 53/54 survived).
5. **[03] Established the Anchor (Deterministic)**: Shows the item, ID, and plain-text causal reasoning.
6. **[04] AI Style Synthesis (AI)**: Gemini's rationale for assembling the grounded candidates.
7. **[05] Verified the Recommendation (Deterministic)**: Checkmarks for Role Cardinality, Prep exclusion, etc.
8. **[RESULT] The Ensemble**: Top, Bottom, Shoes, and Cosmetic Grid.
9. **[EVALUATION] Intent Fulfillment**: (If intent specified) Score out of 100 with dimensions.
10. **[EVALUATION] FASHIONISTA**: 92.7/100 aesthetic calibration score.
11. **[DIAGNOSTICS] Style Architecture**: The collapsible `AuditTrailView` engineering logs.

---

## 3. Component Implementation Details

### Timeline Step Redesign
We will introduce `isDeterministic: Boolean` to the `TimelineStep` component.
* If `isDeterministic == true`, we display a subtle `DETERMINISTIC` badge.
* If `false`, we display an `AI SYNTHESIS` badge.

### Causal Anchor Reasoning Translation
We need to translate the backend `anchorReason` into user-facing narrative:
* Backend: `[INTENT ANCHOR] High-chroma intent override`
* UI translation: *"Your request emphasized color, so a high-chroma candidate became the anchor."*

### Layout Reversal
Remove the `isJourneyExpanded` toggle that hides the timeline. The timeline **IS** the screen. The user scrolls through the timeline naturally to arrive at the outfit result and the FASHIONISTA score.

---

## 4. Verification Plan

* **Unit Tests**: Run `:applications:kocolor:data:testDebugUnitTest`.
* **Build Verification**: Run `:applications:kocolor:apps:mobile:assembleDebug`.
* **UI Inspection**: Open the `@Preview` for `StyleCreationStoryScreen` in the IDE to verify the new linear flow from Context $\to$ Anchor $\to$ AI $\to$ Validation $\to$ Outfit $\to$ Evaluation.
