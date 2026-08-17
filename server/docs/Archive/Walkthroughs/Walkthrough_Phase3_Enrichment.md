# Walkthrough: Phase 3 Enrichment Pipeline (Actives, Unit Price & Search)

This document details Phase 3 of the "Compute at Compile Time" strategy. We have offloaded ingredient extraction, price normalization, and search tokenization to the Rust compiler.

---

## 🧪 1. Active Ingredient Extraction

**The Challenge**: Identifying key "hero" ingredients (like Retinol or Niacinamide) from a long list of technical INCI names is difficult for users and requires heavy processing on the device.

**The Rust Solution**:
The compiler now scans the `ingredients` array against a professional dictionary of known "hero" actives and injects a clean, pre-parsed list.

*   **Logic**: `engine_enrichment::get_hero_actives`
*   **Storage**: Injected as `calculated_hero_actives` array.
*   **Android Benefit**: The app can instantly show "Key Actives" badges without parsing thousands of strings on the fly.

---

## 💰 2. Unit Price Normalization

**The Challenge**: Comparing the value of products (e.g., Price per 10ml) is hard when volume strings are inconsistent (e.g., "30ml", "1.0 oz").

**The Rust Solution**:
The compiler parses the `volume` string, extracts the numeric value, and divides the `price` by it to compute a standardized price-per-unit.

*   **Logic**: `engine_enrichment::calculate_unit_price`
*   **Storage**: Injected as `calculated_unit_price` (Double).
*   **Android Benefit**: The boutique can now sort by "Best Value" instantly.

---

## 🔍 3. Search Tokenization

**The Challenge**: Typo-tolerant and fast search in SQLite can be slow if done with complex regex or `LIKE` queries on every keystroke.

**The Rust Solution**:
The compiler generates a flat array of search tokens (alphanumeric words from the name and brand) during the build phase.

*   **Logic**: `engine_enrichment::generate_search_tokens`
*   **Storage**: Injected as `calculated_search_tokens` array.
*   **Android Benefit**: RoomDB can perform simple indexed array matches, making the search experience feel instantaneous.

---

## 🏗️ 4. Execution & Integration

1.  **Enrichment Interception**: The `kocolor-compiler` mutably iterates through all cosmetics and applies these Phase 3 enrichments alongside Chemistry, CIELAB, and BlurHash.
2.  **Strict Integrity**: Every enriched field is signed into the `.kpkg` binary, ensuring that these "Intelligence" layers are as secure as the raw product data.

---
**Status**: ✅ **PHASE 3 DEPLOYED**
**Compiler**: `kocolor-compiler 1.7.0`
**Enrichment Engine**: Rust (Actives/Price/Search)
