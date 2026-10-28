**Prompt for AI Coding Assistant / IDE (Copy & Paste)**

**Context**
We are refactoring the **KoColor Recommendation Pipeline** to eliminate LLM hallucinations, fix invalid schema generation, and implement a deterministic post-LLM validation layer. This work is strictly for the generative retrieval pipeline and does not touch the FASHIONISTA evaluation engine.

**Task 1: Dynamic Prompt Construction (Pre-flight)**
Modify the Gemini prompt builder to dynamically request cosmetic categories based ONLY on what is available in the retrieved candidate manifest.

* Do not hardcode `"1 Eye, 1 Cheek, 1 Lip, 1 Nail"`.
* If the deterministic pruning stage yields `EYES`, `DIMENSION`, `LIPS`, and `PREP`, the prompt must dynamically instruct the LLM to select from those exact available categories.
* Enforce a strict grounding rule in the system prompt: *"Do not invent stylistic adjectives (e.g., do not call nylon 'structural'). Describe items strictly using the physical materials and attributes listed in the manifest."*

**Task 2: Deterministic Post-LLM Validator (Kotlin)**
Create a new `RecommendationValidator.kt` class that intercepts the raw JSON string from Gemini before it reaches the UI state flow. Implement the following verification checks:

1. **Category Integrity:** Parse `selectedClothingIds` and map them against the manifest. Assert exactly 1 `TOP`, 1 `BOTTOM`, and 1 `SHOES` (or `OUTERWEAR` based on dynamic request). Throw an internal retry exception if violated.
2. **ID Existence:** Assert that every ID in `selectedClothingIds` and `selectedCosmeticIds` actually exists in the `AVAILABLE CANDIDATES` list.
3. **Rationale Cross-Check (Sanitization):** Scan the `rationale` string for product names. If the rationale explicitly names a product (e.g., "Luminescent C Serum") but its corresponding ID (e.g., `c_150`) is missing from the selected arrays, safely strip that sentence from the rationale or trigger a fallback regeneration.

**Task 3: Relational Cosmetic Scoring**
In the deterministic pruning stage (`GreedyRehydrator.kt` or equivalent scoring engine), replace the flat `3.10` "Role diversity match" for cosmetics.

* Implement a basic relational heuristic: Score cosmetic candidates based on their color temperature compatibility with the locked outfit anchor.
* Warm anchors should boost the retrieval score of warm cosmetics (e.g., Terracotta Brick Lipstick); cool anchors should boost cool cosmetics.
* Retain role diversity as a secondary weight, but ensure the final retrieval score creates a mathematical ranking delta (e.g., `3.85`, `3.60`, `3.10`) so the LLM receives contextually relevant makeup.

**Task 4: Slot Deduplication Fallback**
Ensure the pipeline gracefully handles missing wardrobe categories. If the `AVAILABLE CANDIDATES` list contains 0 `SHOES`, the prompt must mathematically adapt to request `"Select BEST 2 clothing items (Top, Bottom)"` and the post-validator must adjust its assertions accordingly.