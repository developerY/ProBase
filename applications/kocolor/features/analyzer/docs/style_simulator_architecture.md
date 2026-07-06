# Architecture: KoColor Style Simulator Engine

The **Style Simulator** is a high-fidelity decision engine that merges a user's biological state with their physical wardrobe. Unlike generic fashion assistants, this engine is **context-aware**, **offline-resilient**, and **anchored in reality**.

## 1. The Contextual Inputs (The "Why")

Before the simulation begins, the `StyleSimulatorViewModel` aggregates three layers of high-signal data to create a "Biological Anchor":

| Marker | Data Source | Impact on Simulation |
| :--- | :--- | :--- |
| **Circadian Context** | System Clock | Adjusts logic for "Defense & Protection" (Morning) vs "Recovery" (Night). |
| **Routine Status** | `RoutineDao` | Informs the AI if the user has completed their morning ritual. |
| **Wellness Score** | Health Core | A numerical representation of the user's overall physical readiness. |

## 2. The Wardrobe Sync (The "What")

The engine performs a real-time sync with the `WardrobeRepository`. It serializes the user's physical collection into a structured "Vault Manifest":
- **IDs**: For deterministic re-mapping after the simulation.
- **Categories**: Ensuring a valid outfit (Top, Bottom, Shoes).
- **Color Identity**: Raw HEX codes used to calculate harmonic palettes.

## 3. Two-Tier Processing (The "How")

The [StyleSimulatorEngine](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/data/StyleSimulatorEngine.kt) follows a **Deterministic-to-Probabilistic** strategy.

### Tier 1: Probabilistic (Cloud Gemini)
When an API key is present, the engine tasks **Gemini 1.5 Flash** to act as a "Style Architect."
1.  **Selection**: Picks exactly 3 core items (Top, Bottom, Shoes) from the manifest.
2.  **Harmonization**: Recommends 2 accessories and generates a 3-color harmonic palette.
3.  **Synthesis**: Writes a brief **Stylistic Rationale** connecting the user's "Intent" (e.g., "crisp look for a negotiation") with the chosen items.

### Tier 2: Deterministic (Local Heuristics)
If the device is offline or the NPU is bypassed, the engine switches to a local **Keyword Matcher**.
- It uses string-distance algorithms to match the "User Intent" against item names and notes.
- It builds a palette derived from the dominant colors of the matched items.

## 4. Logical Persistence & Output

The simulation result is not transient. When a user clicks **"Add to Collection"**, the following occurs:

- **Object Mapping**: The simulation's IDs are used to pull full `ClothingItem` objects from the database.
- **Advice Synthesis**: A full `FashionAdvice` object is generated, including specialized makeup suggestions (e.g., "brick red lip to anchor the look").
- **Room 3 Storage**: The final blueprint is saved with a `ServiceStatus.SUCCESS` flag, making it a permanent part of the user's style history.

---
**Status:** ✅ **HIGH-SIGNAL**
**Compliance:** Zero-Footprint, Context-Aware, and MAD 2026 Compatible.
