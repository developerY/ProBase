# Wardrobe Color Engine: Local Stylistic Intelligence

The **Wardrobe Color Engine** is a local analytical framework designed to extract dominant color signatures from garment imagery. Unlike traditional AI-heavy solutions, this engine processes data entirely on-device, providing deterministic, privacy-focused fashion intelligence that powers the KoColor styling ecosystem.

---

## 1. Technical Vision

The engine serves as the analytical foundation for **Makeup-to-Wardrobe Coordination**. By standardizing how garment colors are identified and stored, we enable:
- **Offline Precision**: Professional-grade color matching without cloud latency or costs.
- **Data-Driven Harmony**: Accurate seasonal palette mapping (e.g., "Deep Winter" vs. "Warm Autumn").
- **Privacy-First Stylings**: All image analysis remains on the user's secure device.

---

## 2. Multi-Stage Processing Pipeline

The framework utilizes a three-stage pipeline to transform raw pixels into fashion signatures.

### Stage 1: Pre-Processing & Normalization
Incoming garment images are standardized to ensure consistent extraction.
- **Memory Optimization**: Efficient bitmap resizing to prevent OOM errors during analysis.
- **Normalization**: Standardizing lighting and contrast conditions to improve downstream accuracy.

### Stage 2: Palette Extraction (Android Palette API)
The engine leverages the native Android Palette API to identify visual weights.
- **Dominant Detection**: Identifying the primary color that defines the garment's visual identity.
- **Vibrant & Muted Tones**: Extracting secondary colors to build a complex "Signature Palette."
- **Swatch Population**: Weighting colors based on their pixel density within the frame.

### Stage 3: Signature Generation & Semantic Mapping
Raw HEX values are transformed into structured fashion intelligence.
- **Color Temperature Heuristics**: Algorithms calculate the "Warmth" vs. "Coolness" of extracted tones.
- **Seasonal Type Mapping**: Dominant colors are mapped to semantic KoColor groups:
    - **Spring**: High lightness, high saturation.
    - **Summer**: Cool temperature, muted values.
    - **Autumn**: Warm temperature, earthy/muted values.
    - **Winter**: High contrast, cool temperature, deep values.

---

## 3. Data Model & Architecture

The analytical results are persisted within the expanded `ClothingItem` architecture:

```kotlin
data class ClothingItem(
    // Visual Signatures
    val dominantHex: String?,
    val paletteHexes: List<String>,
    
    // Semantic Metadata
    val colorTemperature: String?, // WARM, COOL, NEUTRAL
    val seasonalPalette: String?   // SPRING, SUMMER, AUTUMN, WINTER
)
```

---

## 4. Future Integration: The Comprehensive Look

The Wardrobe Color Engine is the primary data provider for the **Comprehensive Look Builder**. This upcoming feature will automatically correlate:
1. **Garment Signatures** (from this engine)
2. **Cosmetic Inventory** (from the Inventory Engine)
3. **Personal Color Analysis** (from the FaceLab/Analyzer)

The result is a scientifically harmonized beauty look tailored to the user's specific outfit, achieved entirely without the use of external generative AI.
