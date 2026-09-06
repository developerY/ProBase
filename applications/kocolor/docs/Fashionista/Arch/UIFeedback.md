Scaffold the Jetpack Compose presentation layer to render the KoColor `StyleBlueprint` and our dual-axis evaluation metrics on device.

1. Create UI State Holder: Create a `StyleResultUiState` data class containing:
    - `blueprint` (StyleBlueprint?)
    - `fashionistaScore` (FashionistaScore?)
    - `intentMatch` (IntentFulfillmentScore?)
    - `isLoading` (Boolean)
    - `errorMessage` (String?)

2. Create the ViewModel: Create `StyleResultViewModel` which consumes the orchestration engine, exposes a `StateFlow<StyleResultUiState>`, and triggers the generation request.

3. Build Compose Screen (`StyleResultScreen.kt`): Implement a clean, modern Jetpack Compose layout with:
    - A loading state showing simulation progress.
    - An Outfit Card displaying the selected wardrobe items, their materials, and color swatches derived from `recommendedPalette`.
    - A FASHIONISTA Badge component rendering the aesthetic score (0-100) and APPROVED/REJECTED status.
    - An Intent Match Badge displaying the fulfillment percentage.
    - The AI rationale text rendered in a clean typography container.