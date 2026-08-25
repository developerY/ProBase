Here is the fully updated V2 Architecture Markdown. It elevates the "Playlist" to the center of the product, refines the privacy terminology, breaks out the Cosmetic Crossfade as a standalone killer feature, and formally introduces the state machine that loops V2's orchestration right back into V1's memory.

---

# KoColor: V2 Product Strategy & Architecture Vision

**The Core Philosophy:** *V1 builds the memory. V2 turns the memory into orchestration.*

KoColor is evolving from a static digital inventory into a **Personal Style Operating System**. By adapting the mechanics of audio streaming—discovery, algorithmic curation, and frictionless user engagement—KoColor removes the cognitive load of daily styling. It programs the user's wardrobe into a continuously evolving style playlist fueled by six distinct data streams: **Wardrobe, Cosmetics, Bio Data, Calendar, Location, and Weather.**

## 1. The Strategic Vision: "Spotify for the Closet"

The underlying architecture maps directly to the proven engagement loops of audio streaming, placing the "Playlist" as the central consumer-facing object of the KoColor ecosystem.

| Streaming Concept | KoColor Translation | Architecture Component |
| --- | --- | --- |
| **Track** | Garment / Cosmetic | `ClothingItemEntity` / `CosmeticItemEntity` |
| **Playlist** | Outfit Sequence | 7-Day Style Playlist |
| **Crossfade** | Garment ↔ Cosmetic Transition | Phenotype-to-Virtual Vanity Engine |
| **Discover Weekly** | 7-Day Style Forecast | Scheduled Batch Generation Loop |
| **Recently Played** | Wear History | V1 `ClothingUsageEntity` |
| **Wrapped** | Style Wrapped | V1 Analytics & Context Aggregation |

---

## 2. Core Architecture: On-Device Phenotype Calibration

To provide flawless personalization before a single garment is uploaded, the application establishes the user's mathematical aesthetic baseline. To guarantee absolute privacy, this relies entirely on local processing rather than cloud infrastructure.

### 2.1 Color & Contrast Calibration (Zero-Cloud)

* **Lighting Validation:** The engine utilizes ambient light sensors and camera exposure metrics to ensure natural lighting, preventing artificial color casting.
* **On-Device Processing:** Using Android CameraX and an on-device ML model, the system extracts color hex codes from the skin surface, iris, and natural hair root. Frames are processed entirely in memory and immediately discarded.

### 2.2 The Classification Pipeline

1. **Undertone & Brightness:** Calculates whether the skin pulls warm (yellow/peach) or cool (blue/pink) and measures overall luminance.
2. **Contrast Delta:** Measures the difference between the skin, eyes, and hair (e.g., pale skin with dark hair = high contrast).
3. **12-Season Matrix Mapping:** These vectors deterministically map the user's Bio Data to a specific seasonal palette (e.g., *Soft Summer*, *Deep Autumn*).

---

## 3. The Killer Feature: The Cosmetic Crossfade

Cosmetics are not treated as a separate, static inventory; they act as the algorithmic bridge between the user's biological phenotype and their chosen garments.

If a user selects a garment that technically violates their color harmony, KoColor does not reject the outfit. Instead, it queries the **Virtual Vanity** to recommend specific makeup shades that artificially compensate for the harmony gap.

**The Crossfade Execution Flow:**

> **Garment** (e.g., Cool Winter Black Dress)
> ↓
> **Color Analysis** (Detects item is high-contrast, cool-toned)
> ↓
> **User Phenotype** (e.g., Soft Autumn Bio Data)
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

KoColor operates a proactive batch-processing engine that transforms meteorological and scheduling data into a cohesive weekly style playlist. It optimizes for outfit quality, weekly diversity, weather safety, schedule context, and wardrobe utilization simultaneously.

### 4.1 The Engine Inputs

1. **Location & Weather:** Ingests a 7-day forecast to establish hard thermal limits and fabric-safety filters.
2. **Calendar (NLP):** Scans device events, translating raw strings into structured context tags (e.g., "Board Review" $\rightarrow$ `[Context: Formal Work]`).
3. **Wardrobe & Cosmetics:** The available inventory pool.

### 4.2 The Sequential Pipeline (Simulated Wear)

The engine prevents the AI from generating seven outfits that look identical by utilizing a simulated state-forwarding loop.

When an item is selected for Day 1, a *simulated usage event* is written into the in-memory rotation state. This immediately enforces the V1 48-hour hard cooldown and frequency penalties across Day 2 and Day 3, guaranteeing natural rotation and diversity across the entire week.

### 4.3 The Playlist Lifecycle (The State Machine)

The playlist is a stateful entity that natively closes the loop between V2 generation and V1 memory.

> `GENERATE` (Engine builds the 7-day forecast)
> ↓
> `PREVIEW` (User reviews the week)
> ↓
> `USER ACCEPTS` (User confirms or pins specific items)
> ↓
> `PLAYLIST LOCKED` (State is saved to Room)
> ↓
> `DAILY OUTFIT` (Contextual routing applied per day)
> ↓
> `USER WEARS` (Action taken)
> ↓
> `COMMIT` (Atomic database transaction)
> ↓
> `ROTATION HISTORY` (V1 `ClothingUsageEntity` updated)
> ↓
> `NEXT PLAYLIST` (Data feeds the next generation)

---

## 5. Integration with V1 Analytics (Style Wrapped)

The rigorous behavioral data tracked by the closed Playlist Lifecycle will be synthesized into highly shareable, viral aesthetic summaries.

* **Wardrobe Heroes (Top Played Tracks):** Visualizes the items with the highest algorithmic rotation and manual selection scores.
* **The Investment Portfolio:** Rebrands standard Cost-Per-Wear (CPW) into a celebration of high-ROI purchases based on actual playlist deployments.
* **Style Eras:** Uses historical Calendar context tags to define the user's shifting aesthetic personas over time (e.g., "Corporate Siren" vs. "Dark Academia").