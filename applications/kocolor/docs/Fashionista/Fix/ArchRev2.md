This is **substantially better**. The architecture now reads like a real hardening/refactoring document rather than a description of incremental fixes.

The biggest improvement is that it clearly preserves the boundary:

> **FASHIONISTA = “How good is this?”**
> **Recommendation Engine = “What should I wear?”**

And the five refactoring areas address the actual problems seen in the Gemini log.

### What I would change before calling it final

#### 1. Task 1: Don't use `contains()` on the compact manifest

This is the biggest technical weakness in the implementation example.

```kotlin
compactManifest.contains("SHOES", ignoreCase = true)
```

and:

```kotlin
compactManifest.contains("DIMENSION", ignoreCase = true)
```

are **textual heuristics**, not structural validation.

A manifest containing `"notes": "shoe-inspired..."` could theoretically trigger the check. More importantly, you're making the prompt-generation logic dependent on a serialized representation.

Better:

```kotlin
val availableClothingCategories =
    clothingCandidates.map { it.category }.toSet()

val availableCosmeticCategories =
    cosmeticCandidates.map { it.category }.toSet()
```

Then:

```kotlin
val hasShoes = "SHOES" in availableClothingCategories
```

That makes the grounding genuinely deterministic.

**Principle:**

> Build the prompt from typed candidate data, not from serialized prompt text.

---

### 2. The cosmetic-category mapping needs to be explicit

This:

```kotlin
if (cosmeticCategories.isNotEmpty()) {
    "Select 1 item from each available cosmetic category..."
}
```

is better than the old hardcoded Cheek requirement, but there is still a subtle problem.

Your domain category is:

```text
DIMENSION
```

while your prompt calls it:

```text
Cheek
```

That mapping needs to be an explicit domain rule rather than:

```kotlin
DIMENSION || CHEEK -> Cheek
```

I'd define something like:

```kotlin
CosmeticRole.EYE
CosmeticRole.CHEEK
CosmeticRole.LIP
CosmeticRole.NAIL
```

and have candidate categories map deterministically to those roles.

Then the prompt is generated from **roles**, not arbitrary strings.

---

### 3. The validator should do more than remove hallucinated IDs

This is important.

Your current validator says:

> "Removes hallucinated IDs."

But the validator should also enforce the **requested composition invariant**.

For example, Gemini could return:

```json
"selectedClothingIds": ["w_44", "w_37", "w_14"]
```

All three IDs are legitimate, but they're:

```text
TOP
TOP
DRESS
```

There is no Bottom or Shoes.

So I'd explicitly state that the validator verifies:

```text
ID exists
        ↓
No duplicate IDs
        ↓
Valid category
        ↓
Required role coverage
        ↓
Cardinality
        ↓
Rationale consistency
        ↓
Accept / Repair / Reject
```

This is much stronger than an ID-existence validator.

---

### 4. Rationale sanitization by regex is useful, but fragile

This:

```kotlin
Regex("(?i)[^.]*\\b${Regex.escape(name)}\\b[^.]*\\.")
```

works for straightforward English prose, but sentence-boundary regex isn't a particularly robust semantic validator.

For example:

```text
"Warm Eucalyptus Flats pair with the Oxford. Together, they..."
```

could behave differently depending on punctuation.

More importantly, **you shouldn't really need to sanitize hallucinated rationale after the fact.**

The strongest architecture is:

```text
Gemini
   ↓
structured selected IDs
   ↓
deterministic validation
   ↓
validated selection
   ↓
deterministic rationale generation
```

rather than:

```text
Gemini
   ↓
free-form rationale
   ↓
regex surgery
```

If Gemini still generates the rationale, I'd at least make it return only facts tied to IDs and generate the final human-readable product names/material claims locally.

---

### 5. Task 3 is good, but the scoring constants need calibration documentation

This:

```kotlin
score += 1.85
score += 1.25
score += 0.60
score += 0.75
score += 0.25
```

is deterministic, but these numbers are currently **heuristic weights** unless you've actually calibrated them.

That's perfectly fine for the Recommendation Engine.

I'd just document them as:

> Deterministic recommendation heuristics; not FASHIONISTA aesthetic calibration parameters.

That's important because otherwise someone could later accidentally interpret these as the mathematical aesthetic standard.

---

### 6. Task 4: "at most 1 OUTERWEAR" deserves clarification

You currently say:

> `distinctBy { it.category }`, constraining outfits to at most 1 TOP, 1 BOTTOM, 1 SHOES, and 1 OUTERWEAR.

That's reasonable **if that is actually your outfit schema**.

But `distinctBy { category }` is broader than that description. It means:

```text
one item per every category
```

not merely those four slots.

If tomorrow you have:

```text
ACCESSORY
BAG
SCARF
BELT
```

they could all be affected.

I'd make the slot policy explicit instead:

```kotlin
enum class OutfitSlot {
    TOP,
    BOTTOM,
    SHOES,
    OUTERWEAR
}
```

and deduplicate by slot.

That gives you a proper domain invariant rather than an accidental consequence of category strings.

---

### 7. Task 5: I would change the default `88`

This:

```kotlin
@Serializable val fashionistaScore: Int = 88
```

is a dangerous default.

Even though you've fixed the persistence path, a default of `88` means a missing score can silently become a legitimate-looking score.

That's exactly the sort of thing that caused the historical-score bug in the first place.

Prefer something like:

```kotlin
val fashionistaScore: Int? = null
```

or, if the model absolutely requires a value, use an explicit sentinel that cannot be mistaken for a real score.

Even better, if FASHIONISTA is now properly separated:

```kotlin
val fashionistaScore: Int?
val fashionistaCalibrationVersion: String?
```

That preserves the provenance of the score.

---

## One other important architectural distinction

I would rename this section:

> **History Score Persistence**

to:

> **FASHIONISTA Score Persistence**

because the score being persisted is specifically the FASHIONISTA evaluation score.

And I'd make the data flow explicit:

```text
Recommendation Engine
        │
        │ generates outfit
        ▼
Selected Ensemble
        │
        │ observed / represented as FashionistaObservation
        ▼
FASHIONISTA Evaluation Engine
        │
        ▼
aestheticScore + coverage + radar
        │
        ▼
History Persistence
```

That prevents the old candidate-ranking score from accidentally becoming the historical aesthetic score.

---

# Overall

I'd characterize this version as **architecturally sound with a few implementation-level changes still worth making**.

The strongest parts are:

* Dynamic prompt construction
* Deterministic post-LLM validation
* Ghost-anchor protection
* Category/slot enforcement
* Relational cosmetic scoring
* Removal of the old hardcoded cosmetic assumption
* Separation of Recommendation from FASHIONISTA
* Correct persistence of the calculated score
* 35/35 tests passing
* Successful debug and release builds

The **three changes I'd make before merging** are:

1. **Generate prompt requirements from typed candidate structures, not `String.contains()`.**
2. **Make the validator enforce role/cardinality invariants, not merely ID existence.**
3. **Remove `fashionistaScore = 88` as a silent default.**

With those changes, the document describes a much stronger **deterministic-first recommendation architecture**: Gemini becomes the synthesis layer, while deterministic code controls the evidence, constraints, validation, and persistence boundaries.
