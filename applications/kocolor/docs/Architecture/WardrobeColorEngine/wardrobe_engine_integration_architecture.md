# **Wardrobe Color Engine: Integration & Persistence Architecture**

## **1\. Architectural Overview**

The **Wardrobe Color Engine** has been successfully transitioned from a standalone processing concept into a fully integrated, multi-module pipeline. The core objective of this phase was establishing the **"Auto-Analysis" flow**: a seamless, localized orchestration where raw garment imagery is automatically analyzed for color intelligence *before* it is committed to the persistence layer.

By strictly separating concerns across the :model, :features:color, :db, and :data modules, the architecture remains decoupled, highly testable, and primed for the upcoming **Comprehensive Look** recommendation system.

## **2\. Module Responsibilities & Expansion**

The system enforces a strict unidirectional data flow, ensuring that heavy image processing logic never leaks into the UI or database layers.

### **A. The Core Domain (:model)**

The :model module acts as the single source of truth for the application's domain vocabulary. It has been expanded to support rich analytical metadata without holding any references to Android-specific frameworks (like Room or Palette).

* **ClothingItem:** Now includes exact hexadecimal signatures (dominantHex, vibrantHex), structural markers (contrastLevel, colorTemperature), and semantic styling tags (seasonalPalette).  
* **CosmeticItem & HarmonizedLook:** Pre-emptively defined to share the exact semantic vocabulary (e.g., WARM, AUTUMN) required for future makeup-to-garment matching.

### **B. The Analytical Engine (:features:color)**

This module is the isolated home for all local color science and pixel-processing heuristics. It contains no database logic.

* **WardrobeAnalyzer:** Handles safe bitmap downsampling and utilizes the Android Palette API to extract population-weighted color swatches.  
* **ColorScienceUtils:** A pure mathematical utility that converts RGB values into HSL coordinate space, applies deterministic logic to calculate temperature (WARM, COOL, NEUTRAL), and maps tones to KoColor's strict seasonal palettes.  
* **WardrobeColorEngine:** The facade that orchestrates the analyzer and the math utilities, returning a fully populated ClothingItem ready for storage.

### **C. The Persistence Layer (:db)**

The :db module is strictly responsible for local Room database operations. It does not know how colors are extracted; it only knows how to store and query the results.

* **ClothingEntity:** The Room-specific data class mirroring the domain model.  
* **Converters:** Implements Gson-backed TypeConverters to flatten lists of palette hex strings into structured JSON for SQLite storage.  
* **ClothingDao:** Exposes highly specific queries (e.g., getClothingBySeason("AUTUMN")) that will power offline, high-speed styling recommendations.

## **3\. The "Auto-Analysis" Orchestration (:data)**

The :data module bridges the gap between raw input and persistent storage. The WardrobeRepositoryImpl acts as the traffic controller, intercepting save requests to trigger the analytical pipeline on a background thread.

### **The Pipeline Execution Flow**

1. **Input:** The UI layer passes a raw image URI to the repository.  
2. **Thread Shift:** The repository immediately shifts execution to Dispatchers.IO to prevent UI blocking.  
3. **Extraction:** The repository invokes WardrobeColorEngine.processGarment().  
   * *The Engine scales the bitmap, extracts the swatches, calculates the HSL values, and applies the semantic KoColor tags.*  
4. **Mapping:** The resulting domain ClothingItem is mapped directly to a ClothingEntity.  
5. **Persistence:** The enriched entity—now containing both the image reference and the full suite of color intelligence metadata—is saved to Room via the ClothingDao.

### **Sequence Diagram Representation**

\[UI\] \-\> (Raw URI) \-\> \[WardrobeRepository\]  
                          |  
                          v (Dispatchers.IO)  
                     \[WardrobeColorEngine\]  
                          |-- 1\. loadDownsampledBitmap()  
                          |-- 2\. Android Palette Extraction  
                          |-- 3\. ColorScienceUtils (RGB \-\> HSL)  
                          |-- 4\. Semantic Mapping (WARM, AUTUMN, etc.)  
                          v  
                 (Returns: ClothingItem)  
                          |  
                          v (Maps to ClothingEntity)  
                     \[ClothingDao\]  
                          |  
                          v  
                   \[Local SQLite DB\]

## **4\. Foundation for the "Comprehensive Look"**

By standardizing the extraction and storage of wardrobe color data, this architecture successfully sets the stage for the **Comprehensive Look Builder**.

Because every saved garment now definitively knows its colorTemperature and seasonalPalette, the future :features:suggestions module can execute instantaneous, deterministic matching against the user's cosmetic inventory (CosmeticItem) using simple heuristic scoring—entirely avoiding the latency, privacy concerns, and financial overhead of remote generative AI.