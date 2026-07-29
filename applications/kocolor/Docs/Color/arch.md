# Modular Layered Architecture

The separation between these two modules follows a **Modular Layered Architecture**, designed to keep the core **intelligence** independent from any specific **platform UI**.

The architecture cleanly separates **what the application knows** from **how the user interacts with it**.

---

# 1. `:applications:kocolor:features:colors`
## The Domain & Intelligence Engine

This is a **platform-agnostic** module.

It contains the core science, business rules, and intelligence that power KoColor.

### Role

**The Brain**

This module performs all of the technical and analytical work.

### Responsibilities

#### Color Science

Implements the mathematical foundation for color analysis, including:

- CIELAB
- HSV
- Euclidean distance
- Color conversions
- Color similarity calculations

Example:

```text
ColorScienceUtils
```

---

#### Intelligence Engines

Contains the application's decision-making components.

Examples include:

- `StylistEditEngine`
- `WellnessAdvisor`

These engines transform raw color information into personalized recommendations.

---

#### Data Aggregation

Provides a unified view of color information by combining multiple data sources.

Example:

```text
ColorIntelligenceRepository
```

Responsibilities include:

- Wardrobe integration
- Vanity integration
- Cosmetic inventory
- Combined color analytics

---

#### Domain Models

Defines the application's core data structures.

Examples include:

- `ColorInfo`
- `PantoneMatch`
- `WellnessInsight`

These models remain independent of any UI framework.

---

## Why This Module Exists Separately

This module has **no dependency on Jetpack Compose or Android UI components**.

Because it contains only business logic, it can be reused across multiple platforms without modification.

Examples include:

- Android phones
- Wear OS
- Android XR
- Smart displays
- Future desktop or web clients

In short:

> **One Color Brain. Multiple User Experiences.**

---

# 2. `:applications:kocolor:apps:mobile:features:color`

## The Mobile User Interface

This module provides the Android phone implementation of the Color feature.

### Role

**The Face**

Its responsibility is presenting information to users and handling interaction.

---

### Responsibilities

#### Compose Screens

Examples include:

- `ColorHubScreen`
- `ColorSearchScreen`
- `ChromaticDnaBar`

These screens visualize the information produced by the engine.

---

#### State Management

ViewModels connect the UI to the domain layer.

Example:

```text
ColorHubViewModel
```

Responsibilities include:

- Loading data
- Exposing UI state
- Responding to user interactions
- Calling repositories

---

#### User Interface Assets

Contains mobile-specific resources such as:

- Compose layouts
- Animations
- Navigation
- Material components
- Touch interactions

These concerns remain isolated from the intelligence layer.

---

## Dependency Direction

The architecture follows a one-way dependency flow.

```text
Mobile UI
      │
      ▼
Features Engine
      │
      ▼
Repositories
      │
      ▼
Color Science
```

The UI depends on the engine.

The engine never depends on the UI.

---

# Benefits of the Split

## Platform Independence

The color intelligence is written once and reused everywhere.

For example:

```text
                 Color Engine
                      │
      ┌───────────────┼────────────────┐
      │               │                │
      ▼               ▼                ▼
 Android Phone     Wear OS        Android XR
      │               │                │
      └───────────────┼────────────────┘
                      ▼
            Shared Intelligence
```

---

## Code Reuse

Future platforms can immediately leverage:

- Color science
- Stylist recommendations
- Wellness analysis
- Gap analysis
- Palette generation
- Cosmetic intelligence

without rewriting any business logic.

---

## Easier Testing

Business logic can be unit tested independently of the Android framework.

This enables:

- Faster tests
- Higher coverage
- Better reliability

---

## Cleaner Architecture

The separation of concerns makes the project easier to understand and maintain.

Each layer has a single responsibility:

- **Engine:** Makes decisions.
- **Repository:** Provides data.
- **ViewModel:** Coordinates state.
- **Compose UI:** Displays information.

---

## Long-Term Scalability

This architecture is particularly valuable for KoColor because the application is expected to span multiple device categories.

Potential future clients include:

- Android phones
- Tablets
- Foldables
- Wear OS watches
- Android XR glasses
- Automotive displays

Each platform can build its own optimized user experience while sharing the exact same intelligence layer.

---

# Architectural Summary

```text
                    User
                     │
                     ▼
          Mobile Compose UI
                     │
                     ▼
            ViewModel Layer
                     │
                     ▼
      Color Intelligence Repository
                     │
                     ▼
      StylistEditEngine
      WellnessAdvisor
      ColorScienceUtils
                     │
                     ▼
           Domain Models
```

---

# Conclusion

Separating the project into a **platform-agnostic intelligence engine** and **platform-specific UI modules** provides a highly scalable architecture.

The `:features:colors` module becomes the single source of truth for all color intelligence, while each platform—mobile, Wear OS, Android XR, or future devices—implements its own presentation layer.

This design follows a simple principle:

> **Write the intelligence once. Deliver it everywhere.**