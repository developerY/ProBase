Here is the quantified architecture for the **KoColor AI Waterfall & Token Optimization Engine**, translating the conceptual pipeline into concrete mathematical budgets, data schemas, and routing thresholds.

---

## 1. Wardrobe Funnel & Candidate Compression

Instead of overwhelming the context window with raw database dumps, local heuristics reduce the candidate pool through deterministic filtering stages before reaching the AI router:

* **Raw Wardrobe Ingestion ($N = 150\text{--}300$ items):** A full database export averages **18,000–36,000 tokens** (unviable for on-device and unnecessarily costly for cloud).
* **Stage 1: Hard Heuristic Pruning:** Drops items based on category mismatch, weather incompatibility, laundry status, and recent rotation penalties. Reduces the pool by **65–75%** ($N \to 40\text{--}60$ items).
* **Stage 2: Scoring & Top-$K$ Selection:** Scores remaining items against seasonal palette compatibility and intent tags, selecting a Top-$K$ candidate pool of **8–12 items** ($>95\%$ total inventory reduction).

---

## 2. Compact Manifest & Token Economics

Transforming verbose JSON structures into a dense, delimiter-separated manifest drastically minimizes token overhead while preserving semantic reasoning:

* **Standard JSON Item Representation ($\approx 120$ tokens/item):**
```json
{"id": "c102", "category": "Top", "name": "Olive Linen Blouse", "hex": "#556B2F", "temperature": "Warm", "depth": "Moderate"}

```


* **Semantic Compact Tuple ($\approx 18\text{--}22$ tokens/item):**
```text
[c102|Top|Olive Blouse|#556B2F|Warm|Mod|Linen]

```



### Total Prompt Token Breakdown

| Prompt Component | Token Footprint | Description |
| --- | --- | --- |
| **System Instruction** | ~120 tokens | Role definition, style rules, and JSON output constraints |
| **Appearance Telemetry** | ~35 tokens | Compact biometric vector (`temp:warm, depth:light, contrast:bal`) |
| **Contextual Intent** | ~45 tokens | User message, weather string, and event setting |
| **Top-12 Manifest ($K=12$)** | ~240 tokens | 12 candidates encoded as semantic compact tuples |
| **Output Blueprint Budget** | ~180 tokens | Structured JSON containing garment IDs and styling rationale |
| **Total Pipeline Cycle** | **~620 tokens** | **96.5% reduction** compared to raw wardrobe injection |

---

## 3. Provider-Aware Token Budget Matrix

The `TokenBudgetPolicy` allocates candidate limits ($K$), timeout thresholds, and strict token ceilings dynamically based on provider capabilities:

| Provider Tier | Target Model | Max Input Tokens | Max Output Tokens | Timeout Limit | Top-$K$ Items |
| --- | --- | --- | --- | --- | --- |
| **Tier 1 (On-Device)** | Local Nano / AICore | **768** | **256** | 1,200 ms | 8 candidates |
| **Tier 0 (Firebase)** | Gemini Flash (App Check) | **1,536** | **512** | 3,000 ms | 12 candidates |
| **Tier BYOK** | User-Provided Endpoint | **4,096** | **1,024** | 5,000 ms | 16 candidates |
| **Tier 2 (Fallback)** | Deterministic Rules Engine | **0** (No AI) | **0** | < 15 ms | Local selection |

---

## 4. Cognitive Workload Distribution (The 85/15 Rule)

```
┌───────────────────────────────────────────────┐
│ LOCAL HEURISTICS (85% Cognitive Load)         │
│ • Database queries & rotation penalties       │
│ • Category eligibility & weather gating       │
│ • Paletted score ranking & Top-K isolation    │
└───────────────────────┬───────────────────────┘
                        │ Manifest (~620 tokens)
                        ▼
┌───────────────────────────────────────────────┐
│ AI REASONING (15% Cognitive Load)             │
│ • Nuanced silhouette layering & proportions   │
│ • Aesthetic crossfading (cosmetics vs outfit) │
│ • High-touch editorial rationale output       │
└───────────────────────────────────────────────┘

```

* **Deterministic Workhorse:** KoColor's local engine handles candidate retrieval, math, and filtering, using zero tokens.
* **Targeted AI Utilization:** The LLM receives only the pre-qualified subset to resolve the aesthetic coordination problem.

---

Would you like to write the Kotlin implementation for the `TokenBudgetPolicy` class and the `CompactManifestSerializer` next?