This is the architectural leap that takes the system from a feature to a platform, Ash.

You completely nailed the distinction between a *Projected Rotation State* and a *Committed Rotation State*. Without that separation, a simulated 7-day forecast would permanently corrupt the user's actual database history every Sunday morning. Structuring `SelectionRationale` as a deterministic data class rather than a black-box AI string is also a brilliant move for future debugging and user trust.

Here is the finalized, mathematically sound, and architecturally locked V2 Specification.

---

# KoColor: V2 Product Strategy & Architecture Vision

**The Core Philosophy:** *V1 builds the memory. V2 turns the memory into orchestration.*

KoColor is evolving from a static digital inventory into a **Personal Style Operating System**. The application operates as a personal style orchestration engine that continuously converts the user's wardrobe, cosmetics, color profile, calendar, location, weather, and behavioral history into an adaptive style playlist.

In this architecture, the **Playlist** is not a UI projection; it is the central, stateful product object that drives the entire consumer experience.

## 1. The Orchestration Architecture

The system is strictly divided into **Personalization Context Streams** (the current state of the user and their environment) and the **Feedback Stream** (the historical memory of their actions).

### The Core Loop

> **Context** → **Intelligence** → **Playlist** → **Behavior** → **Memory** → **Intelligence**

```text
                    ┌──────────────────────┐
                    │      CONTEXT         │
                    │                      │
                    │ Calendar             │
                    │ Location             │
                    │ Weather              │
                    │ Wardrobe             │
                    │ Cosmetics            │
                    │ Color Profile        │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │    STYLE ENGINE      │
                    │                      │
                    │ Compatibility        │
                    │ Rotation             │
                    │ Weather              │
                    │ Context              │
                    │ Color                │
                    │ Cosmetics            │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │   STYLE PLAYLIST     │
                    │                      │
                    │ 7-day plans          │
                    │ outfits              │
                    │ remixes              │
                    │ cosmetics            │
                    │ rationales           │
                    └──────────┬───────────┘
                               │
                               ▼
                         USER WEARS
                               │
                               ▼
                    ┌──────────────────────┐
                    │   ROTATION MEMORY    │
                    │                      │
                    │ useCount             │
                    │ recency              │
                    │ history              │
                    └──────────┬───────────┘
                               │
                               └──────────────► STYLE ENGINE

```

---

## 2. Core Architecture: On-Device Color & Contrast Calibration

To provide flawless personalization, the application establishes the user's mathematical aesthetic baseline locally.

### 2.1 Zero-Cloud Feature Extraction

* **Lighting Validation:** The engine utilizes ambient light sensors and camera exposure metrics to ensure natural lighting, preventing artificial color casting.
* **On-Device Processing:** Using Android CameraX and an on-device ML model, the system extracts color hex codes from the skin surface, iris, and natural hair root. Frames are processed entirely in memory and immediately discarded.

### 2.2 The Classification Pipeline

1. **Undertone & Brightness:** Calculates whether the skin pulls warm (yellow/peach) or cool (blue/pink) and measures overall luminance.
2. **Contrast Delta:** Measures the difference between the skin, eyes, and hair (e.g., pale skin with dark hair = high contrast).
3. **12-Season Matrix Mapping:** These color and contrast measurements are deterministically mapped to a seasonal palette classification (e.g., *Soft Summer*, *Deep Autumn*).

---

## 3. The Killer Feature: The Cosmetic Crossfade

Cosmetics are functional components of styling, not just another inventory category. They act as the algorithmic bridge between the user's color profile and their chosen garments, creating a closed personalization triangle: **Garment ↔ User Color Profile ↔ Cosmetic Inventory**.

If a user selects a garment that technically violates their color harmony, KoColor queries the **Virtual Vanity** to recommend specific makeup shades that artificially compensate for the harmony gap.

**The Crossfade Execution Flow:**

> **Garment** (e.g., Cool Winter Black Dress)
> ↓
> **Color Analysis** (Detects item is high-contrast, cool-toned)
> ↓
> **User Color & Contrast Profile** (e.g., Soft Autumn)
> ↓
> **Detected Harmony Gap** (Clash: User needs warmth and muted contrast)
> ↓
> **Virtual Vanity** (Queries owned `CosmeticItemEntity` database)
> ↓
> **Targeted Palette** (Recommends warm peach blush + soft brown eyeliner)
> ↓
> **COHESIVE LOOK**

---

## 4. Core Architecture: 7-Day Style Playlist Engine

The Playlist is a first-class domain entity containing the logic for *what* to wear, *when* and *where* it makes sense, *how* to adapt it, *which* cosmetics complete it, and *why* the engine selected it.

### 4.1 Provenance & The Selection Rationale

To ensure the AI's decisions are inspectable and deterministic, every playlist item includes a structured rationale:

```kotlin
data class SelectionRationale(
    val calendarReason: String?,
    val weatherReason: String?,
    val locationReason: String?,
    val colorReason: String?,
    val rotationReason: String?,
    val cosmeticReason: String?
)

```

### 4.2 Projected vs. Committed Rotation State

To prevent the engine from generating seven identical outfits, KoColor utilizes a simulated state-forwarding loop separated from the user's actual database.

* **Projected Rotation State:** Exists only during playlist generation. When an item is selected for Day 1, a simulated usage event writes to this temporary state, immediately enforcing the V1 48-hour cooldown across Day 2 and Day 3. This creates a deterministic constraint that promotes rotation and diversity across the week.
* **Committed Rotation State:** The actual wear history persisted to the `ClothingUsageEntity` memory layer only after the user commits to the outfit.

### 4.3 The Playlist Lifecycle (The State Machine)

As a stateful entity, the Playlist natively closes the loop between V2 generation and V1 memory through a strict lifecycle that accounts for multi-event days:

> `GENERATE` (Engine builds the 7-day forecast from Context Streams)
> ↓
> `PREVIEW` (User reviews the week)
> ↓
> `ACCEPT` (User confirms or pins specific items)
> ↓
> `LOCK` (State is saved to Room)
> ↓
> `DAILY ROUTE` (Contextual routing adapts the base plan into an Evening Remix)
> ↓
> `WEAR` (Action taken)
> ↓
> `COMMIT` (Atomic database transaction to Committed State)
> ↓
> `ROTATION HISTORY` (Feedback Stream updates V1 Memory)
> ↓
> `NEXT PLAYLIST` (Engine triggers anew)

---

## 5. Integration with V1 Analytics (Style Wrapped)

The rigorous behavioral data generated by the closed Playlist Lifecycle will be synthesized into highly shareable, viral aesthetic summaries.

* **Wardrobe Heroes (Top Played Tracks):** Visualizes the items with the highest algorithmic rotation and manual selection scores.
* **The Investment Portfolio:** Rebrands standard Cost-Per-Wear (CPW) into a celebration of high-ROI purchases based on actual playlist deployments.
* **Style Eras:** Uses historical Calendar context tags to define the user's shifting aesthetic personas over time.

---

With the V2 Architecture and domain vocabulary officially locked, what is the next step for implementation—mapping out the Room database schema migrations for the stateful `Playlist` entity, or scaffolding the CameraX flow for the Color & Contrast Calibration?