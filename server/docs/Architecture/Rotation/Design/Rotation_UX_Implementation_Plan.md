# Architecture Specification: Rotation Visualization & UX (V1 Locked)

This document outlines the presentation layer for the KoColor Clothing Rotation System, transforming raw database analytics into a premium, responsive user experience.

## 1. Deterministic Freshness States

Garment cards (`WardrobeItemCard`) will display a dynamic status indicator. The system evaluates state in a strict top-down precedence to guarantee no ambiguity:

### 1. RESTING (Plum / Muted Border)
*   **Logic**: `lastUsed < 48h` OR `CategoryShare > 35%`.
*   **UX Meaning**: "You've used me recently—consider something else."
*   **Constraint**: The garment photograph remains fully vibrant and selectable. No grayscale or lock icons.

### 2. FRESH (Cyan / Pulsing Dot)
*   **Logic**: `useCount == 0` OR `lastUsed >= 10 days`.
*   **UX Meaning**: "Use me."
*   **Stylist Note**: "You haven't worn this piece recently. It could be a great way to bring more variety to today's look."

### 3. IN ROTATION (Gold / Solid Border)
*   **Logic**: Default fallback (None of the above apply).
*   **UX Meaning**: "I'm being used regularly."

---

## 2. Dashboard Vanity Metrics

Two independent metrics will reside at the top of the Wardrobe surface:

### The Glow Score (Wardrobe Utilization)
A percentage measuring how much of the owned wardrobe is actually being worn.
*   **Formula**: `(Unique garments with useCount > 0) / (Total garments owned)`
*   **Display**: 0–100% circular progress gauge.

### The Diversity Index (Wardrobe Entropy)
A textual rating measuring how evenly usage is distributed across categories, preventing a user from hiding a stagnant wardrobe behind a high Glow Score. (Mathematical formula deferred to V1.1; base off simple category variance for V1).

### Cooldown Ticker
A horizontal scrolling list showing items currently trapped by the 48-hour hard window, making the penalty tangible.
*   **Format**: `[Item Name] — [Hours Remaining]h` (e.g., *Obsidian Moto Jacket — 31h*).

---

## 3. Domain-Level Architecture Integration

The ViewModel will not map canonical data to personalization data manually. The Repository will expose a joined data class.

```kotlin
// Domain Layer
data class ClothingWithUsage(
    val garment: ClothingItem, // Canonical Source of Truth (Domain Model)
    val usage: ClothingUsageEntity?  // Personalization State
)

// UI Layer (ViewModel)
// Observes the unified stream directly
val uiState: StateFlow<WardrobeUiState> = rotationScoringUseCase
    .observeAllClothingWithUsage()
    .map { WardrobeUiState(items = it) }
    .stateIn(...)
```

---

## 4. The Animation Contract (Truthful UX)

Visual feedback must never lie about database state. The "Data Beam" sync animation and the "Wear Count" badge increments will **only** fire as a callback *after* the Room database transaction completes successfully.

*   **Action**: User taps SAVE.
*   **Process**: Await `repository.commitOutfit(ids)`.
*   **Success**: Trigger Data Beam animation + Heavy Haptic impact.
*   **Failure**: Silently fail (or show a discrete error toast). Do not play success animations.
