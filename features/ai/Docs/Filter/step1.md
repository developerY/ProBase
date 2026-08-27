When a user **explicitly selects one or more items**, I would change the algorithm fundamentally: **the selected items become constraints/anchors, not merely candidates.**

The algorithm should work **around what the user chose**, rather than reconsidering whether those items belong.

### Recommended behavior

```text
USER SELECTS ITEMS
        │
        ▼
Identify selected roles
        │
        ▼
Validate compatibility among selections
        │
        ▼
Create Selection Color Profile
        │
        ▼
Find what is MISSING
        │
        ▼
Local Color + Context Engine
        │
        ▼
Rank compatible additions
        │
        ▼
Top-K complementary inventory
        │
        ▼
AI reasoning (if available)
```

### 1. Treat selected items as locked

If the user selects:

> Navy blazer + white shirt

the algorithm should **not replace either item**.

Those become the styling anchors.

If they select:

> Red dress

the algorithm should ask:

> What from the wardrobe works *with this dress*?

—not:

> Is the red dress the best garment?

That's a major distinction.

---

## 2. Build a composite mathematical profile

For multiple selected items, calculate their combined profile:

```text
Selected Items
    ↓
Colors
    ↓
Hue distribution
Saturation
Lightness
ΔE relationships
Contrast
Warm/Cool temperature
Pattern information
Garment roles
    ↓
Composite Selection Profile
```

For example:

```text
Selected:
Navy blazer
White shirt
Tan belt

Composite:
Dominant → Navy
Secondary → White
Accent → Tan
Temperature → Neutral/Warm
Contrast → High
Style → Smart casual
```

Now the local engine knows what the **existing outfit already contains**.

---

## 3. Find the missing roles

This is where I think KoColor can become particularly smart.

Don't simply search for "matching colors."

Determine what the selected items are **missing**.

Example:

```text
Selected:
Navy blazer
White shirt

Roles:
✓ Outerwear
✓ Top

Missing:
? Bottom
? Footwear
? Optional accessory
```

Then retrieve only candidates capable of filling those roles.

---

## 4. Colorimetry should operate relative to the selection

The ColorHarmonyEngine now becomes:

```text
Selected Palette
       │
       ├── Hue relationships
       ├── ΔE00
       ├── Lightness
       ├── Saturation
       ├── Contrast
       └── Temperature
              │
              ▼
       Candidate evaluation
```

So if the user selects a navy blazer, the engine might rank:

```text
Gray trousers       HIGH
Khaki trousers      HIGH
Cream trousers      HIGH
Black trousers      MEDIUM
Bright orange       LOW
```

But **color alone isn't enough**.

The candidate must also satisfy:

```text
Weather
Occasion
Garment role
Availability
Rotation
User appearance profile
```

---

# 5. Don't just return the "best colors"

This is important.

Suppose the user selects:

**black leather jacket**

There could be 40 mathematically compatible garments.

The engine should find the **best complete combinations**.

For example:

```text
                    BLACK JACKET
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
           White       Gray       Burgundy
             │           │           │
           Jeans       Black       Dark denim
             │           │           │
           Sneakers     Boots       Boots
```

The local engine can score these combinations before AI ever sees them.

---

# 6. Multiple selections should progressively constrain the search

This is perhaps the most important rule.

If the user selects:

```text
Navy blazer
```

→ large compatible candidate space.

Then:

```text
Navy blazer + white shirt
```

→ smaller candidate space.

Then:

```text
Navy blazer + white shirt + tan shoes
```

→ smaller still.

Then:

```text
Navy blazer + white shirt + tan shoes + gray trousers
```

→ AI may only need to decide whether accessories/finishing touches improve the combination.

So:

> **Every user selection reduces the search space rather than restarting the search.**

That gives you an elegant interactive styling system.

---

# 7. And this changes your Top-K definition

For an unselected wardrobe:

```text
300 items
   ↓
context
   ↓
colorimetry
   ↓
roles
   ↓
Top-K
```

For a user-selected outfit:

```text
300 items
   │
   ├── SELECTED ITEMS ──────────────┐
   │                               │
   ▼                               ▼
Context filtering          Composite color profile
   │                               │
   └──────────────┬────────────────┘
                  ▼
          Find missing roles
                  │
                  ▼
       Color + Context scoring
                  │
                  ▼
        Compatible additions
                  │
                  ▼
                Top-K
```

**The selected items themselves should not consume the candidate budget.**

If the user selected 3 garments and your AI policy allows 12 candidates, I'd consider:

```text
3 locked selections
+
up to 12 retrieved additions
```

rather than forcing the selected garments into the 12-item retrieval pool.

That distinction will make the system much more powerful.

---

## 8. Then AI gets a very different prompt

Instead of:

> "Here are 300 wardrobe items. Create an outfit."

AI gets something closer to:

```text
USER SELECTED:
[w12|Blazer|Navy|#1F2937]
[w42|Shirt|White|#F8F8F5]

COMPATIBLE ADDITIONS:
[w55|Trouser|Khaki|#B8A992]
[w71|Trouser|Gray|#6B7280]
[w91|Shoe|Brown|#6B4423]
[w103|Shoe|White|#F5F5F5]

CONTEXT:
28°C
Business Casual
Daytime

TASK:
Complete the user's selected outfit.
Do not replace selected items.
```

That's **dramatically smaller and dramatically smarter** than asking AI to search the wardrobe.

### I would add this to your architecture as a new section:

> **User Selection Mode:** Explicitly selected garments become immutable styling anchors. The deterministic engine derives a composite color/context profile from the selected items, identifies missing garment roles, and retrieves only compatible additions. Each subsequent selection progressively constrains the candidate space. AI may synthesize and rank among retrieved additions but may never replace an explicitly locked selection.

That, in my opinion, is the right behavior for KoColor.
