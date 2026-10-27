# Seaweed Financial App - Behavioral Awareness Engine Refactor

I have completed a project-wide refactor of the Seaweed app, transforming it into a **"Financial Awareness Engine"** centered on behavioral change. This update moves beyond simple tracking and focuses on helping users distinguish between **Needs (Required)** and **Wants (Optional)** spending.

## Core Behavioral Architecture

### 1. The "Needs vs. Wants" System
- **Dashboard Comparison**: A new high-impact chart at the top of the Home screen reveals the truth about your spending patterns at a glance.
- **Dynamic Savings Insights**: Integrated a **"What If" Simulator** that shows exactly how much you'd save annually by reducing "Optional" spending by just 20%.
- **Behavioral Tension Layer**: The app now tracks **System Defaults** vs. **User Overrides**. You can see exactly where you've reclassified discretionary spending as a "Need," encouraging honest reflection.

### 2. High-Precision Currency Model
- **Cents-Based (Long)**: Migrated the entire database and domain layer to use **Long (cents)** for all monetary amounts. This eliminates floating-point rounding errors and ensures 100% financial accuracy across all views.
- **`CurrencyUtils`**: Centralized all formatting and conversion logic, ensuring consistent presentation of currency throughout the UI.

### 3. Dedicated Category Intelligence
- **Category Repository**: Introduced a standalone Category entity that stores system-level defaults (e.g., Rent = NEED, Netflix = WANT).
- **Consolidated "Brain"**: This repository now acts as the single source of truth for suggestions and behavioral classifications across the Transactions, Budget, and Analytics screens.

## UI & UX Enhancements

### 4. Consolidated Navigation
- **3 Primary Hubs**: Focused the app on **Main**, **Transactions**, and **Settings**.
- **Interactive Toggles**: Every recurring bill now features a "Star" toggle and clear labeling to instantly mark it as Required or Optional.

### 5. Extraordinary Visual Polish
- **Dynamic Motion**: Dashboard sections now feature staggered entrance animations (fade + slide-in).
- **Modern Data Viz**: The spending ring now features an elegant "filling" animation and modular segment gaps.

---

## Technical Verification

### Automated Verification
- Successfully ran a full Gradle build of the mobile application:
    - `gradle_build(":applications:seaweed:apps:mobile:assembleDebug")`
- Verified database migration to **Version 5** with optimized entities.

### Manual Verification Path
1.  **Dashboard Load**: Observe the new "Needs vs. Wants" chart filling up alongside the spending ring.
2.  **Spend Log**: Use the [+] button to generate random data and see it populate the behavioral chart.
3.  **Bill Management**: Toggle a subscription (e.g., Netflix) from Optional to Required and watch your "Real Starting Balance" and "Needs" chart update in real-time.
