# Implementation Plan: Hardened Style Simulator (3-Tier Engine)

Upgrade the Style Simulator to a three-tier intelligence model with manifest pre-filtering and biological skin-anchoring.

## Proposed Changes

### 1. The 3-Tier Intelligence Strategy (`StyleSimulatorEngine.kt`)
- **Tier 1 (Cloud)**: Gemini 1.5 Flash (Existing).
- **[NEW] Tier 1.5 (Local LLM)**: Implement Gemini Nano via `LocalAiEngine`. This will handle the Manifest-to-Outfit reasoning on flagship NPUs.
- **Tier 2 (Heuristics)**: Best-effort keyword matching (Existing).

### 2. Manifest Pre-Filtering (`WardrobeRepository.kt`)
- Implement a `getShortlistByIntent(intent: String)` query.
- Use a "Vibe Map" to translate intent keywords into `Formality` thresholds.
- Exclude categorical mismatches (e.g., Activewear for "Negotiation") before serialization.

### 3. Biological Skin Anchoring (`StyleSimulatorViewModel.kt`)
- Query the `FashionProfile` (Undertone, Seasonal Type) from RoomDB.
- Inject the profile into the "Biological Anchor" section of the prompt.
- **Goal**: Ensure the "Brick Red" makeup suggestion is mathematically derived from the user's cool/warm undertones.

### 4. Code Resilience
- Implement a `RequiresCloudException` for Nano-to-Cloud handoff if the manifest exceeds Nano's local context window.

## Verification Plan

### Automated Tests
- `StyleSimulatorEngineTest`: Verify that Tier 1.5 (Nano) is attempted before falling back to Tier 2.
- `VibeFilterTest`: Ensure "Negotiation" correctly filters out "Joggers".

### Manual Verification
- Run simulation on Pixel 9 Pro (Nano path) vs Pixel 9a (Cloud path).
- Verify that the "Stylistic Rationale" explicitly mentions skin undertones (e.g., "To complement your Cool Summer profile...").
