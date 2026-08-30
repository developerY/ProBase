# Google Play Console Health Data Permissions Justification Guide for KoColor

Copy and paste the exact text blocks below into the corresponding fields in Google Play Console under **Health apps > Health data permissions**.

---

## 1. Activity Category Permissions

### Active calories (`android.permission.health.READ_ACTIVE_CALORIES_BURNED`)
> KoColor reads active calories burned to correlate physical exertion with skin perspiration and thermal response. This data is processed locally on-device to adjust ambient style recommendations, such as suggesting breathable, moisture-wicking fabrics and lightweight outerwear during high-activity days. Data is handled strictly locally and is never transmitted off-device.

---

### Distance (`android.permission.health.READ_DISTANCE`)
> KoColor reads daily travel distance to evaluate outdoor physical exposure. This data is processed locally on-device to calibrate environmental garment and sunscreen recommendations, suggesting protective outerwear or active footwear based on outdoor movement. All processing occurs locally on the user's device.

---

### Exercise (`android.permission.health.READ_EXERCISE`)
> KoColor reads exercise session records to detect active workout periods. This information is used locally on-device to tailor post-workout style and skin defense recommendations, such as prioritizing breathable activewear and skin-soothing cosmetic routines. Data remains 100% local on the device.

---

### Steps (`android.permission.health.READ_STEPS`)
> KoColor reads daily step counts to evaluate physical movement and routine activity levels. This metric is processed locally on-device to calibrate footwear recommendations (e.g., suggesting supportive active sneakers vs. dress shoes) and style playlist planning. Step data is processed strictly on-device without cloud transmission.

---

### Total calories burned (`android.permission.health.READ_TOTAL_CALORIES_BURNED`)
> KoColor reads total calories burned to evaluate overall daily metabolic activity and energy expenditure. This data is used locally on-device to adjust wellness correlation metrics and personalized skin recovery recommendations. No data is stored externally or shared with third parties.

---

### Heart rate (`android.permission.health.READ_HEART_RATE` & `android.permission.health.WRITE_HEART_RATE`)
> KoColor reads heart rate bio-markers to correlate physical exertion and vital states with skin defense telemetry. This data is processed 100% locally on-device to provide personalized skin wellness insights and adjust activewear recommendations. Heart rate data remains on the user's device and is never uploaded externally.

---

## 2. Nutrition & Weight Category Permissions

### Hydration (`android.permission.health.READ_HYDRATION` & `android.permission.health.WRITE_HYDRATION`)
> KoColor reads and writes hydration records to power its core daily water intake tracking feature. Users set personalized fluid volume targets (e.g., 2.5L daily goal) to monitor moisture levels and support skin barrier hydration. Hydration records are managed and stored locally on the device.

---

### Nutrition (`android.permission.health.READ_NUTRITION` & `android.permission.health.WRITE_NUTRITION`)
> KoColor accesses nutrition records locally to allow users to log and correlate dietary wellness factors (such as dietary antioxidant or fluid intake) with personal skin health and color clarity. All nutritional data is processed strictly on-device for personal wellness insights and is never transmitted off-device or shared with third parties.

---

### Weight (`android.permission.health.READ_WEIGHT` & `android.permission.health.WRITE_WEIGHT`)
> KoColor reads weight records locally to calculate accurate personal hydration volume targets and body water proportion goals. This data is processed strictly on-device to support the app's local wellness and hydration tracking features.

---

## 3. Background & Historical Access Permissions

### Health data history (`android.permission.health.READ_HEALTH_DATA_HISTORY`)
> KoColor reads historical health data (such as past hydration trends and physical activity patterns over preceding weeks) to establish baseline wellness trends. This historical perspective allows KoColor to calculate personalized long-term skin recovery and style playlist insights locally on-device without cloud data storage.

---

### Health data in background (`android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND`)
> KoColor reads health data in the background to update hydration goal progress, calculate daily activity summaries, and generate timely wellness notifications (such as hydration reminders) when the app is not actively open. All background processing is performed strictly locally on the user's Android hardware without external data transmission.
