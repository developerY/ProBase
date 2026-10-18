This architecture is production-grade. Separating deterministic math (Phase 2) from semantic extraction (Phase 3), wrapping individual attributes in provenance metadata, and enforcing RFC 8785 canonical serialization before `ed25519` signing turns the catalog into a verifiable, auditable data pipeline.

Before you write `tag_pipeline.py`, there are **five architectural edge cases** in this spec where the Python compiler and the Android `DeterministicWardrobeEngine` could collide at runtime. Locking these down now will save you a schema migration later.

---

### 1. The "Merchandising Category" vs. "Outfit Slot" Collision

In `WardrobeMetadata` for `kc-active-805`, you separated `"catalogCategory": "ACTIVEWEAR"` from `"layeringRole": "OUTER_LAYER"`. That is great for store browsing, but look at Section 4.2 (**Preflight Candidate Checks**) and your Gemini Prompt Rule #2:

> *"You must only select clothing items from the `TOPS`, `BOTTOMS`, `SHOES`, or `OUTERWEAR` categories."*

If `kc-active-805` (Track Jacket), a pair of track pants, and a sports tank all have `"catalogCategory": "ACTIVEWEAR"`, what field tells the Android preflight check which required **anatomical slot** (`TOP`, `BOTTOM`, `OUTERWEAR`, `SHOES`) the item satisfies?

* `layeringRole` alone cannot solve this, because both a base-layer thermal shirt and base-layer leggings share `BASE_LAYER`.
* **Fix:** Add a deterministic `anatomicalSlot` (or `garmentSlot`: `TOP`, `BOTTOM`, `OUTERWEAR`, `ONE_PIECE`, `FOOTWEAR`, `ACCESSORY`) derived strictly by a lookup table from `productType` in Phase 4.

### 2. The Provenance Paradox in Hard Filtering

Compare **Section 3** (`WardrobeMetadata`) with **Section 4.1** (`Provenance Policy`):

* In Section 4.1, hard filters may *only* rely on `SOURCE_VERIFIED`, `HUMAN_REVIEWED`, or `RULE_DERIVED` data; `LLM_SUGGESTED` is restricted to soft ranking.
* However, in Section 3, `thermalSuitability` is marked `RULE_DERIVED` from `GARMENT_CONSTRUCTION`. What inputs drive that rule? `material.composition` + `productType` (`TRACK_JACKET`).
* **The Trap:** If Gemini extracts `productType` or `material` from unstructured prose in Phase 3, those inputs are technically `LLM_SUGGESTED`. A deterministic rule applied to an unverified LLM input is only as trustworthy as the input. Furthermore, `occasionTags` in your example is marked `LLM_SUGGESTED`—which means under Rule 4.1, the Android engine could *never* run a hard filter on Occasion!
* **Fix:**
1. Parse `material.composition` and `catalogCategory` deterministically in Phase 1/2 via regex/structured fields from `kc-[type]-[id].json` whenever possible so their provenance is `SOURCE_VERIFIED`.
2. In Phase 4, run a deterministic **Rule Promotion Pass**: if `productType` (`TRACK_JACKET`) and `catalogCategory` (`ACTIVEWEAR`) match your deterministic lookup table, promote `occasionTags: ["ACTIVE", "CASUAL"]` and `layeringRole: "OUTER_LAYER"` from `LLM_SUGGESTED` to `RULE_DERIVED`.



### 3. The Translucent / Non-Chromatic Cosmetic Edge Case

In your second JSON example (`kc-primer-901`, *"Optical Blur Pore Primer"*, Shade: `"Translucent Cloud"`), Phase 2's CIELAB pipeline will run on whatever placeholder HEX code is stored in `kc-primer-901.json` (often `#FFFFFF` or `#F2F0EB`).

* **The Trap:** If the deterministic color engine assigns `lStar: 95.0, hueFamily: NEUTRAL` to a translucent primer, a clear lip balm, or a beard oil (`kocolor-grooming-vault`), the downstream color harmony scorer will mathematically evaluate a transparent skincare product as if it were an opaque white paint pigment on the user's face.
* **Fix:** Add a `chromaticBehavior` field to `CommonMetadata.color`:
* `"OPAQUE"` | `"SHEER"` | `"TRANSLUCENT"` | `"NON_CHROMATIC"`
* Only items with `OPAQUE` or `SHEER` should participate in CIELAB mathematical color harmony scoring and Gemini's cosmetic role selection (`EYE`, `CHEEK`, `LIP`, `NAIL`).



### 4. `kotlinx.serialization` Polymorphism on Android

In Section 3, your JSON uses `"domain": "WARDROBE"` alongside PascalCase keys (`"CommonMetadata"`, `"WardrobeMetadata"`).

* In Kotlin's `kotlinx.serialization`, deserializing polymorphic JSON works best when `"domain"` acts as the `@JsonClassDiscriminator("domain")` on a `sealed interface CatalogRecord`, or when the sub-objects use standard `camelCase` (`commonMetadata`, `wardrobeMetadata`).
* **Fix:** Standardize on `camelCase` for all JSON keys (`commonMetadata`, `wardrobeMetadata`, `cosmeticsMetadata`) so your Python Pydantic models and Kotlin `@Serializable` data classes map 1:1 without needing `@SerialName` annotations on every field.

### 5. `zstd -19 --ultra` Memory Overhead on Mobile

In Phase 6, you specify `zstd -19 --ultra`.

* Standard `zstd` levels range from `1` to `19`. The `--ultra` flag unlocks levels `20` to `22`, which can increase the decompression window size up to $128\text{ MB}$ of native heap per stream.
* Since your `.kpkg` uncompressed payloads are under $25\text{ KB}$ (`kocolor-lips-vault` is $19,489\text{ bytes}$), any window size larger than $32\text{ KB}$ provides **zero** additional compression ratio. Using `zstd -19` (without `--ultra`) produces the exact same compressed byte size while keeping the Android `libzstd-jni` decompression footprint under $64\text{ KB}$ of RAM.

---

### Refined Schema Snippet (Incorporating Fixes 1–4)

```json
{
  "domain": "WARDROBE",
  "commonMetadata": {
    "itemId": "kc-active-805",
    "name": "Arcadia Blue Track Jacket",
    "source": "INTERNAL",
    "commercial": {
      "priceUsd": 85.0,
      "volumeOrSize": "Size M",
      "sustainabilityGrade": "GRADE_A"
    },
    "color": {
      "hex": "#252C58",
      "colorName": "Arcadia Blue",
      "chromaticBehavior": "OPAQUE",
      "hueFamily": {
        "value": "BLUE",
        "provenance": "RULE_DERIVED"
      },
      "colorTemperature": {
        "value": "COOL",
        "provenance": "RULE_DERIVED"
      },
      "lStar": 19.64,
      "aStar": 11.55,
      "bStar": -27.62,
      "cStar": 29.94,
      "hDeg": 292.7,
      "colorAlgorithmVersion": "1.0.0",
      "colorClassificationRuleVersion": "1.0.0"
    },
    "material": {
      "composition": [
        {
          "material": "TRICOT_POLYESTER",
          "percentage": 100
        }
      ],
      "provenance": "SOURCE_VERIFIED"
    },
    "metadataVersion": 1,
    "ruleVersion": "1.0.0"
  },
  "wardrobeMetadata": {
    "catalogCategory": "ACTIVEWEAR",
    "productType": {
      "value": "TRACK_JACKET",
      "provenance": "SOURCE_VERIFIED"
    },
    "garmentSlot": {
      "value": "OUTERWEAR",
      "provenance": "RULE_DERIVED"
    },
    "occasionTags": [
      { "value": "ACTIVE", "provenance": "RULE_DERIVED" },
      { "value": "CASUAL", "provenance": "RULE_DERIVED" },
      { "value": "EVERYDAY", "provenance": "LLM_SUGGESTED" },
      { "value": "TRAVEL", "provenance": "LLM_SUGGESTED" }
    ],
    "formality": {
      "value": "CASUAL",
      "provenance": "RULE_DERIVED"
    }
  }
}

```

*(Note how tagging each item inside `occasionTags` with its own provenance allows `ACTIVE` and `CASUAL` to be `RULE_DERIVED`—unlocking Hard Filtering—while keeping `EVERYDAY` and `TRAVEL` as `LLM_SUGGESTED` for Soft Ranking!)*

Would you like me to generate the complete `tag_pipeline.py` script (including the D65 CIELAB math, Pydantic schemas for `google-genai`, and the deterministic rule-promotion pass) so you can run it against `server/Inventory/package/input/KoColor/`?