# Unified Style Command Center: The KoColor V2 Architecture

This document outlines the convergent architecture of the KoColor Style Simulator, explaining how biometric data, user constraints, and external context flow through a single funnel to generate both immediate and long-term fashion intelligence.

At its core, this system operates on a fundamental invariant: **KoColor may predict behavior, but it never records predicted behavior as historical fact.**

---

## 1. System Architecture Flow

```text
                   COMMAND CENTER
                         │
          ┌──────────────┼──────────────┐
          │              │              │
     IDENTITY         ANCHORS         INTENT
          │              │              │
          └──────────────┼──────────────┘
                         │
                CONTEXT ENRICHMENT
                         │
       ┌─────────────────┼─────────────────┐
       │                 │                 │
    Calendar          Weather          Location
       │                 │                 │
       └─────────────────┼─────────────────┘
                         │
                    STYLE ENGINE
                         │
              ┌──────────┴──────────┐
              │                     │
         SINGLE ADVICE          PLAYLIST
              │                     │
              │                 7 DAYS
              │                     │
              └──────────┬──────────┘
                         ▼
                 STYLE COLLECTION
                         │
                    USER WEARS
                         │
                      COMMIT
                         │
                  V1 ROTATION MEMORY
                         │
                         └──────────────►
                              STYLE ENGINE

```

---

## 2. The Core Funnel: Three User-Facing Inputs, Enriched by Context

We have moved away from a distributed feature model to a **Convergent Command Center**. The UI exposes three explicit user-facing inputs, which the engine internally enriches with a broader contextual stream (Wardrobe, Cosmetics, Location, Weather, and Rotation History).

1. **Visual Identity (Edge AI)**:
* **Action**: The user captures or picks a portrait.
* **Processing**: On-device ML Kit Face Detection samples skin, hair, and eye luminance.
* **Result**: Establishes the user's "Mathematical Season" (e.g., True Winter) locally, saved to the `FashionProfile` in Room.


2. **User Anchors (Hard Constraints)**:
* **Action**: The user explicitly locks certain items (e.g., "I must wear my Blue Silk Blazer today").
* **Processing**: These anchors *do not* bypass the AI. Instead, they constrain the AI's selection space and become mandatory inputs to the outfit solver. The engine still optimizes what shirt, pants, shoes, and cosmetics perfectly complete the blazer based on weather and rotation constraints.


3. **Intent**:
* **Action**: The user types their intent (e.g., "Boardroom presentation").
* **Future Integration**: The Calendar icon will automatically pull upcoming events to populate this intent.



---

## 3. The Branching Engine: Single vs. 7-Day

Once the inputs are set, the architecture forks into two distinct logic paths using the `StyleSimulatorEngine`.

### **Path A: Single Fashion Advice (Immediate)**

* **Trigger**: "Get Fashion Advice" button.
* **Process**: The `StyleSimulatorViewModel` calls the Engine once.
* **Output**: A `StyleBlueprint` with one outfit, one palette, and one rationale.

### **Path B: Style Playlist (7-Day Forecast)**

* **Trigger**: "Generate 7-Day Playlist" button.
* **Process**: The `GeneratePlaylistUseCase` runs a **7-iteration loop** through the Engine, heavily relying on the `ProjectedRotationState`.
* **State Forwarding**:
* Simulated wear is *never* written to the V1 `ClothingUsageEntity`.
* **Day 1** is generated -> written to in-memory `Projected Wear`.
* **Day 2** is generated (penalizing Day 1's items) -> written to `Projected Wear`.
* This loops through **Day 7** to guarantee a varied week without corrupting historical data.



---

## 4. The Unified Sink: The Style Collection Tab

Both results converge in the **Collection Tab**. This is not a passive archive; it is the user's active **Style Command Center and Playback Surface**.

1. **The Active Playlist (Top Card)**:
* Provides a persistent summary of the current 7-day forecast.
* **Execution Semantics**: `PREVIEW` → `ACCEPT / LOCK` → `I'M WEARING THIS` → `COMMITTED ROTATION EVENT`.
* **Idempotency**: When the user taps "I'M WEARING THIS", an atomic, idempotent transaction increments the wear count (`useCount + 1`) in the V1 Closet and sets the 48-hour cooldown. Double-taps cannot corrupt the ledger.


2. **Historical Playback**:
* Houses past Curated Looks, Remixes, rotation history, and future "Style Wrapped" metrics.



---

## 5. Architectural Invariants: The Three States

To keep the system robust across V1 and V2, the architecture strictly isolates three types of state:

| State | Meaning | Persistence |
| --- | --- | --- |
| **Context State** | What is true right now / external conditions (Weather, Location, Calendar). | Current Live Data |
| **Projected State** | What the engine *predicts* will happen over the next 7 days. | Temporary / In-Memory |
| **Committed State** | What the user *actually* did (historical fact). | V1 Room Database (`ClothingUsageEntity`) |

---

## 6. User Journey Walkthrough

1. **Open "Analyze Style"**: User enters the Command Center.
2. **Identity**: User taps the portrait card and takes a photo. The app confirms "True Winter Established."
3. **Anchors**: User selects "Top" and taps the "Blue Silk" family. The engine accepts this as a mandatory constraint for the generation solver.
4. **Intent**: User taps the Calendar icon. The AI reads "Meeting with CEO at 2 PM."
5. **Action**: User clicks **"Generate 7-Day Playlist"**.
6. **Review**: The user is navigated to the **Collection Tab** to preview their upcoming week, with Monday perfectly optimized around the anchor and the CEO meeting.
7. **Commitment**: Monday morning, user taps **"I'M WEARING THIS"**. The idempotent transaction fires, the items are marked worn, and they are temporarily removed from the AI's rotation for the next 48 hours.
