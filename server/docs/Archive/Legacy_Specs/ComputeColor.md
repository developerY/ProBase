# KoColor Architecture: Compute-at-Compile-Time Enrichment

## 1. Executive Summary

This specification defines the **Compute-at-Compile-Time** architectural pattern for the KoColor ecosystem. By leveraging the Rust compiler to perform heavy algorithmic evaluations (thermodynamic phase mapping and spatial colorimetry) during `.kpkg` generation, we permanently offload complex processing from the mobile client.

This guarantees that the Jetpack Compose UI and RoomDB entity mapping remain exceptionally lightweight, ensuring zero-latency rendering on the Android Hub while enabling professional-grade, scientific cosmetic calculations.

---

## 2. Architectural Pipeline Injection

This enrichment phase occurs strictly inside the Rust `kocolor-compiler`, after the raw JSON is validated against the KCPS v1 schema, but *before* deterministic serialization and Zstandard compression.

```text
       ┌─────────────────┐
       │   Partner JSON  │ (Raw KCPS v1)
       └────────┬────────┘
                │
                ▼
       ┌─────────────────┐
       │ Strict Validate │ (KCPS v1 Enforcer)
       └────────┬────────┘
                │
                ▼
       ┌─────────────────┐
       │   Derive Math   │ ◄── COMPUTE-AT-COMPILE-TIME INJECTION
       │   & Chemistry   │
       └────────┬────────┘
                │
                ▼
       ┌─────────────────┐
       │ Canonicalize &  │ (Deterministic byte-vector)
       │ Compress (Zstd) │
       └────────┬────────┘

```

By relying on the KCPS v1 **Forward Compatibility Rule** (clients MUST ignore unknown fields), the Rust engine safely injects these computed fields into the payload without requiring immediate schema migrations in the Android client.

---

## 3. Module A: Thermodynamic Phase Resolution

To power the Interfacial Chemistry and Layering Compatibility Engine (e.g., detecting "pilling" risks), the compiler translates standard commercial chemistry bases into explicit physical phase boundaries.

The Rust compiler implements a deterministic mapping of the `chemistry_base` enum to a new `calculated_chemistry_phase` property.

| KCPS v1 `chemistry_base` | Computed Phase Classification | Physical UI Implication |
| --- | --- | --- |
| `WATER` | `HYDROPHILIC_AQUEOUS` | High surface tension mismatch over siloxanes. |
| `SILICONE` | `HYDROPHOBIC_SILOXANE` | Cross-links seamlessly with compatible volatile carriers. |
| `OIL` / `WAX` | `LIPOPHILIC_LIPID` | High solubility risk over un-set hydrophilic bases. |
| `ALCOHOL` | `VOLATILE_SOLVENT` | Requires flash-off delay before subsequent layering. |
| *Null/Unknown* | `UNKNOWN_PHASE` | Triggers neutral fallback in compatibility engine. |

---

## 4. Module B: CIELAB Colorimetry Mathematics

To power algorithmic shade matching, undertone synergies, and temperature filtering, the Rust engine converts standard sRGB `color_hex` strings into the CIELAB ($L^*a^*b^*$) color space using the CIE Standard Illuminant D65.

### 4.1 Hex to Linear RGB

The compiler converts the hex code to normalized $R, G, B$ values in the range $[0.0, 1.0]$. It then applies gamma expansion to yield linear values ($C_{linear}$ for each channel $C$):

$$C_{linear} = \begin{cases} \frac{C}{12.92} & \text{if } C \le 0.04045 \\ \left(\frac{C + 0.055}{1.055}\right)^{2.4} & \text{if } C > 0.04045 \end{cases}$$

### 4.2 Linear RGB to XYZ (D65)

The linear RGB coordinates are transformed into the CIE XYZ color space via the D65 reference matrix:

$$X = R_{linear} \times 0.4124 + G_{linear} \times 0.3576 + B_{linear} \times 0.1805$$

$$Y = R_{linear} \times 0.2126 + G_{linear} \times 0.7152 + B_{linear} \times 0.0722$$

$$Z = R_{linear} \times 0.0193 + G_{linear} \times 0.1192 + B_{linear} \times 0.9505$$

### 4.3 XYZ to CIELAB ($L^*a^*b^*$)

Using the D65 reference white points ($X_n = 0.95047$, $Y_n = 1.00000$, $Z_n = 1.08883$), the compiler evaluates the function $f(t)$:

$$f(t) = \begin{cases} t^{1/3} & \text{if } t > 0.008856 \\ 7.787 \times t + \frac{16}{116} & \text{if } t \le 0.008856 \end{cases}$$

The final CIELAB coordinates and hue angle ($h_{ab}$) are computed as:

$$L^* = 116 \times f\left(\frac{Y}{Y_n}\right) - 16$$

$$a^* = 500 \times \left( f\left(\frac{X}{X_n}\right) - f\left(\frac{Y}{Y_n}\right) \right)$$

$$b^* = 200 \times \left( f\left(\frac{Y}{Y_n}\right) - f\left(\frac{Z}{Z_n}\right) \right)$$

$$h_{ab} = \arctan\left(\frac{b^*}{a^*}\right) \times \left(\frac{180}{\pi}\right)$$

---

## 5. Canonical Payload Contract

After the Rust compiler executes the derivation phase, it silently injects the results into the canonical JSON string. The downstream Android application receives a heavily enriched data structure without any effort from the B2B partner providing the original JSON.

**Example Injected Output:**

```json
{
  "id": "kc-starter-lips-01",
  "name": "Signature Crimson Lip Color",
  "macro_category": "LIPS",
  "micro_category": "LIPSTICK",
  "color_hex": "#A81C28",
  "chemistry_base": "OIL",
  
  "calculated_chemistry_phase": "LIPOPHILIC_LIPID",
  "calculated_cielab": {
    "l": 38.5,
    "a": 52.1,
    "b": 24.3,
    "hue_angle_hab": 25.0
  }
}

```

## 6. Implementation Constraints

1. **Immutability:** The derived fields must only be written by the Rust compiler. Android clients treat these fields as read-only.
2. **Schema Ignorance:** Because these fields are not part of the strict KCPS v1 input contract, the validation step (`serde_json::from_str`) must not fail if these fields are absent in the source JSON. They are strictly output projections.
3. **Opt-In Client Parsing:** The Android Hub developers can update their RoomDB entities to parse `calculated_cielab` at their discretion, without breaking the existing Version 1 ingestion pipeline.