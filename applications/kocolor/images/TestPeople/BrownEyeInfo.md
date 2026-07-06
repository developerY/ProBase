### 1. Visual Spectrum Extraction

| Feature | Target HEX Value | Color Characteristics |
| --- | --- | --- |
| **Skin Highlight** | `#E2D7D4` | Cool, translucent ivory with a slight pinkish/blue reflection |
| **Skin Mid-tone** | `#D1BBAE` | Muted cool-beige with a subtle olive (green-grey) anchor |
| **Skin Shadow** | `#A38C82` | Cool taupe shadow, completely lacking warm orange or golden depth |
| **Hair Dominant** | `#1A181C` | Deep ash-black with a cool, almost blue-ish reflection |
| **Eye Iris** | `#3B2F2F` | Deep espresso/black-brown with cool depth |

### 2. Derived Biological Profile

Based on the high contrast between her deep, cool-toned hair and her cool-olive skin, her deterministic fashion classification is:

* **Undertone:** Cool Olive (often misdiagnosed as warm).
* **Seasonal Type:** True Winter (Deep, clear, cool coloring).
* **Contrast Level:** High.

---

### 3. Production Testing Payloads

#### Room Database Mock Entity

Use this to seed your `FashionProfileDao` before the simulation run:

```kotlin
val testFashionProfileAsian = FashionProfile(
    userId = "test_user_asian_young_lady",
    undertone = "COOL_OLIVE",
    seasonalType = "TRUE_WINTER",
    skinHex = "#D1BBAE",
    hairHex = "#1A181C",
    eyeHex = "#3B2F2F",
    contrastRatio = 0.85f, // High contrast
    lastUpdated = 1783287500000L
)

```

#### Multimodal JSON Mock Context

Inject this into the Tier 1 Cloud prompt:

```json
{
  "biological_anchor": {
    "classification": "True Winter",
    "undertone": "Cool Olive",
    "dominant_chroma": "Clear and High Contrast",
    "hex_map": {
      "skin_base": "#D1BBAE",
      "hair_dominant": "#1A181C",
      "eye_iris": "#3B2F2F"
    }
  },
  "environmental_context": {
    "lighting_condition": "Studio Ring Light (5600K)",
    "white_balance_anchor": "Pure white background"
  }
}

```

### Expected AI Synthesis Output

This test will immediately expose if your Tier 1 Cloud AI is hallucinating or falling back on generic stereotypes.

* **Pass Criteria (Valid Harmonies):** The AI must recommend highly saturated, cool jewel tones like sapphire blue, true red, vivid emerald, royal blue, or pure black and white. For makeup, it should suggest blue-based reds, cool mauve, or raspberry lip stains.
* **Fail Criteria (Hallucination):** If the engine outputs recommendations for mustard yellow, terracotta, burnt orange, or warm brown, the AI has failed the undertone test and wrongly assumed a warm profile based on ethnicity.
