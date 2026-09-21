# KoColor: Master System Architecture, Taxonomy & Design Specification

> [!NOTE]
> **KoColor** is a privacy-first, on-device Personal Style Operating System built within the ProBase ecosystem. It bridges environmental context, health rituals, chromatic intelligence, digital wardrobe management, and cosmetics formulation science into a unified consumer experience.

---

## 🏛️ 1. Executive System Overview

KoColor operates on the principle that **AI recommendations become significantly more valuable when grounded in structured personal context**. Rather than relying on generic e-commerce catalogs or unconstrained AI prompts, KoColor maintains an offline-first, highly structured representation of what a user owns, wears, applies, and experiences daily.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           ENVIRONMENTAL CONTEXT                         │
│                  (Weather, Temp, UV Index, Humidity, Season)            │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
┌────────────────────────────────────▼────────────────────────────────────┐
│                             GLOW ARCHIVE                                │
│          (Wardrobe Collection + Vanity Cosmetics Inventory)             │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
┌────────────────────────────────────▼────────────────────────────────────┐
│                    3-TIER PROFESSIONAL TAXONOMY                         │
│        (Macro Category ➔ Micro Category ➔ Level 3 Engine Facets)         │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
┌────────────────────────────────────▼────────────────────────────────────┐
│                     ON-DEVICE ORCHESTRATION ENGINES                     │
│  Deterministic Math (CIELAB, Chemistry, Rotation) + AI Creative Synthesis │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
┌────────────────────────────────────▼────────────────────────────────────┐
│                       MODERN GLASSMOPHIC UI LAYER                       │
│    (Standard Cards, Micro-Animation Suite, Interactive Blueprint History) │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🏷️ 2. The 3-Tier Professional Taxonomy System

To balance high-speed user navigation with strict algorithmic precision, KoColor classifies all inventory items across three distinct architectural tiers:

### 🟢 Level 1: Macro Categories (UI Navigation Layer)
Intuitive body-zone "buckets" designed for quick navigation, filtering, and visual organization:

| Domain | Level 1 Macro Category | Body-Zone Mapping / Function |
| :--- | :--- | :--- |
| **Wardrobe** | **Tops** | Strategic foundational upper-body silhouettes (*Blazers, Shirts, Knitwear, Tees*) |
| **Wardrobe** | **Bottoms** | Structural lower-body base elements (*Trousers, Skirts, Denim, Slacks*) |
| **Wardrobe** | **Shoes** | Grounding anchors for the ensemble (*Heels, Flats, Sneakers, Boots, Sandals*) |
| **Wardrobe** | **Dresses** | Standalone full-body silhouettes for high-impact single-piece statements |
| **Wardrobe** | **Outerwear** | Protective and statement layering pieces (*Coats, Jackets, Blazers, Vests*) |
| **Wardrobe** | **Activewear** | High-performance gear engineered for movement and transition |
| **Wardrobe** | **Bags** | Functional luxury carriers for daily essentials (*Handbags, Clutches, Backpacks*) |
| **Wardrobe** | **Hats** | Expressive headwear framing facial geometry (*Caps, Beanies, Fedoras*) |
| **Wardrobe** | **Jewelry** | Precious metallic accents highlighting personal undertones (*Necklaces, Rings, Earrings*) |
| **Wardrobe** | **Accessories** | Curated styling enhancements (*Belts, Scarves, Sunglasses, Gloves*) |
| **Vanity** | **Skincare & Prep** | Canvas preparation prior to pigment application (*Cleansers, Toners, Serums, SPFs, Primers*) |
| **Vanity** | **Complexion (Base)** | Unifying formulas that balance skin tone (*Foundations, Concealers, Setting Powders*) |
| **Vanity** | **Color & Dimension** | Life, shadow, and highlights (*Blushes, Bronzers, Contours, Highlighters*) |
| **Vanity** | **Eyes & Brows** | Upper-face definition (*Eyeshadows, Eyeliners, Mascaras, Brow Gels*) |
| **Vanity** | **Lips** | Color and hydration for the lip zone (*Lipsticks, Glosses, Liners, Tints, Balms*) |
| **Vanity** | **Tools & Hygiene** | Equipment and sanitization (*Brushes, Sponges, Sanitizing Sprays*) |

### 🔵 Level 2: Micro Categories (Product Type)
Specific product format definitions stored directly in database records (`ClothingItemEntity` and `CosmeticItemEntity`) to guarantee clean searchability and catalog indexing.

### 🟣 Level 3: Professional Facets (The Engine Layer)
Expert-grade attributes evaluated by the **Fashionista Engine** and **Chemistry Analyzer** to calculate compatibility and prevent layering conflicts:

* **Cosmetics Chemistry Base**: `Water`, `Silicone`, or `Oil`. *Crucial for preventing pilling during multi-product skin prep!*
* **Cosmetics Finish & Coverage**: `Matte`, `Satin`, `Radiant`, `Metallic` $\times$ `Sheer`, `Light`, `Medium`, `Full`, `Buildable`.
* **Color Temperature & Undertone**: `Warm`, `Cool`, `Neutral`, `Olive` tied to CIELAB $L*a*b*$ color space angles.
* **Wardrobe Formality Rank**: `LOUNGE (1)` $\to$ `GALA (6)`.
* **Garment Chromatic DNA**: Dominant Hex, Vibrant Hex, Muted Hex, Contrast Level, and Seasonal Match.

---

## 🗄️ 3. Database Architecture & History Mechanics

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

## 🎨 4. Modern Glassmorphic UI & Signature Micro-Animation Suite

Every major feature card in KoColor follows a unified **3-Zone Glassmorphic Design Specification**:
1. **Main Frosted Glass Window**: A `RoundedCornerShape(44.dp)` translucent white pill (`Color.White.copy(alpha = 0.35f)` with a `1.dp` `0.6f` white border) housing the section title. Tapping this primary area opens the main detail/management screen.
2. **Floating Action Hero Badge**: A `52.dp` circular badge intersecting the top-right corner of the frosted glass pill, equipped with a feature-specific micro-animation to signal interactivity.
3. **Collapsible Analytics Section (`AnimatedVisibility`)**: An overlapping bottom sub-header row (`offset(y = -14.dp)`) with a rotating chevron arrow (`[ ∨ ]` $\to$ `[ ∧ ]`) that smoothly expands to reveal inline analytics.

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

## 🔒 5. Firebase & Play Integrity Attestation

To safeguard the application's AI endpoints and on-device features without leaking memory, **`AppCheckInitializer`** manages App Check initialization:

```kotlin
object AppCheckInitializer {
    fun initialize(context: Context) {
        val appContext = context.applicationContext

        val isEmulator = Build.FINGERPRINT.contains("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86")

        val providerFactory = if (BuildConfig.DEBUG && isEmulator) {
            DebugAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }

        try {
            Firebase.appCheck.installAppCheckProviderFactory(providerFactory)
        } catch (e: IllegalStateException) {
            // Safely catches duplicate initialization attempts
        }
    }
}
```

* **Physical Device Attestation**: Physical devices (both debug and release builds) enforce cryptographic attestation via **Play Integrity**.
* **Emulator Isolation**: Android emulators safely fall back to `DebugAppCheckProviderFactory` for frictionless local development.
* **Leak Prevention**: Uses `context.applicationContext` to eliminate Activity reference leaks.

---

### Verification & Compliance
* All components across `:applications:kocolor:apps:mobile`, `:applications:kocolor:features:inventory`, `:applications:kocolor:features:cosmetics`, and `:applications:kocolor:features:starterpack` build with **0 errors**.
* Full compliance with **Modern Android Development (MAD)** gold standards: state-driven unidirectional data flow, clean composable file extraction, and explicit parameter contracts (`uiState`, `onEvent`, `navTo`, `modifier`).
