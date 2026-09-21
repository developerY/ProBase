# KoColor: Master System Architecture, Taxonomy & Design Specification

> [!NOTE]
> **KoColor** is a privacy-first, on-device-first Personal Style Operating System built within the ProBase ecosystem. KoColor performs personal data processing locally whenever practical, with controlled cloud AI available only where required by capability, configuration, or user choice.

---

## 🏛️ 1. Executive System Overview & Umbrella Architecture

At the center of KoColor is the **Collection Hub**, which acts as the master umbrella architecture uniting fashion collections, beauty inventories, environmental context, and personal style intelligence:

```
                               COLLECTION HUB
                                ├── FASHION ARCHIVE
                                └── COSMETICS VAULT

┌─────────────────────────────────────────────────────────────────────────┐
│                           REAL-TIME CONTEXT                             │
│                  (Weather, Temp, UV Index, Humidity, Season)            │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
┌────────────────────────────────────▼────────────────────────────────────┐
│                    FOOTPRINT & BEHAVIORAL MEMORY                        │
│          (Wardrobe Footprint + Rotation Velocity + Wear History)        │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
┌────────────────────────────────────▼────────────────────────────────────┐
│                  2-LEVEL TAXONOMY + ENGINE FACETS                       │
│        (Macro Category ➔ Micro Category + Orthogonal Facets)            │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
┌────────────────────────────────────▼────────────────────────────────────┐
│                   RECOMMENDATION ENGINE CONTRACT                        │
│  Deterministic Filter & Score ➔ Canonical Composition ➔ AI Synthesis  │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
┌────────────────────────────────────▼────────────────────────────────────┐
│                     EVALUATION & DUAL VISUAL MODES                      │
│    FASHIONISTA Evaluation + Intent Fulfillment ➔ Style Journey          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## ⚙️ 2. Core Recommendation Engine Contract

The central intelligence flow in KoColor operates on a clear separation of concerns between recommendation generation, aesthetic evaluation, and intent fulfillment:

```text
REAL-TIME CONTEXT
      +
FOOTPRINT
      +
BEHAVIOR
      +
USER INTENT
      ↓
DETERMINISTIC FILTERING
      ↓
DETERMINISTIC SCORING
      ↓
CANONICAL RECOMMENDATION COMPOSITION
      ↓
AI CREATIVE SYNTHESIS
      ↓
DETERMINISTIC VALIDATION
      ↓
VALIDATED RECOMMENDATION
      ↓
FASHIONISTA EVALUATION + INTENT FULFILLMENT
      ↓
STYLE JOURNEY
```

### Key Engine Separations:
1. **RECOMMENDATION ENGINE**: What ensemble should be created? *(Calculates thermal limits, color harmony, and composition candidates)*.
2. **FASHIONISTA**: How aesthetically successful is this ensemble? *(Evaluates silhouette balance, color contrast, and style coordination independently of selection)*.
3. **INTENT FULFILLMENT**: How well does this ensemble satisfy what the user specifically asked for? *(Measures event/occasion appropriateness and requested dress code constraints)*.

---

## 🏷️ 3. The 2-Level Professional Taxonomy + Engine Facets

To balance high-speed user navigation with strict algorithmic precision, KoColor organizes items using two true taxonomy levels combined with orthogonal engine facets:

### 🟢 Level 1: Macro Categories (UI Navigation Layer)
Intuitive user-facing macro categories designed for navigation, filtering, collection organization, and high-level recommendation roles:

| Umbrella Subsystem | Level 1 Macro Category | High-Level Recommendation Role |
| :--- | :--- | :--- |
| **Fashion Archive** | **Tops** | Foundational upper-body silhouettes (*Blazers, Shirts, Knitwear, Tees*) |
| **Fashion Archive** | **Bottoms** | Structural lower-body base elements (*Trousers, Skirts, Denim, Slacks*) |
| **Fashion Archive** | **Shoes** | Grounding anchors for the ensemble (*Heels, Flats, Sneakers, Boots, Sandals*) |
| **Fashion Archive** | **Dresses** | Standalone full-body silhouettes for high-impact single-piece statements |
| **Fashion Archive** | **Outerwear** | Protective and statement layering pieces (*Coats, Jackets, Blazers, Vests*) |
| **Fashion Archive** | **Activewear** | High-performance gear engineered for movement and transition |
| **Fashion Archive** | **Bags** | Functional luxury carriers for daily essentials (*Handbags, Clutches, Backpacks*) |
| **Fashion Archive** | **Hats** | Expressive headwear framing facial geometry (*Caps, Beanies, Fedoras*) |
| **Fashion Archive** | **Jewelry** | Precious metallic accents highlighting personal undertones (*Necklaces, Rings, Earrings*) |
| **Fashion Archive** | **Accessories** | Curated styling enhancements (*Belts, Scarves, Sunglasses, Gloves*) |
| **Cosmetics Vault** | **Skincare & Prep** | Canvas preparation prior to pigment application (*Cleansers, Toners, Serums, SPFs, Primers*) |
| **Cosmetics Vault** | **Complexion (Base)** | Unifying formulas that balance skin tone (*Foundations, Concealers, Setting Powders*) |
| **Cosmetics Vault** | **Color & Dimension** | Life, shadow, and highlights (*Blushes, Bronzers, Contours, Highlighters*) |
| **Cosmetics Vault** | **Eyes & Brows** | Upper-face definition (*Eyeshadows, Eyeliners, Mascaras, Brow Gels*) |
| **Cosmetics Vault** | **Lips** | Color and hydration for the lip zone (*Lipsticks, Glosses, Liners, Tints, Balms*) |
| **Cosmetics Vault** | **Tools & Hygiene** | Equipment and sanitization (*Brushes, Sponges, Sanitizing Sprays*) |

### 🔵 Level 2: Micro Categories (Product Type)
Specific product format definitions stored directly in database records (`ClothingItemEntity` and `CosmeticItemEntity`) to guarantee clean searchability and catalog indexing.

### 🟣 Orthogonal Engine Facets (The Engine Layer)
Expert-grade attributes evaluated by the **Fashionista Engine** and **Chemistry Analyzer** to calculate compatibility and prevent layering conflicts:

* **Cosmetic Formulation Facets**:
  * Formulation system (Liquid, Cream, Powder, Gel, Balm)
  * Water / silicone / oil phase characteristics
  * Emulsion type where known
  * Film-forming characteristics
  * Occlusivity / absorption behavior where available
  * Layering compatibility interaction flags *(computed to the extent supported by available formula/ingredient data)*
* **Color Temperature & Undertone**: Warm, Cool, Neutral, Olive, derived from calibrated color measurements using CIELAB/L\*C\*h° features and domain-specific classification rules.
* **Wardrobe Formality Rank**: `LOUNGE (1)` $\to$ `GALA (6)`.
* **Garment Chromatic DNA**: Dominant Hex, Vibrant Hex, Muted Hex, Contrast Level, and Seasonal Match.

---

## 🗄️ 4. Database Architecture & History Mechanics

### Room Database (`KoColorDatabase`)
* **Protocol Lock**: Standardized to `version = 1` with `fallbackToDestructiveMigration()`.
* **Entities**: `ClothingItemEntity`, `CosmeticItemEntity`, `SavedSuggestionEntity`, `FashionProfileEntity`, `RoutineEntity`, `StylePlaylistEntity`, `DailyStylePlanEntity`.

> [!IMPORTANT]
> ### Prediction History vs. Pinned Collections
> To keep the user's workspace clutter-free while preserving exploratory AI predictions, `SavedSuggestionEntity` handles prediction lifecycle management via the `isSavedToCollection` flag:
> 
> 1. **Unsaved AI Predictions (`isSavedToCollection = false`)**:
>    * Automatically generated during style analysis (`analyzePhotos()` / `analyzeStyle()`).
>    * Stored under **BLUEPRINT HISTORY** on the Home tab.
>    * Strictly capped at **7 entries** (`trimUnsavedSuggestions()`). The 8th oldest unsaved prediction is automatically purged.
> 
> 2. **Saved Collections (`isSavedToCollection = true`)**:
>    * Created when the user explicitly clicks the **"SAVE"** button on an AI prediction.
>    * Persisted permanently inside the **Collection Tab (Second Tab)** under **PAST ANALYSES**.
>    * Immune to automatic purging.

---

## 🎨 5. Dual Visual Modes & Signature Micro-Animation Suite

KoColor defines **two intentional visual modes** within its design system:

1. **KoColor Glassmorphic Interaction System**: Primary dashboard and collection surfaces use standard 3-zone glassmorphic cards (Frosted Glass Pill + Floating Hero Badge + Collapsible Inline Analytics).
2. **KoColor Editorial Design System**: High-fidelity editorial experiences such as Style Journey use refined typography, generous whitespace, fine dividers, and luxury-magazine presentation.

```
┌─────────────────────────────────────────────────────────────────────────┐
│  GLASSMORPHIC CARD CONTAINER (RoundedCornerShape 32.dp)                 │
│                                                                         │
│   ┌──────────────────────────────────────────────────┐  ┌───────────┐  │
│   │  MAIN FROSTED GLASS PILL (Shape 44.dp)           │  │ FLOATING  │  │
│   │  • Section Title & Subtitle Badge                │  │ HERO BADGE│  │
│   │  • Tapping navigates to main screen              │  │ (Pulsing /│  │
│   └──────────────────────────────────────────────────┘  │ Animated) │  │
│                                                         └───────────┘  │
│             ┌──────────────────────────────────────────────┐            │
│             │  SUB-HEADER ROW (e.g., "54 PIECES TRACKED")  │            │
│             │  • Clickable Chevron [ ∨ ] Toggles Expand    │            │
│             └──────────────────────────────────────────────┘            │
│   ┌─────────────────────────────────────────────────────────────────┐   │
│   │  COLLAPSIBLE ANALYTICS PANEL (AnimatedVisibility)               │   │
│   │  • Harmony Scores, Spectrum Distributions, & Detailed Metrics   │   │
│   └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

### 🌟 Signature Micro-Animation Suite

| Card Component | Hero Badge Icon | Micro-Animation Engine | Interaction Purpose |
| :--- | :--- | :--- | :--- |
| **Weather Header** | `UV Index` Disc | **Golden Sun-Glint Shimmer Sheen** (`linearGradient` with `#FFD700` sweeping across every 4000ms) | Signals live meteorological updates & opens Sun Intelligence |
| **Bio-Markers Card** | `WaterDrop` 💧 | **Breathing Icon Pulse + Expanding Water Ripple** (`scale` `1.0` $\to$ `1.15` + fading `0.9` $\to$ `1.4` scale ripple ring) | Invites tapping to log daily hydration & health metrics |
| **Rituals Card** | `Layers` 🧘 | **12° Zen Icon Rotation Sway + Breathing Lavender Aura** (`rotationZ` `-12°` $\to$ `+12°` + `#8B5A82` aura scale) | Represents active routine balance & opens Ritual Management |
| **Collection Hub Card** | `AutoAwesome` ✨ | **Gold/Black Twinkle Scale Pulse + Prismatic Rainbow Aura** (`#D4AF37` $\leftrightarrow$ `#1C1B1F` color loop + `#FFD700`/`#E91E63`/`#8E24AA`/`#00BCD4` sweep aura) | Highlights the central Personal Style AI Simulator |
| **Wardrobe & Vanity Intel** | `Palette` 🎨 | **Endless Color-Pulsing Palette Badge** (Smooth `#6A1B9A` Purple $\leftrightarrow$ `#1A1A1A` Black tint loop) | Prompts exploration of the Chromatic Blueprint & Color DNA |
| **Active Rotation Behavior** | `Sync` ↻ | **Intermittent Pause-and-Spin Rotation** (`360°` rotation with `FastOutSlowInEasing` snappy spin & pause) | Draws focus to wardrobe circulation velocity & rotation health |
| **Vanity Inventory Card** | `Warning` ⚠️ | **Pulsing Expiration Badge** (`1.0` $\to$ `1.25` scale pulse when `expiringCount > 0`) | Alerts the user to PAO expired or low-volume cosmetics |

---

## 🔒 6. Firebase & Play Integrity Attestation

To safeguard the application's AI endpoints and on-device features without retaining Activity references:

* **Lifecycle Safety**: Uses `context.applicationContext` so Firebase initialization does not retain an Activity context.
* **Environment Provider Configuration**:
  ```text
  DEBUG (Emulator) ➔ Debug App Check Provider
  RELEASE / Physical Devices ➔ Play Integrity App Check Provider
  ```

---

## 📊 7. Verification Status

```text
• Current build: PASS
• Unit tests: PASS
• UI tests: PASS
• Static analysis: PASS
• Architecture checks: PASS
```

> **Architecture Alignment**: Architecture is fully aligned with Modern Android Development practices, including state-driven UI, unidirectional data flow, modularization, and explicit composable contracts (`uiState`, `onEvent`, `navTo`, `modifier`).
