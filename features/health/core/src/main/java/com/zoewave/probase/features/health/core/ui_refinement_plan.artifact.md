# Implementation Plan - Health Dashboard UI Refinement

I will overhaul the Health & Wellness dashboard to match the high-fidelity, editorial design provided, focusing on icon coloring, button clarity, and typography alignment.

## 1. Research & Analysis
- **Current Issues**: Summary card icons are monochromatic, hydration buttons are unreadable due to incorrect blur application, and some text elements are wrapping unexpectedly.
- **Visual Goal**: A clean, "Atelier" style dashboard with editorial typography and high-density information display.

## 2. Technical Steps

### UI Refinement (`StyleHealthDashboard.kt`)
- [ ] **Summary Cards**:
    - Update `SummaryCard` to accept an `iconColor`.
    - Pass specific colors in `StyleHealthDashboard`:
        - Sleep: `Color(0xFF9C27B0)` (Purple)
        - Hydration: `Color(0xFF2196F3)` (Blue)
        - Vitals: `Color(0xFF4CAF50)` (Green) or `Color.Gray`.
    - Set `maxLines = 1` and adjust font size for the `value` text to prevent awkward wrapping.
- [ ] **Hydration Visual**:
    - Fix the quick-add buttons: Move the `blur` effect to a separate background layer or use a semi-transparent `Surface` with `MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)` to ensure text remains sharp.
    - Standardize the wavy background opacity.
- [ ] **Alerts Section**:
    - Refine the typography to use `Serif` for primary alert titles.
- [ ] **Activity Section**:
    - Ensure `ActivityCardRefined` uses outlined icons and centered, bold serif values.

## 3. Visual & Aesthetic Standards
- **Typography**: Extensive use of `Serif` for headers and primary data points.
- **Colors**: Intentional use of categorical colors for icons.
- **Spacing**: Consistent `spacedBy(16.dp)` or `spacedBy(24.dp)` for major sections.

## 4. Verification
- [ ] **Visual Check**: Compare the new dashboard against the provided screenshot.
- [ ] **Accessibility**: Ensure all text on "frosted" elements is legible.
- [ ] **Build Check**: Run `:applications:kocolor:apps:mobile:assembleDebug`.

---
<!-- feedback_request -->
I've updated the plan to specifically target the colored icons and the blurred button issue. I'll also ensure all typography is aligned to your provided design.

**Should I proceed with the UI overhaul?**
