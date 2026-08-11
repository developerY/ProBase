# Implementation Plan: Interfacial Chemistry Engine & Pilling Warnings

This document outlines the implementation of the **Interfacial Chemistry Engine**, a high-performance rules engine that uses pre-calculated thermodynamic phases to warn users about product layering risks (pilling).

---

## 🏗️ 1. Scientific Data Model

We will map the `calculated_chemistry_phase` strings from the KCPS wire payload into a type-safe Kotlin enumeration.

### `ChemistryPhase` Enum
*   `HYDROPHILIC_AQUEOUS`: Water-based products.
*   `HYDROPHOBIC_SILOXANE`: Silicone-based products.
*   `LIPOPHILIC_LIPID`: Oil/Wax-based products.
*   `ANHYDROUS_POWDER`: Dry powder products.
*   `UNKNOWN`: Fallback for non-enriched data.

---

## 🧪 2. The Pilling Rules Engine

The logic will reside in a specialized utility object, ensuring microsecond execution times.

### Layering Logic Matrix:
| Base Layer | New Layer | Result | Recommendation |
| :--- | :--- | :--- | :--- |
| **Silicone** | **Water** | ⚠️ **Pilling Risk** | "Apply water-based first." |
| **Oil** | **Water** | ⚠️ **Pilling Risk** | "Oil repels water." |
| **Powder** | **Liquid/Cream** | ⚠️ **Pilling Risk** | "Liquid over powder creates mud." |
| **Same Phase** | **Same Phase** | ✅ **Optimal** | Seamless blending. |

---

## 🎨 3. UI Integration (Jetpack Compose)

We will build a high-fidelity **`CompatibilityBadge`** component for the Routine Builder and Product Detail screens.

### Features:
*   **Dynamic Visibility**: Only appears when a "High Risk" pairing is detected.
*   **Luxury Aesthetic**: Subdued warning colors (soft amber/red) to maintain a premium feel.
*   **Instructional**: Provides a clear resolution (e.g., "Swap application order").

---

## 🛠️ Implementation Checklist

- [x] **Step 1: Core Models**
    - Defined `ChemistryPhase` in `:core:model`.
- [x] **Step 2: Engine Implementation**
    - Built `ChemistryCompatibilityEngine` in `:core:util`.
- [x] **Step 3: Domain Mapping**
    - Updated `CosmeticItem` domain model to expose the enum version of the phase.
- [x] **Step 4: UI Component**
    - Created the `CompatibilityBadge` in `:core:ui`.
- [x] **Step 5: Feature Activation**
    - Integrated the engine into the **Routine Builder** and **Cosmetic Detail** screens.

---
**Status**: ✅ **V1 ENGINE AWAKENED**
**Compute Strategy**: 0ms Device Compute (CCT Dependent)
