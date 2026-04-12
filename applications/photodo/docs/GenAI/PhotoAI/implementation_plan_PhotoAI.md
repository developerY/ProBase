# Implement "Smart Capture" Feature Module

The goal is to create a new, isolated module `:features:smartcapture` that allows users to capture a photo, extract text via ML Kit, and intelligently parse it into a structured `SmartTask` using Gemini Nano or a Regex fallback.

## Proposed Changes

### 1. New Module Setup: `:features:smartcapture`

- **`build.gradle.kts`**: Configure plugins (library, hilt, compose, serialization) and dependencies (ML Kit, Generative AI, core modules).
- **`settings.gradle.kts`**: Include the new module.

### 2. Domain Layer

- **`SmartTask.kt`**: A serializable data model for the extracted task.
- **`SmartTaskParser.kt`**: Interface for the parsing logic.

### 3. Data Layer

- **`MlKitOcrEngine.kt`**: Implementation using `TextRecognition` to extract raw text.
- **`GeminiNanoParser.kt`**: Implementation using `GenerativeModel` to parse text into JSON.
- **`RegexTaskParser.kt`**: Fallback implementation using regular expressions.
- **`SmartCaptureCoordinator.kt`**: Manages the multi-step pipeline (Capture -> OCR -> Parse).

### 4. UI Layer

- **`SmartCaptureUiState.kt`**: Represents the capture and parsing states.
- **`SmartCaptureViewModel.kt`**: Handles image processing and state updates.
- **`SmartCaptureScreen.kt`**: UI for capturing images and reviewing extracted task details.

## Technical Details

### Hybrid Pipeline Logic
1.  **Vision**: Extract raw text using ML Kit.
2.  **Reasoning**:
    - If `gemini-nano` is available, prompt it for JSON.
    - If unavailable or fails, use `RegexTaskParser`.

---

## Verification Plan

### Automated Tests
- Run `:features:smartcapture:assembleDebug` to ensure compilation.

### Manual Verification
- Integrate with `FeatureInventory` in the main app for isolated testing.
- Verify the OCR and parsing logic through the inventory UI.
