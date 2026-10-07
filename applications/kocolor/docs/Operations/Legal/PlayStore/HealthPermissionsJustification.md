# Google Play Console Health Data Permissions Justification Guide for KoColor

Use this guide when completing the **Health Connect Declaration Form** in Google Play Console (under **App Content > Health Connect**).

> **IMPORTANT:** Per Google Play Health Connect Policy (Minimum Scope / Data Minimization), you must **ONLY** declare the 5 active permissions listed below. Do **NOT** check Distance, Heart Rate, Exercise, or Nutrition.

---

## 1. Active Declared Permissions (Keep Checked)

### Hydration (`android.permission.health.READ_HYDRATION` & `android.permission.health.WRITE_HYDRATION`)
> KoColor reads and writes hydration records to power its core daily water intake tracking feature. Users set personalized fluid volume targets (e.g., 2.0L daily goal) on the Home dashboard and Settings screen to monitor moisture levels and support skin barrier hydration. Hydration records are managed locally on the device.

---

### Steps (`android.permission.health.READ_STEPS`)
> KoColor reads daily step counts to evaluate physical movement and routine activity levels. This metric is processed locally on-device on the Home screen to calibrate footwear recommendations (e.g., suggesting supportive active sneakers vs. dress shoes) and style playlist planning. Step data is processed strictly on-device without cloud transmission.

---

### Sleep (`android.permission.health.READ_SLEEP`)
> KoColor reads sleep duration records to display sleep health telemetry on the main dashboard. This metric is processed locally on-device to correlate overnight recovery with skin barrier wellness and suggest morning skin defense routines.

---

### Active calories burned (`android.permission.health.READ_ACTIVE_CALORIES_BURNED`)
> KoColor reads active calories burned to correlate physical exertion with skin perspiration and thermal response. This data is processed locally on-device to adjust ambient style recommendations, such as suggesting breathable, moisture-wicking fabrics and lightweight outerwear during high-activity days.

---

### Total calories burned (`android.permission.health.READ_TOTAL_CALORIES_BURNED`)
> KoColor reads total calories burned to evaluate overall daily metabolic activity and energy expenditure. This data is used locally on-device on the Home wellness dashboard to adjust personalized skin recovery recommendations.

---

## 2. Removed / Unchecked Permissions (Do NOT Check in Play Console)

The following data types are **REMOVED** from KoColor's manifest and must **NOT** be selected in the Play Console declaration form:
- ❌ **Distance** (`READ_DISTANCE`)
- ❌ **Exercise / ExerciseSession** (`READ_EXERCISE`)
- ❌ **Heart Rate** (`READ_HEART_RATE`)
- ❌ **Nutrition** (`READ_NUTRITION`)
