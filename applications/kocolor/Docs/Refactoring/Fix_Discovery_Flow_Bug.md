# Case Study: Resolving the "Ghost Form" Navigation Bug

## The Issue
During the development of the **New Discovery** flow (product archival), we encountered a critical bug where scanning a barcode would successfully retrieve data from the Open Beauty Facts (OBF) database, but the UI form would remain completely empty upon returning to the app.

### The "Smoking Gun" (Lifecycle & State Loss)
Detailed Logcat analysis revealed that the `CosmeticsViewModel` was being destroyed and recreated every time the user returned from the external GMS Barcode Scanner activity.

**Log Evidence:**
1. `CosmeticsVM D ViewModel created: 78800312` (Initial launch)
2. `CosmeticsVM D Scanned code received... (Instance: 241602285)` (Scanner returns, new instance!)
3. `CosmeticsVM D ViewModel created: 265243440` (UI resumes, a third instance!)

Because Jetpack Compose ViewModels are scoped to the Composable's lifecycle, the volatile memory (StateFlows) containing the scanned barcode and the fetched Maybelline product data was being wiped clean before the UI had a chance to render it.

## The Resolution: Singleton Session Repository
To ensure data integrity across activity transitions and ViewModel recreations, we shifted from a **ViewModel-centric** state to an **Archival Session-centric** architecture.

### 1. Singleton Draft Store
We introduced a `cosmeticDraft` StateFlow within the `FashionSessionRepository`. Since this repository is a Dagger **@Singleton**, it survives the destruction of any specific UI screen or ViewModel.

```kotlin
@Singleton
class FashionSessionRepository {
    private val _cosmeticDraft = MutableStateFlow<CosmeticItem?>(null)
    val cosmeticDraft: StateFlow<CosmeticItem?> = _cosmeticDraft.asStateFlow()
    // ...
}
```

### 2. Reactive ViewModel Binding
The `CosmeticsViewModel` was refactored to treat this repository as the **Single Source of Truth** for the discovery form. Instead of holding its own `draftItem` state, it now subscribes directly to the session repository.

*   When the scanner returns a code, it is saved directly to the **Session Repository**.
*   The OBF lookup updates the **Session Repository**.
*   The UI, observing the ViewModel's `uiState`, automatically reflects these changes because the ViewModel is now just a pass-through for the persistent session data.

### 3. State-Aware Initialization
We updated the `InitializeAdd` logic to be "lazy." It now intelligently detects if the session already contains data (like a scanned barcode or a captured image) and skips the default reset-to-empty logic.

## Key Benefits
*   **Zero Data Loss**: Scanned barcodes and AI analyses now survive even if the OS kills the main activity to reclaim memory while the scanner is open.
*   **Atomic UI Updates**: Binding the input fields directly to the persistent session state fixed the "not able to write" bug, as the state loop is now stable and globally consistent.
*   **Architectural Cleanliness**: The complex logic of discovery (AI + Barcode + Manual Entry) is now isolated from the transient lifecycle of a single screen.

**Outcome:** The discovery flow is now high-performance and technically robust, providing the fast, automated experience required for the **Atelier** professional archive.
