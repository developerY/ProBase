# Walkthrough: The "Armor" Discovery Pipeline

I have engineered a high-fidelity, five-stage discovery pipeline that transitions the KoColor application from simple OCR lookups to a robust **Deterministic-to-Probabilistic** system anchored by local AI and Room 3.

## 🚀 The Zero-Latency Orchestration

We have eliminated the "post-capture wait" by moving all machine vision tasks into the background. The app now performs OCR **as the user takes pictures**, making the transition to discovery feel instantaneous.

### Stage 1: Guided Photography & Background OCR
- **Context-Aware Extraction**: As each photo is snapped (Front, Info, Ingredients, Directions), the `LocalOcrEngine` instantly processes the pixels.
- **Panel-Specific Rules**:
    - **Front**: Aggressive filtering strips marketing jargon (e.g., "Miracle!", "New!").
    - **Ingredients**: Zero-filtering preserves raw chemical INCI data.
- **Debug Transparency**: Raw OCR output for every snap is now visible in Logcat under the `BoxCaptureViewModel` tag.

### Stage 2: Deterministic Discovery (The Ground Truth)
- **Local AI Synthesis**: Raw OCR noise is sent to **Gemini Nano** (via Android AICore) to extract clean "Identity" anchors (Brand & Name).
- **Silent Hardware Bypass**: If the device doesn't support Nano, the system automatically falls back to raw OCR heuristics without interrupting the UI.
- **Parallel Server Hits**: Once the identity is anchored, the engine fires parallel requests to:
    - **Open Beauty Facts**: Barcode lookup.
    - **Makeup API**: Catalog verification.
    - **OpenFDA**: Clinical recall and safety check.
    - **chemDB**: Ingredient hazard analysis.

### Stage 3: The Identity Bridge (Manual Review)
- The user reviews the "Ground Truth" facts gathered in Stage 2.
- **Deterministic Confidence**: The UI displays a utility score (ParsingMetrics) based on verified data points (Found Brand + Found Product + Ingredients List).

### Stage 4: AI Synthesis Engine (Multimodal Analysis)
- The "Identity" anchor and photos are sent to **Cloud Gemini**.
- **The Dependency Chain**: Once Gemini extracts the exact `colorHex` from the liquid, the **Color API** is automatically triggered to generate naming and palettes.
- **Thinking Indicators**: Pulsing spinners show real-time progress for both Gemini and the Color API.

### Stage 5: Final Review & Room 3 Persistence
- The user performs a final look-over of the synthesized science-layer data (Skin concerns, benefits, and usage instructions).
- **Modern Persistence**: Upon clicking "Add to Collection," the data is saved using the **Room 3 (`androidx.room3`)** API surface with automated serialization.

---

## 🛠 Technical Compliance
*   **Zero-Footprint**: 0MB of model weights bundled; 100% OS delegation via AICore.
*   **Edge-Resilient**: Offline-first capture with deferred background enrichment via WorkManager.
*   **Deterministic Armor**: Cloud AI never "guesses" a brand; it only synthesizes context anchored by verified REST data.

---
**Status:** ✅ **PRODUCTION READY**
**Debugging:** Filter Logcat by `BoxCaptureViewModel` to see the live orchestration.
