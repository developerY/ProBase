# Seaweed Gemini Receipt Integration Plan

Integrate Google Gemini into Seaweed for intelligent receipt scanning and data entry, mirroring the PhotoDo architecture.

## Architecture

### 1. New Module: `:applications:seaweed:features:receiptcapture`
Create a dedicated feature module for receipt scanning UI and AI orchestration.

#### Domain Layer
- `SmartReceiptDraft`: Data class representing extracted receipt data (Merchant, Total, Date, Category).
- `ReceiptEngine`: Interface for receipt parsing engines.

#### Data Layer
- `CloudReceiptEngine`: Tier 1 engine using Gemini (Cloud) for multimodal parsing.
- `LocalReceiptEngine`: Tier 2 engine using ML Kit + Regex heuristics.
- `ReceiptOrchestrator`: Manages the fallback logic between Cloud and Local engines.

#### UI Layer
- `SmartReceiptScreen`: Handles the camera capture, context input (optional comment), and AI analysis splash.
- `ReceiptSaveForm`: Review form for the user to confirm/edit extracted data before saving to the database.
- `SmartReceiptViewModel`: Manages the state and events for the receipt capture flow.

### 2. Settings Integration
- Update Seaweed's `AppSettingsRepository` to implement `AiConfigurationSettings`.
- Add AI configuration fields to Seaweed's `UserSettings` (Room database).
- Use `AiConfigurationCard` from `:features:ai:configuration` in Seaweed's settings tab.

---

## Proposed Changes

### Seaweed Core Components

#### [UserSettings.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/model/src/main/java/com/zoewave/probase/seaweed/model/UserSettings.kt)
- Add `isAiEnabled: Boolean` and `aiModel: String`.

#### [UserSettingsEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/database/src/main/java/com/zoewave/probase/seaweed/database/UserSettingsEntity.kt)
- Add AI fields and update mappers.

#### [UserSettingsRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/data/src/main/java/com/zoewave/probase/seaweed/data/UserSettingsRepository.kt)
- Implement `AiConfigurationSettings` interface.

---

### UI & Navigation

#### [seaweedNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/src/main/java/com/zoewave/probase/seaweed/mobile/ui/navigation/seaweedNavEntryProvider.kt)
- Add `ReceiptCapture` route.
- Handle navigation from camera to review form.

#### [SeaweedDestination.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/model/src/main/java/com/zoewave/probase/seaweed/model/navigation/SeaweedDestination.kt)
- Add `ReceiptCapture` destination.

---

## Verification Plan

### Automated Tests
- Run `./gradlew :applications:seaweed:features:receiptcapture:assembleDebug`
- Run `./gradlew :applications:seaweed:apps:mobile:assembleDebug`

### Manual Verification
1. Open Seaweed Settings and configure Gemini API Key.
2. Navigate to Transaction list and tap "Add via Receipt" (Camera).
3. Snap a photo, add an optional comment.
4. Verify the "Cloud AI" or "Local AI" analysis splash appears.
5. Review the extracted Merchant, Total, and Date in the form.
6. Confirm and verify the transaction is saved correctly in Seaweed.
