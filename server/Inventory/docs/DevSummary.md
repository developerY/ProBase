# KoColor Launch, Architecture & Catalog Tagging Master Summary

## 1. Google Play Release & Production Security Checklist

### Play Console Deployment (`v0.1`)

* **German (`de`) Translation Error Resolved:** A transient server failure occurred in Google Play Console's automatic string translation engine during `.aab` ingestion (`Translation failed for these languages: de`). Re-uploading `mobile-release.aab` cleared the timeout without needing to disable automatic translation on the **Grow users > Translations > App strings** page.



### Firebase App Check & Play Integrity (`403` Fix)

* **Root Cause:** Local IDE builds succeed because the Debug/Upload keystore SHA-256 is registered in Firebase, whereas Play Store downloads are re-signed by Google Play App Signing. This caused `IntegrityService` tokens to fail verification (`403: App attestation failed`), triggering a dummy token fallback (`ServerException: Firebase App Check token is invalid`).
* **Resolution Steps:**
1. Copy the **App signing key certificate (SHA-256)** from **Google Play Console > Test and release > Setup > App integrity > App signing**.
2. Add that SHA-256 fingerprint to **Firebase Console (Project `20197827893`) > Project settings > General > Your apps (`com.zoewave.probase.kocolor`)**.
3. Confirm **Play Integrity** is linked under **Firebase Console > Build > App Check > Apps** and enabled in the Google Cloud Console.
4. Clear local app storage on test devices to reset the `l40: Too many attempts` client rate-limit lockout.



### Logcat Telemetry & Key Audit

* **Firebase AI Keys:** Verified that no Firebase AI or Gemini API keys are leaked in logcat during cold start or manifest verification.
* **OpenWeatherMap API Key:** Identified a plaintext OpenWeatherMap key (`appid=ea705...`) logged in HTTP `GET` traces; restrict HTTP logging to `NONE` in release builds or proxy weather requests.

---

## 2. Promotional Video, Audio Synthesis & B2B Outreach

### Video Walkthrough & YouTube Studio Configuration

* **Full Walkthrough (`W7gTx8q0e7A`):** Generated a 28-block timed SubRip (`.srt`) caption track and a continuous narrative voiceover script for Google AI Studio (**Gemini 3.8 Flash-Lite TTS** / **Gemini 3.8 Flash TTS**).
* **YouTube Studio Behavior:**
* The **Automatic dubbing** banner (`You are eligible for early access to automatic dubbing`) is a channel-level translation feature that requires existing spoken audio and will not synthesize primary speech from an `.srt` file.


* On the **Video subtitles** screen, the manual `English (video language)` track published on Oct 4, 2026 remains active while the `Processing... Automatic` track scans the silent audio stream before timing out.




* **YouTube Shorts Iteration (`DKuv7__uTEI` $\rightarrow$ `sZtUNOY_MVk`):**
* Validated the 3-tab bottom navigation (`Home`, `Collection`, `Settings`), starter pack imports (173 Vanity items + 54 Wardrobe items = 227 items / $10,772 portfolio), and Paris Travel Packing AI.
* For future Shorts: boost voiceover gain by $+3\text{ to }+6\text{ dB}$, add burned-in dynamic captions for muted viewers, and open with the 5-second AI result hook before showing the import flow.


* **Automated `.srt`-to-Audio CLI Tooling:** Architected both **Rust** (`tokio` + `reqwest` + `ffmpeg`) and **Mojo** (`PythonObject` interop + `ffmpeg`) pipelines using `adelay` and `amix=normalize=0` to place synthesized TTS clips at exact millisecond timestamps.

### Strategic Partnership Outreach (MAC Cosmetics)

* Prepared executive and simplified B2B outreach templates positioning KoColor's **The Vanity** and deterministic gap-detection engine as a high-intent, contextual commerce channel for MAC Cosmetics.

---

## 3. KoColor Catalog Tagging Pipeline (`metadataVersion: 1`)

### Architectural Goals

1. Eliminate the **Zero-Item Manifest Bug** (`Initial Wardrobe: 0 items`), where an empty candidate list caused Gemini to hallucinate a generic rationale and the FASHIONISTA calibration engine to award an `85.4/100` score to an empty outfit.
2. Separate **Deterministic Hard Filters** (binary eligibility) from **Soft Preference Scoring** (mathematical ranking) before invoking the LLM.
3. Track **Field-Level Provenance** so unverified AI guesses (`LLM_SUGGESTED`) never trigger hard environmental bans.

### 7-Phase Server Pipeline (`server/Inventory/package/tag_pipeline.py`)

| Phase | Component | Technical Implementation |
| --- | --- | --- |
| **Phase 1** | **Discovery & Pairing** | Recursively scans `server/Inventory/package/input/KoColor/` and pairs `kc-[type]-[id].json` with `kc-[type]-[id].notes.json`. |
| **Phase 2** | **Deterministic Color Math** | Converts `#RRGGBB` $\rightarrow$ linear sRGB $\rightarrow$ XYZ (D65, $2^\circ$) $\rightarrow$ CIELAB ($L^*, a^*, b^*$) $\rightarrow$ LCh ($C^*, h^\circ$) and derives `hueFamily` and `colorTemperature` (`RULE_DERIVED`). |
| **Phase 3** | **Gemini Structured Extraction** | Uses `google-genai` with Pydantic `response_schema` (`temperature = 0.0`) to classify `ProductType`, `OccasionTag`, `FormalityLevel`, `ConstructionFeature`, and `ChromaticBehavior`. |
| **Phase 4** | **Deterministic Rule Promotion** | Maps `ProductType` to an anatomical `GarmentSlot` (`TOP`, `BOTTOM`, `OUTERWEAR`, `ONE_PIECE`, `FOOTWEAR`, `ACCESSORY`) and promotes verified construction/occasion rules to `RULE_DERIVED`. |
| **Phase 5** | **Quality Gate & Quarantine** | Verifies every record has valid IDs, color telemetry, and trusted hard-filter facets. Writes `quality_report.json` and routes invalid items to `quarantine/`. |
| **Phase 6** | **Serialization & Compression** | Sorts records by `itemId`, formats canonical JSON (RFC 8785), and compresses via `zstd -19` (omitting `--ultra` to keep Android `libzstd-jni` RAM windows $\le 64\text{ KB}$). |
| **Phase 7** | **Hashing & Ed25519 Signing** | Computes `sha256` on compressed `.kpkg` bytes, signs each pack with `atelier-dev-01` (`ed25519`), and signs the outer `manifest.json` envelope. |

---

## 4. Controlled Taxonomy & Schema Reference

### Core Taxonomy Dimensions

* **`DomainType`**: `WARDROBE` | `COSMETICS`
* **`ProvenanceType`**: `SOURCE_VERIFIED` | `RULE_DERIVED` | `HUMAN_REVIEWED` *(Trusted for Hard Filters)* · `LLM_SUGGESTED` | `UNKNOWN` *(Soft Ranking Only)*
* **`GarmentSlot` (Anatomical Role)**: `TOP` | `BOTTOM` | `OUTERWEAR` | `ONE_PIECE` | `FOOTWEAR` | `ACCESSORY`
* **`CatalogCategory` (Merchandising)**: `TOPS` | `BOTTOMS` | `OUTERWEAR` | `ACTIVEWEAR` | `FOOTWEAR` | `ONE_PIECE` | `ACCESSORIES`
* **`OccasionTag`**: `CASUAL` | `EVERYDAY` | `WORK` | `EVENING` | `TRAVEL` | `FORMAL_EVENT` | `ACTIVE`
* **`FormalityLevel`**: `CASUAL` | `SMART_CASUAL` | `BUSINESS` | `FORMAL` | `GALA`
* **`ThermalBand`**: `HOT` ($>26^\circ\text{C}$) | `WARM` ($20\text{–}26^\circ\text{C}$) | `MILD` ($14\text{–}20^\circ\text{C}$) | `COOL` ($7\text{–}14^\circ\text{C}$) | `COLD` ($<7^\circ\text{C}$)
* **`ChromaticBehavior`**: `OPAQUE` | `SHEER` *(Included in Color Harmony)* · `TRANSLUCENT` | `NON_CHROMATIC` *(Excluded from Color Harmony)*

---

## 5. Android Runtime Enforcement (`com.zoewave.probase.kocolor`)

1. **Provenance Policy (`ProvenanceType.isTrustedForHardFilter`):**
* Hard filters (weather bans, thermal suitability, required occasion matching) evaluate **only** attributes marked `SOURCE_VERIFIED`, `RULE_DERIVED`, or `HUMAN_REVIEWED`.
* Attributes marked `LLM_SUGGESTED` are evaluated exclusively in `scoreWardrobeSoftPreferences()`.


2. **Preflight Candidate Gate (`EnginePreflightResult.AbortedMissingSlots`):**
* Before calling Firebase AI / Gemini, `DeterministicCatalogEngine` verifies that every required `GarmentSlot` (`TOP`, `BOTTOM`, `FOOTWEAR`) contains at least 1 eligible item. If any slot is empty, execution halts immediately without spending tokens or computing a fallback score.


3. **Cosmetic Category & Chromatic Isolation:**
* Strictly filters cosmetics to `EYE`, `CHEEK`, `LIP`, and `NAIL` with `chromaticBehavior` in `["OPAQUE", "SHEER"]`, preventing transparent primers, beard oils, or cleansers (`PREP`, `HAIR`, `COMPLEXION`) from entering the Gemini manifest or distorting CIELAB harmony math.


4. **Grounded Manifest Serialization & Post-AI Validation:**
* Serializes verified `material.composition` and `construction.features` into the prompt so Gemini obeys Strict Grounding Rule #1.
* Runs `BlueprintValidator.validateAiSelection()` on Gemini's response prior to `[5] AESTHETIC CALIBRATION (FASHIONISTA)` to reject hallucinated IDs or incomplete outfits.