Exactly. **That is the architecture I would build.** The key is that AI selection and token optimization are two separate layers.

### The KoColor AI Waterfall

```text
                         KoColor Request
                                │
                                ▼
                    ┌──────────────────────┐
                    │ Local Candidate RAG  │
                    │ + filtering          │
                    │ + ranking             │
                    │ + compact manifest    │
                    └──────────┬───────────┘
                               │
                               ▼
                         Local Cache?
                         /          \
                       HIT          MISS
                        │              │
                        ▼              ▼
                     RETURN      AI Capability Router
                                      │
             ┌────────────────────────┼──────────────────────┐
             │                        │                      │
             ▼                        ▼                      ▼
       On-device AI                 BYOK             Firebase AI Logic
       Gemini/Gemma                 User pays             We pay
       IMAGE + data                 data only             data only
             │                        │                      │
             └────────────────────────┼──────────────────────┘
                                      │
                                      ▼
                              Other AI Available?
                                      │
                                      ▼
                              Best available AI
                                      │
                                      ▼
                              Local Heuristics
```

And the priority should be exactly what you said:

1. **Best capable local AI → use it**
2. **User BYOK → use it**
3. **Firebase capacity → use it**
4. **Other available AI → use it**
5. **Deterministic KoColor engine → always works**

The important improvement is that **we shouldn't hard-code "Gemini Nano" as Tier 1.5**. The architecture should detect the best capable on-device model available on that device. If a future device has a much better local model, KoColor should automatically take advantage of it.

---

# Token optimization becomes a first-class subsystem

And I completely agree with your last sentence:

> **Token count is SUPER important for every AI path except the deterministic path.**

So we should optimize **before choosing the provider**, not after.

### The pipeline should be:

```text
Raw Wardrobe
     │
     ▼
Local Filtering
     │
     ├── Remove irrelevant categories
     ├── Rotation penalty
     ├── Weather suitability
     ├── Occasion
     ├── Color compatibility
     └── Availability
     │
     ▼
Candidate Ranking
     │
     ▼
Top-N Selection
     │
     ▼
Semantic Compression
     │
     ▼
Compact AI Context
     │
     ▼
Token Budget Check
     │
     ├── Local model budget
     ├── BYOK model budget
     ├── Firebase budget
     └── Other model budget
     │
     ▼
AI Router
```

That is **much better than sending the entire wardrobe to whichever AI happens to run.**

---

## And I would add one important optimization

Don't make the LLM solve problems that KoColor already knows how to solve.

For example:

**Local deterministic engine should decide:**

* weather suitability
* garment category
* rotation penalty
* availability
* basic color compatibility
* season
* occasion eligibility
* candidate ranking
* duplicate elimination

Then AI gets the **small, difficult reasoning problem**:

> "Given these 12 candidates, this appearance telemetry, this weather context, and this occasion, construct the best coordinated style."

That is where the AI adds value.

### In other words:

**Don't use tokens to perform database queries.**

**Don't use tokens to perform filtering.**

**Don't use tokens to perform arithmetic.**

**Don't use tokens to rediscover facts KoColor already knows.**

Use tokens for **reasoning**.

---

# I would change your current plan accordingly

Instead of:

> `MAX_CLOUD_INPUT_TOKENS = 3,000`

I would make token budgeting **provider-aware**:

```text
TokenBudgetPolicy

localBudget
byokBudget
firebaseBudget
fallbackBudget
```

Because a 3,000-token prompt might be perfectly reasonable for one model and unnecessarily large for another.

The router should effectively ask:

```text
What is the best AI available?
What is its context/token capability?
What is our desired quality?
What is the minimum context required?
```

Then construct the **smallest prompt that preserves the intelligence required for that model**.

---

## The real goal

We're not trying to make the AI **see less**.

We're trying to make the AI **think with less irrelevant information**.

That's a very important distinction.

Your architecture should therefore be:

> **Maximum local intelligence → minimum necessary AI context → maximum reasoning per token → deterministic fallback.**

That gives KoColor a genuinely intelligent architecture rather than simply throwing a huge JSON document at Gemini and hoping the model figures out what matters.
