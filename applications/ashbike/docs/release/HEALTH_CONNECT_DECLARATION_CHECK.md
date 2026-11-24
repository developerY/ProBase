# Google Play Console: Health Connect Declaration Audit

This document serves as the final truth for the Health Connect permissions actively bundled into the AshBike Mobile Release App Bundle (.aab). 
It has been programmatically generated from the final merged release AndroidManifest.xml.

**App Version:** v0.0.5 (Build 5)
**Date Generated:** November 22, 2026

## 1. Verified Release Permissions
The following table reflects the EXACT 8 Health Connect permissions that Google Play Console will detect when this App Bundle is uploaded. All other permissions have been successfully stripped.

| Data Type | Read Permission | Write Permission | Justification for Play Console Declaration |
|---|---|---|---|
| **Distance** | `READ_DISTANCE` | `WRITE_DISTANCE` | Used by the Ride Tracker and Ride History screens to calculate total mileage per session and sync completed bike rides with Health Connect, allowing users to track their cycling distance over time. |
| **Exercise Session** | `READ_EXERCISE` | `WRITE_EXERCISE` | Used by the Bike Computer and Ride Summary features to log structured cycling workouts to Health Connect so users can review their ride history alongside other fitness apps. |
| **Heart Rate** | `READ_HEART_RATE` | `WRITE_HEART_RATE` | Used to display real-time cardiac intensity during rides and log heart rate samples for synced workout sessions in Health Connect. |
| **Calories Burned** | `READ_TOTAL_CALORIES_BURNED` | `WRITE_TOTAL_CALORIES_BURNED` | Used by the Ride Summary screen to calculate and save estimated energy expenditure for tracked bike rides to Health Connect, helping users monitor workout intensity and daily fitness goals. |

## 2. Privacy Policy Configuration Verification
The merged manifest has been verified to include the mandatory Health Connect privacy intent handlers.

- `androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE` (Present)
- `android.intent.action.VIEW_PERMISSION_USAGE` (Present)
- `android.permission.START_VIEW_PERMISSION_USAGE` (Present on alias)
- `android.intent.category.HEALTH_PERMISSIONS` (Present on alias)

## 3. Play Console Action Required (PENDING HUMAN VERIFICATION)
Before clicking "Send for review" on the Google Play Console:

1. **Verify Checkboxes:** Navigate to **App content → Health apps**. Ensure the exact 4 data types in the table above (Distance, Exercise Session, Heart Rate, Calories Burned) are checked. Ensure `Steps`, `Sleep`, `Hydration`, `Speed`, `Elevation`, and `Weight` are UNCHECKED.
2. **Copy Justifications:** Paste the exact justification text from the table above into the corresponding text boxes in the Play Console declaration form.
3. **Verify Privacy Policy URL:** Ensure the exact URL displayed in **App content → Privacy policy** perfectly matches the URL opened when the Android 14 Health Connect settings screen invokes the privacy intent.
4. **Confirm Declaration Status:** Ensure the Health apps declaration status reads **Approved**.
