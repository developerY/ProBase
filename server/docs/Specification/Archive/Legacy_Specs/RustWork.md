Here is the specification document for your development backlog. It maps out exactly how to implement the Phase 3 compiler upgrades, providing clear instructions and JSON output examples for whichever engineer picks up the ticket.

---

# Roadmap: Phase 3 Advanced Compiler Enrichments (V1.2)

This document outlines the next iteration of the **Compute-at-Compile-Time** architecture for the `kocolor-compiler`.

Our core directive remains unchanged: **Protect the mobile CPU.** We will continue to offload heavy string parsing, mathematical formatting, and search indexing to the Rust compiler to ensure the Jetpack Compose UI maintains a strict 60fps, zero-latency standard.

---

## 🚀 1. Hero Ingredient Extraction (INCI Parser)

**The Challenge**: The standard `ingredients` array contains dense chemical nomenclature (INCI). Mobile users want to see marketable "Hero" ingredients (e.g., Niacinamide, Hyaluronic Acid) at a glance, but forcing Android to parse 50-item string arrays on the fly wastes processing cycles.

**The Rust Solution**:

* Implement a dictionary-based scanner in the enrichment module.
* The compiler cross-references the `ingredients` array against a predefined `HERO_ACTIVES` hash set.
* **Data Injection**: It extracts the matches and injects a `calculated_hero_actives` array.

**Target JSON Output**:

```json
"calculated_hero_actives": [
  "Niacinamide",
  "Squalane",
  "Ascorbic Acid"
]

```

**Android Benefit**: Jetpack Compose binds this array directly to a horizontal chip row under the product image, bypassing all text-parsing logic.

---

## 🧮 2. Price-Per-Unit Normalization

**The Challenge**: Displaying a "Price per ml/g" is a standard retail UX requirement. The KCPS schema stores `price` (Float) and `volume` (String, e.g., "30ml"). Forcing Android to run regex on the volume string, convert it to a float, and execute division for 50 items in a LazyGrid causes unnecessary CPU drag.

**The Rust Solution**:

* The compiler strips the alphabetic characters from the `volume` string to isolate the integer/float.
* It identifies the unit of measurement (`ml`, `g`, `oz`).
* It performs the division: `price / volume`.
* **Data Injection**: It formats the final string and injects `calculated_unit_price`.

**Target JSON Output**:

```json
"price": 34.00,
"volume": "30ml",
"calculated_unit_price": "$1.13/ml"

```

**Android Benefit**: The mobile client treats this as a dumb string and simply renders it to the screen.

---

## 🔍 3. N-Gram & Phonetic Search Tokenization

**The Challenge**: Standard SQLite `LIKE %term%` queries are slow and completely intolerant of typos. Executing fuzzy matching (Levenshtein distance) in Kotlin is too heavy for instantaneous search results.

**The Rust Solution**:

* The compiler digests the `name`, `brand`, and `micro_category`.
* It generates a highly optimized index array including exact matches, bi-grams, common typos, and phonetic equivalents (using Soundex or Metaphone algorithms in Rust).
* **Data Injection**: It injects a `calculated_search_tokens` array.

**Target JSON Output**:

```json
"name": "Seamless Silk Foundation",
"calculated_search_tokens": [
  "seamless", "silk", "foundation", "fndtn", "base", "seamles"
]

```

**Android Benefit**: RoomDB executes a hyper-fast `IN` query against the pre-computed token array. Users experience typo-tolerant, instant search results with zero computational overhead on the device.

---

## 🛡️ Implementation Guardrails

1. **KCPS v1 Forward Compatibility**: All injected fields MUST be prefixed with `calculated_` to clearly distinguish them from the raw partner input data.
2. **Graceful Degradation**: Because these fields are outside the strict KCPS v1 schema, they remain optional. The Rust compiler must not fail if an item lacks the required data to compute a specific enrichment (e.g., if `volume` is missing, simply skip the `calculated_unit_price` injection).
3. **Android Opt-In**: The Android Room database entities will be updated to consume these fields asynchronously. The generation of these fields in Rust will not break the existing V1 ingestion pipeline.