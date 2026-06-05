# KoColor Health - Implementation Plan: Vitals and Alerts Separation

I will separate the "Vitals" and "Alerts" into two distinct sections on the Health dashboard to ensure that biometric data and proactive health insights are clearly distinguished.

## 1. Research & Analysis
- **Current State**: `VitalsAlertsCard` combines heart rate data with `SkinInsight` alerts.
- **Requirement**: Separate them. Show "Normal" status for vitals when applicable. Show alerts as a distinct, actionable section.

## 2. Technical Steps

### UI Component Deconstruction (`StyleHealthDashboard.kt`)
- [ ] **Create `VitalsCard`**:
    - Focused on real-time biometrics (Heart Rate).
    - Includes a status indicator: "Normal" (Green) if heart rate is between 60 and 100 bpm, or "Syncing..." if data is missing.
    - Consistent with the "Atelier" design (32dp corners, subtle background).
- [ ] **Create `AlertsSection`**:
    - Only rendered if `uiState.alerts` is not empty.
    - Displays a list of `SkinInsight` alerts with their specific manifestations and recommendations.
    - Uses a prominent alert theme (Red/Pink accents).
- [ ] **Update `StyleHealthDashboard` Layout**:
    - Integrate the new `VitalsCard` and `AlertsSection`.
    - Position `AlertsSection` at the top of the bio-markers list when active to ensure immediate visibility.

### Logic Updates
- [ ] Implement a helper function to determine "Vitals Status" based on heart rate samples.

## 3. Visual & Aesthetic Standards
- **Color Coding**: 
    - Green (`#4CAF50`) for Normal Vitals.
    - Red (`#F44336`) for Active Alerts.
- **Typography**: Retain `Serif` for headers and `Black` weights for primary values.

## 4. Verification
- [ ] Verify that the `AlertsSection` correctly disappears when there are no alerts.
- [ ] Verify that `VitalsCard` correctly displays "Normal" status for typical heart rates.
- [ ] Build and run `:applications:kocolor:apps:mobile`.

---
<!-- feedback_request -->
I've updated the plan to strictly separate biometric status from actionable health alerts. This will eliminate the confusion of mixing "Normal" data with "Alert" messages.

**Should I proceed with this refactor?**
