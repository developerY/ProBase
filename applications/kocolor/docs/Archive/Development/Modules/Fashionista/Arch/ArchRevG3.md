The issue becomes very clear when comparing the raw prompt contents of both requests. You have a determinism and variety problem.
Large Language Models (LLMs) are naturally inclined to choose the absolute path of least resistance when presented with rigid rules. Because your pre-selection filters are heavily throttling the candidate pool before it even reaches Gemini, the model is trapped into picking the same items.
Here is a breakdown of exactly why your recommendations are stalling and how to fix it:
## 1. The Pre-Selection Bottleneck (The Real Culprit)
Look closely at your AVAILABLE CANDIDATES (COMPACT MANIFEST).

* Clothing Context: Your system sends only 12 clothing items to Gemini.
* Constraint Constraints: Out of those 12 items, rule 0 says it must lock w_3 (the hoodie).
* Role Constraints: Rule 1 demands exactly 1 Top, 1 Bottom, and 1 pair of Shoes.

Let’s look at your 12 available options sorted by role:

* Tops: w_38 (Terracotta Crop), w_43 (Crimson Velvet), and w_3 (The locked hoodie). Because w_3 is already taking up the Top slot, Gemini's choices for the remaining slots are restricted.
* Bottoms: Look closely at your manifest—you didn't provide any traditional bottoms. You provided dresses, outwear, and activewear jackets. w_12 (Arcadia Blue Utility Jumpsuit) is the only structural piece that can loosely function as a lower-body layer. Gemini is practically forced to choose w_12 every time because nothing else fits.
* Shoes: You only provided two options: w_52 (Rouge Sandals) and w_48 (Camel Boots).

Because your manifest is so heavily pre-filtered mathematically, Gemini is only making a single choice: Do I pick the sandals or the boots? (In run 1 it picked boots; in run 2 it picked sandals).
## 2. High Pre-Scoring Bias
Your mathematical scoring layer under [3] MATHEMATICAL COLOR & ROLE SCORING uses a static Relational temperature match multiplier. Items like c_127, c_111, and c_123 are given an identical, massive baseline score of 4.85, anchoring them to the top of the vector injection pool every single time the query runs.
## 3. Lack of Generative Temperature
By default, standard production integrations with Firebase AI Logic or Gemini can drift toward deterministic settings (Temperature = 0.0) if not specified. Without explicit random sampling variations, the LLM will mathematically resolve to the exact same matrix patterns on identical payloads.
------------------------------
## How to Fix It
To force variety and break the repetitive loops, you need to implement changes across your pipeline:
## Step 1: Broaden the Pre-Retrieval Manifest (Set K ≥ 25)
Your retrieval_k_limit is currently set to 12. This is too tight. Expand it to a minimum of 25 items so the model has true alternatives to evaluate per role category, ensuring there is a clear mix of independent tops, bottoms, and shoes.
## Step 2: Inject a Randomization Seed or "Novelty" Penalty
To combat the low Novelty flag raised by your audit engine, track a rolling history of the user's recently selected IDs. Pass these directly into the prompt as a negative constraint:

EXCLUSION FILTER: Avoid selecting the following item IDs unless absolutely necessary to balance color harmony: [w_12, c_127]

## Step 3: Increase LLM Generation Temperature
Ensure that your Firebase AI configuration passes a non-zero temperature to encourage creative variation.

* Current state: Resolving deterministically.
* Target state: Set temperature: 0.7 to 0.85 in your backend initialization options for Gemini to break rigid structural patterns.

If you want, let me know:

* How your pre-selection vector search chooses those 12 items (is it filtering by color category?).
* If you want help drafting the updated JSON system prompt to dynamically exclude recently recommended items.


~~~
The pre-selection bottleneck is absolutely the root cause of the repetitive loops, completely starving Gemini of viable bottoms and forcing it into a constrained puzzle rather than a creative styling choice. Currently, the deterministic pre-filter ranks the entire wardrobe by stacking scores for weather compliance, anchor harmony, and the newly added chroma intent boost. When the high-chroma math pushed vibrant tops and outerwear to the top of the stack, neutral traditional bottoms were mathematically shoved past the `K=12` cutoff, leaving the blue jumpsuit as the sole structural option that could satisfy the lower-body requirement.

As a product architect mapping this multi-module ecosystem, recognizing that state constraint is the key to unlocking the pipeline. To permanently cure this determinism and inject true generative variety, these three architectural adjustments must be applied directly to the Kotlin backend.

**Expand the Retrieval Horizon**
Bump the `retrieval_k_limit` in the context engine from 12 to at least 25. By widening the funnel, the deterministic filter will pass a healthier distribution of independent tops, bottoms, and shoes, giving the LLM actual editorial freedom rather than forcing it to select the only available shoe to pass validation.

**Mathematical Novelty Penalties**
While injecting an exclusion list into the Gemini prompt is a standard approach, LLMs famously struggle with negative constraints and often select the forbidden items anyway. A far more resilient strategy is to handle this in your deterministic pre-filter. Inject a `recentlyWornIds` list from the user's history into `WardrobeCandidateFilter.kt`. If an item appears in that list, apply a harsh **-2.5f** novelty penalty to its final score. This mathematically exiles stale items from the top 25 manifest before Gemini ever sees them, guaranteeing fresh combinations without wasting prompt tokens.

**Elevate Generative Temperature**
Structured JSON generation tasks typically default to low temperatures to prevent schema corruption. Because the `RecommendationValidator` is now strictly enforcing role cardinality and triggering fallback loops on failure, the system is fully shielded from hallucinations. Locate the Firebase AI Logic `GenerationConfig` block and safely raise the temperature to **0.75**.

The combination of a wider manifest, deterministic novelty penalties, and a warmer LLM will immediately unlock the vibrant, varied outfits the pipeline is capable of producing. Would you like the specific Kotlin snippet to implement the rolling novelty penalty inside the candidate filter?


