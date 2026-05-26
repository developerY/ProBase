This folder contains the Android AI Sample catalog, a stand alone application giving you access to individual self-contained samples illustrating 
some of the Generative AI capabilities unlocked by some of Google's models.

Android AI Sample Catalog


[ai-samples](github.com/android/ai-samples)
Samples:


Send a voice prompt when first started:
- Use Gemini Nano for local intent classification.
- Fallback to server-side models for complex reasoning.

Routine Projection:
- `GlassConnectionHeaderAction` handles the UI state for device connectivity.
- `RoutinesEvent.ProjectToGlass` triggers the data sync to the wearable display.

UI Components:
- `RoutinesScreen`: Main entry point for viewing daily rituals.
- `RoutineDetailScreen`: Detailed view for individual routine management.
- `GlassConnectionHeaderAction`: Shared component for managing the projection state across screens.

State Management:
- `GlassButtonState`: Enum representing the connection status (`NO_GLASSES`, `READY_TO_START`, `PROJECTING`).
- `uiState.glassButtonState`: Observed state that updates the header action appearance.

Events:
- `RoutinesEvent.ProjectToGlass`: Dispatched when the user interacts with the connection action to initiate or stop projection.

Technical Implementation:
- **Component Location**: `com.zoewave.probase.kocolor.features.routines.ui.components.GlassConnectionHeaderAction`
- **Visual Feedback**:
    - `PROJECTING`: Green background (`0xFF4CAF50`), `CastConnected` icon.
    - `READY_TO_START`: Primary theme color, `PermDeviceInformation` icon.
    - `NO_GLASSES`: Surface variant color (disabled), `UsbOff` icon.
- **Navigation**: The component is placed in the top app bar area of both the list and detail views to provide consistent access to projection controls.

Future Work:
- Implement the background service that maintains the socket connection to the Glass hardware during `PROJECTING` state.

Note:
Requires Firebase setup: the samples relying on 
Google Cloud models (Gemini Pro, Gemini Flash, etc...) 
require setting up a Firebase project and connecting the app to Firebase (read more [here).
