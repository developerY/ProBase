Scaffold the Jetpack Compose presentation layer for the KoColor style recommendation and FASHIONISTA evaluation pipeline.

1. Create UI State Holder: Create a `StyleResultUiState` data class containing:
    - `blueprint` (StyleBlueprint?)
    - `fashionistaScore` (FashionistaScore?)
    - `isLoading` (Boolean)
    - `errorMessage` (String?)

2. Create the ViewModel: Create `StyleResultViewModel` which consumes the orchestration engine, exposes a `StateFlow<StyleResultUiState>`, and handles triggering the simulation/generation request.

3. Build Compose Screen (`StyleResultScreen.kt`): Implement a clean, modern Jetpack Compose layout with:
    - A loading state showing simulation progress.
    - An Outfit Card displaying the selected wardrobe items (anchored by `w_41`), their materials, and color swatches derived from `recommendedPalette`.
    - A Cosmetic Grid mapping the selected roles (Eye, Cheek, Lip, Nail) with their resolved temperatures (WARM/COOL/NEUTRAL).
    - A FASHIONISTA Badge component rendering the final score out of 100 and the APPROVED/REJECTED status pill based on `isApproved`.
    - The fluid AI rationale text rendered in a clean typography container.