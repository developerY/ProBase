# Walkthrough - Camera Binding & Probing Fix

I have resolved the `IllegalArgumentException: No available camera can be found` error by implementing a robust camera probing mechanism and adding dual-permission checks for both the phone and the AI glasses.

## Changes Made

### 1. Camera Context Probing Loop
- Updated [VisionViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/VisionViewModel.kt).
- Implemented a probing sequence that attempts to bind to the camera using:
    1. **Glasses Context**: Targeted specifically at the projected device hardware.
    2. **Host Context**: Targeted at the phone's hardware from a projected perspective.
    3. **Application Context**: Standard fallback.
- For each context, it now probes for both **Back** and **Front** camera lenses, ensuring we find *any* available hardware.

### 2. Dual Permission Enforcement
- Accessing the glasses camera requires **glasses-specific permissions** via the `ProjectedPermissionsResultContract`.
- Updated [VisionRequirementGate.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/components/VisionRequirementGate.kt) to require:
    - **Phone Camera Access**: Mandatory for the OS to allow hardware projection.
    - **Glasses Camera Access**: Explicit user consent for the AI glasses device.
- The gate now provides separate buttons for "GRANT PHONE ACCESS" and "GRANT GLASSES ACCESS".

### 3. Diagnostic Transparency
- Updated the **Event Log** in the Hub to show the step-by-step probing results (e.g., `Probing Glasses context...`, `Found BACK camera in Glasses context. Binding...`).
- Added "Glasses Camera Access" to the **System Status** panel to clearly distinguish it from the phone's permission status.

## Verification Results

### Build Status
> [!TIP]
> The `:features:xr:glass:vision` module and its consumers (including `:features:xr:glass`) build successfully.

### Test Flow
1. Open the **Vision AI** demo.
2. If setup is required, you will see two permission cards.
3. Grant **Phone Access** (Standard Android dialog).
4. Grant **Glasses Access** (XR-specific coordinated flow).
5. **Observation:** The Event Log should show `Successfully created ProjectedContext for Glasses.` and then `SUCCESS: Camera successfully bound to Glasses.`
6. Trigger a capture; the Hub should now correctly show the glasses' viewpoint.
