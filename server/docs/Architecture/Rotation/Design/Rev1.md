This is **much better**. I would consider this the right architectural direction, with **one important mathematical correction** and a few wording changes before you lock it.

### What I like

The separation is now clean:

```text
KCPS Catalog
    │
    │ canonical product
    ▼
ClothingItemEntity
    │
    └──────────────┐
                   │
User state         ▼
             ClothingUsageEntity
                   │
                   ▼
          RotationScoringUseCase
                   │
                   ▼
             Styling Engine
```

That is exactly the separation you want.

### 1. The entity separation is correct

This is a major improvement:

> `ClothingItemEntity` = what the product is
> `ClothingUsageEntity` = how the user uses it

So changing the KCPS catalog—for example, correcting a brand description or shade—doesn't touch:

```text
useCount
lastUsedTimestamp
```

That's excellent.

### 2. The atomic transaction is correct

This is also solid:

```text
BEGIN TRANSACTION

increment totalOutfitsCommitted

deduplicate product IDs

update ClothingUsageEntity
    item 1 +1
    item 2 +1
    item 3 +1
    item 4 +1

COMMIT
```

If item #3 fails:

```text
ROLLBACK EVERYTHING
```

That's exactly what you want.

### 3. Category-aware usage is substantially better

This:

> `Share = Item Use Count / Total Category Use Count`

is a much better concept than comparing every garment against every other garment.

It allows:

```text
TOPS
 ├── Black Tee      40%
 ├── White Shirt    25%
 ├── Blue Blouse    20%
 └── Red Top        15%
```

rather than comparing shirts against shoes, pants, etc.

---

## The one thing I would change

Your **category usage share** should probably be:

```text
Item Use Count
──────────────
Total Uses in Category
```

**only if the category represents a mutually exclusive selection slot.**

For example, if every outfit has exactly one top, then:

```text
100 outfits
100 top selections
```

and the math works beautifully.

But if your system allows:

```text
2 tops
1 jacket
2 accessories
```

then category totals have different semantics.

So I would explicitly define **what constitutes a category selection event**.

For the initial implementation, I'd make the rule:

> A garment contributes one usage event when it is included in a committed outfit, and category share is calculated against all usage events within its rotation category.

That makes the metric deterministic.

---

## I would also change "White Shirts vs. all Tops"

Your example says:

> "White Shirts" vs. all "Tops"

But your actual `categoryId` needs to be clearly defined.

I'd distinguish:

```text
macro_category = TOPS
rotation_category = TOPS
```

and potentially later:

```text
subcategory = SHIRTS
```

Then the system can eventually support:

```text
TOPS
  └── SHIRTS
       ├── White Shirt
       ├── Black Shirt
       └── Blue Shirt
```

without confusing KCPS taxonomy with personalization analytics.

---

# Recency is a good addition

This is one of the strongest parts:

> Items used within the last 48 hours receive the maximum penalty, which decays over time.

I'd keep the **48 hours as a policy constant**, not bake it into the database.

For example:

```text
ClothingUsageEntity
    useCount
    lastUsedTimestamp

RotationPolicy
    recencyWindow = 48h
    decayFunction = ...
```

That lets you change the AI behavior without changing the database schema.

---

# I would make the scoring terminology slightly clearer

You currently say:

```text
Final Selection Score =
    StyleCompatibility -
    (RotationPenalty × WeightingFactor)
```

That's good.

I'd formalize:

```text
RotationPenalty = f(categoryShare, recency)
```

Then:

```text
FinalScore =
    CompatibilityScore
    - RotationPenalty × RotationWeight
```

This makes Rotation a **modifier**, not a competing styling engine.

That's important because the AI should still be able to say:

> "Yes, you've worn this white shirt recently, but it is the overwhelmingly best garment for today's outfit."

The rotation system should **discourage**, not blindly prohibit.

---

# One thing missing: minimum-data behavior

You should specify what happens when usage data is sparse.

For example:

```text
User has 1 committed outfit
```

You don't want the system aggressively penalizing the only shirt they own.

I'd add:

### Cold Start Rule

If insufficient usage history exists:

```text
RotationPenalty = 0
```

until a minimum amount of history exists.

For example:

```text
totalOutfitsCommitted < N
    → no rotation penalty
```

You can choose `N` later.

---

# Final assessment

I'd say this is now **very close to lockable**.

| Area                               | Assessment                                         |
| ---------------------------------- | -------------------------------------------------- |
| Catalog/personalization separation | ✅ Excellent                                        |
| Room persistence                   | ✅                                                  |
| Atomic transactions                | ✅                                                  |
| Deduplication                      | ✅                                                  |
| Category awareness                 | ✅                                                  |
| Recency                            | ✅                                                  |
| AI integration                     | ✅                                                  |
| ViewModel responsibility           | ⚠️ Keep actual scoring in `RotationScoringUseCase` |
| Mathematical definition            | ⚠️ Define category-selection semantics             |
| Cold start                         | ⚠️ Add explicit rule                               |
| Schema migration independence      | ✅                                                  |

### Bottom line

**Yes, this is the architecture I would proceed with.**

I would make just three final additions before calling it **V1 locked**:

1. **Define exactly what a category usage event is.**
2. **Add a cold-start/minimum-history rule.**
3. **Keep the rotation mathematics entirely inside `RotationScoringUseCase`, with the ViewModel only orchestrating it.**

After those, the design is clean: **KCPS remains immutable canonical product data, Room stores user-specific wardrobe history, and the Rotation Scoring layer converts that history into an AI styling signal.**
