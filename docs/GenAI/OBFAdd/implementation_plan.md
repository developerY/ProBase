# Implementation Plan - Open Beauty Facts Contribution

This plan outlines the integration of a crowdsourcing feature that allows KoColor users to contribute product data to **Open Beauty Facts (OBF)** when a barcode scan fails to find a match.

## Goal
Improve the global OBF database by allowing users to upload newly discovered product information (Name, Brand, Ingredients) linked to a barcode, ensuring everyone benefits from the crowdsourced ecosystem.

## User Review Required

> [!IMPORTANT]
> The app will ask for explicit permission before uploading any data to Open Beauty Facts.
> Contribution can be anonymous (if OBF supports it) or authenticated via an OBF account.
> We will need to collect: Product Name, Brand, Category, and Ingredients (which are already being extracted by our AI).

## Proposed Changes

### [Component] Open Beauty Facts API

#### [MODIFY] [OpenBeautyFactsApi.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/obf/src/main/java/com/zoewave/probase/features/obf/data/remote/OpenBeautyFactsApi.kt)
- Add a new `@POST` endpoint for data writing: `cgi/product_jqm2.pl`.
- Support form-encoded parameters for product metadata.

### [Component] OBF Data Layer

#### [MODIFY] [ObfRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/obf/src/main/java/com/zoewave/probase/features/obf/data/repository/ObfRepository.kt)
- Implement `uploadProduct` logic.
- Map `CosmeticItem` fields back to the OBF API schema (e.g., `product_name`, `brands`, `ingredients_text`).

### [Component] Cosmetics UI Flow

#### [MODIFY] [CosmeticsUiState.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/CosmeticsUiState.kt)
- Add state to track if a contribution prompt should be shown (`showObfContributionPrompt`).
- Store the initial barcode if one was scanned.

#### [MODIFY] [CosmeticsViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/CosmeticsViewModel.kt)
- Logic to trigger the OBF prompt after a successful AI analysis or manual entry of a missing barcode.
- Implement the `onContributeToObf` event handler.

#### [MODIFY] [StitchProductBuilder.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/StitchProductBuilder.kt)
- Implement the **Contribution Prompt Dialog**.
- Provide a clear explanation of what is being shared.
- Allow users to enter OBF credentials (optional).

## Verification Plan

### Automated Tests
- Unit test `ObfRepository` with successful and failed POST responses.

### Manual Verification
1. Scan a barcode not in the database.
2. Complete the product details via **Box Scan**.
3. Confirm the "Contribute to Open Beauty Facts?" dialog appears.
4. Agree to contribute and verify the "Success" feedback.
5. (Optional) Check the OBF website to see the newly added "Pending" product.
