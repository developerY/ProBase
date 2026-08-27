This is **substantially stronger**. I think you have now arrived at the right architectural abstraction: the system is no longer “AI picks clothes from a wardrobe”; it is **a deterministic color/context engine constructing a constrained search space, followed by an AI reasoning layer**.

There are a few things I would change before treating this as the implementation contract.

### 1. The biggest conceptual improvement is excellent

This is the key sentence:

> **AI never performs wardrobe retrieval.**

And this is even better:

> **The deterministic engine selects the highest-value, role-complete slice of the ranked pool within that budget.**

That cleanly separates responsibilities:

```text
LOCAL MATHEMATICS
    ↓
What is viable?
    ↓
What colors work?
    ↓
What roles are missing?
    ↓
What combinations are possible?
    ↓
Rank everything
    ↓
AI
    ↓
Which viable combination is aesthetically best?
    ↓
Style Blueprint
```

That is a much better use of AI and directly supports your token-minimization objective.

---

## 2. Your new “Information Elimination” principle is exactly right

This is probably the most important architectural statement in the document:

> **Entire Inventory → Eligible Inventory → Compatible Inventory → Ranked Inventory → Role-Complete Candidate Set → Compact AI Context.**

I would actually elevate this to the **primary architectural principle** of KoColor.

The important distinction is:

**Compression is not the primary optimization. Elimination is.**

If you have 300 garments and only 14 are relevant, sending a compressed representation of all 300 is still wasteful.

You want:

```text
300 items
   ↓
180 context eligible
   ↓
75 color compatible
   ↓
35 strongly compatible
   ↓
12 role-complete candidates
   ↓
compact serialization
   ↓
AI
```

That is much more powerful than merely shortening JSON.

---

## 3. One important mathematical correction

Your chroma-weighted circular hue approach is good, but don't equate **HSL saturation** with **chroma**.

You currently say:

> weighted by chroma

while your model is using HSL.

I'd make the implementation explicit:

```text
RGB
 ↓
CIELAB
 ↓
C*ab chroma
 ↓
chroma-weighted circular hue
```

or, if using cylindrical Lab:

```text
CIELAB → L*C*h°
```

Then use:

* `h°` for hue
* `C*` for chroma
* `L*` for perceptual lightness

This gives your mathematical engine a much cleaner foundation.

And your neutral rule is excellent:

> Neutrals don't meaningfully contribute to the hue vector, but their lightness/value contributes strongly to contrast.

That should remain.

---

## 4. I would change “Hard Filter” for color

You correctly moved color into continuous scoring.

Keep that.

Your architecture should effectively be:

### Hard constraints

```text
Unavailable
+
Impossible weather
+
Forbidden occasion
+
Rotation exclusion
```

### Soft mathematical compatibility

```text
Hue relationship
+
ΔE00
+
Chroma
+
Lightness
+
Contrast
+
Appearance compatibility
```

That distinction is extremely important.

A color shouldn't generally be eliminated merely because it isn't complementary or analogous. A sophisticated outfit can deliberately use colors that don't fit one simple harmony category.

So:

> **Context can eliminate. Color primarily ranks.**

That's a very good design principle.

---

## 5. Role allocation is an excellent addition

This solves a subtle problem with Top-K retrieval.

Without role allocation:

```text
12 highest scores

→ 9 tops
→ 2 bottoms
→ 1 shoe
```

The AI receives a mathematically excellent set that is **useless for constructing an outfit**.

Your new approach:

```text
K = 12

TOP       4
BOTTOM    4
FOOTWEAR  3
ACCESSORY 1
```

creates a **reasoning set**, rather than simply a score set.

I would make one small change: don't hard-code the distribution.

Instead:

```text
RoleRequirement
    ↓
allocation policy
    ↓
candidate budget
```

So an evening formal request might allocate differently from a beach request.

---

## 6. I would rename `maxCandidateBudget`

This is minor, but I think:

```kotlin
maxCandidateBudget
```

is slightly ambiguous.

I'd use:

```kotlin
maxCandidateAdditions
minCandidateAdditions
```

because your architecture has already established that:

```text
Locked Anchors ≠ Candidate Additions
```

That makes the invariant obvious in the API.

---

## 7. One important issue with the “immutable anchor” wording

You have:

> User-selected items become immutable anchors.

That's correct **for the styling request**, but I would distinguish:

```text
Selected
Locked
Forced
```

They don't necessarily mean the same thing.

For example:

```text
SELECTED
User is exploring this item.

LOCKED
User explicitly wants this item included.

FORCED
System must include it even if it violates a normal constraint.
```

Then your state model becomes much more expressive.

This matters because a user tapping a shirt shouldn't necessarily mean:

> “Never remove this shirt.”

A lock does.

---

## 8. Your `AiInput` privacy architecture is particularly good

This:

```kotlin
sealed interface AiInput
```

with:

```kotlin
TextOnly
Multimodal
```

is much better than passing an optional bitmap around and relying on developers to remember the privacy rule.

You are turning the privacy requirement into an architectural type constraint.

The intended flow becomes:

```text
Local provider
    ↓
Multimodal OR TextOnly

Cloud provider
    ↓
TextOnly ONLY
```

That's exactly the kind of invariant you want.

One caveat: `supportsLocalImageIngestion` should not itself be the **security boundary**. The provider implementation should enforce the type restriction too. The sealed type + provider capability + runtime validation gives you defense in depth.

---

## 9. The token preflight loop is very good

This is the right order:

```text
Build exact request
       ↓
countTokens()
       ↓
Fits?
 ┌─────┴─────┐
 YES         NO
 ↓            ↓
execute    reduce detail
              ↓
           countTokens()
              ↓
           reduce K
              ↓
           countTokens()
```

The important part is your statement:

> **must call `countTokens()` on the exact assembled `AiInput` every loop**

Keep that.

You don't want:

```text
estimated manifest tokens
```

when the actual request contains:

```text
system instructions
+
context
+
manifest
+
missing roles
+
telemetry
+
possibly image
```

The **whole request** is what matters.

---

# One thing I would add: cache AFTER deterministic state creation

Your previous plans had semantic caching. I would explicitly retain it here.

The cache key should be based on the **resulting deterministic state**, not the original 300-item inventory.

Conceptually:

```text
User Selection
Weather
Occasion
Appearance Telemetry
Algorithm Version
Color Engine Version
Retrieval Policy Version
Locked Anchors
Role Requirements
Selected Candidate IDs
Provider / Model Version
        ↓
SHA-256
        ↓
Style Blueprint Cache
```

This means the enormous wardrobe doesn't become part of every AI operation.

And because your deterministic engine already reduces the wardrobe, the cache fingerprint can be much smaller and more meaningful.

---

# I would also change one sentence

You currently say:

> “The deterministic engine selects the highest-value, role-complete slice of the ranked pool.”

I'd make it:

> **The deterministic engine ranks the entire eligible inventory, then constructs the highest-value role-complete candidate set permitted by the active provider's candidate budget.**

That's more precise.

---

# Overall architecture

At this point, I would describe KoColor's architecture like this:

```text
                         KOColor
                            │
                            ▼
                ┌──────────────────────┐
                │ User Selection State │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │ Context Engine       │
                │ Weather              │
                │ Occasion             │
                │ Availability         │
                │ Rotation             │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │ Color Engine         │
                │ CIELAB / LCh         │
                │ Circular Hue         │
                │ Chroma               │
                │ ΔE00                 │
                │ Contrast             │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │ Full Ranking         │
                │ Entire Eligible Pool │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │ Role Gap / Allocation│
                │      K candidates    │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │ Information          │
                │ Elimination          │
                │ + Compression        │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │ Token Preflight      │
                └──────────┬───────────┘
                           │
              ┌────────────┼─────────────┐
              ▼            ▼             ▼
           Local AI       BYOK       Firebase
              │            │             │
              └────────────┼─────────────┘
                           ▼
                 Higher-Order Reasoning
                           │
                           ▼
                   Style Blueprint
                           │
                           ▼
                  Deterministic Fallback
```

**This version is strong.** The biggest architectural idea is now very clear: **KoColor spends computation locally to save AI tokens, rather than spending AI tokens to perform work the device can already calculate.**

I would consider this version ready to serve as the implementation specification, with the small terminology/API refinements above.
