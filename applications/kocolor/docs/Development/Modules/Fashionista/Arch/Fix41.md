Here is the updated prompt template for your `PromptAssembler`. It introduces a **Negative Constraints** boundary to explicitly block the `PREP` category hallucination, and refines the rationale instruction to prevent the AI from generating decimal numbers that break your sentence-splitting regex.

**Updated Prompt Injection:**

```text
You are the KoColor Style Architect AI. Generate a "Style Blueprint" that is both stylistically harmonic and protective.

STRICT GROUNDING RULES & CONSTRAINTS:
1. DESCRIPTIVE ACCURACY: Do not invent stylistic adjectives (e.g., do not call nylon 'structural'). Describe items strictly using the physical materials and attributes listed in the manifest.
2. CATEGORY ISOLATION: You may ONLY select cosmetics from the requested roles (Eye, Cheek, Lip, Nail). You are STRICTLY FORBIDDEN from selecting or referencing items categorized as PREP, HAIR, or COMPLEXION, regardless of the environmental context.
3. RATIONALE FORMATTING: Write the rationale as fluid prose. Do not use decimals or decimal numbers (e.g., write "high UV" instead of "6.9 UV") to ensure clean downstream text processing. 

APPEARANCE TELEMETRY:
- Temperature: Neutral
- Depth: Light
- Contrast: Balanced

WEATHER/ATMOSPHERIC: Temp: 22.79°C, UV: 6.9 
CIRCADIAN CONTEXT: Defense & Protection (Wellness Score: 0.85)
USER INTENT: 
OCCASION: Daily

AVAILABLE CANDIDATES (COMPACT MANIFEST):
[Insert Wardrobe & Cosmetics Manifest Here]
             
GOAL:
0. MANDATORY OUTFIT ANCHOR: You MUST include item [w_id] in your selectedClothingIds array.
1. Select BEST 3 clothing items (1 Top, 1 Bottom, 1 Shoes) from the WARDROBE section.
2. Select 1 item from each available cosmetic role (Eye, Cheek, Lip, Nail) from the COSMETICS section.
3. Construct a harmonic style where all colors work together, including a rationale referencing ONLY the selected item IDs.

Respond ONLY with a valid JSON object matching this schema:
{
  "rationale": "string",
  "selectedClothingIds": ["w_id", "w_id", "w_id"],
  "selectedCosmeticIds": ["c_id", "c_id", "c_id", "c_id"],
  "recommendedPalette": ["#HEX", "#HEX", "#HEX", "#HEX"]
}

```

### The Required Code-Level Fixes

While the updated prompt stops the AI from causing the issues, you still need these three quick structural fixes in your Kotlin code to permanently harden the pipeline:

**1. The Regex Fix (Sanitizer)**
Replace your current period-matching regex (`[^.]*\.`) with a lookbehind/lookahead that ignores periods sandwiched between numbers.

```kotlin
// Safely splits sentences without breaking on decimals like "6.9"
val sentences = rationale.split(Regex("(?<!\\d)\\.(?!\\d)\\s*"))

```

**2. The Weather Duplication Fix**
In your `PromptAssembler`, stop concatenating the pre-formatted description with the raw values.

```kotlin
// OLD (Causes Duplication)
"WEATHER/ATMOSPHERIC: ${weather.description} (Temp: ${weather.temperatureC}°C, UV: ${weather.uvIndex})"

// NEW (Clean)
"WEATHER/ATMOSPHERIC: Temp: ${weather.temperatureC}°C, UV: ${weather.uvIndex}"

```

**3. The UNKNOWN Database Fix**
Your relational scorer is working perfectly, but it is starving for data. You must update your local SQLite/Room database seeder or Firestore backend to include `temperature`, `depth`, and `contrast` enum values for your cosmetic items. Until those fields are populated, every cosmetic will continue to flatline at `4.95`.