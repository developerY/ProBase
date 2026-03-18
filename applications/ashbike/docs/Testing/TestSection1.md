### 🐛 Bug Report: Data Discrepancy (Watch vs. Phone Tracking)

**Overview:** The same physical ride was recorded simultaneously (or synced improperly) across the Watch and Phone, resulting in significant data drift across almost every metric.

#### 1. Time & Duration Mismatch (The Root Cause)
The Watch recorded roughly 3 extra minutes of tracking. This time delta is likely responsible for the downstream differences in distance and speed.
* **Watch:** 10:31 – 10:54 (23 min 16 s)
* **Phone:** 10:33 – 10:53 (20 min 11 s)
* *Developer Note:* Check if the phone's background service was killed/delayed by Android's battery optimization, causing it to start tracking 2 minutes late and stop 1 minute early.

#### 2. Distance & Speed Discrepancy
Because the watch tracked for longer, it captured more distance, which altered the speed calculations.
* **Distance:** Watch (7.6 km) vs. Phone (7.2 km)
* **Avg Speed:** Watch (19.5 km/h) vs. Phone (21.4 km/h)
* **Max Speed:** Watch (64.2 km/h) vs. Phone (62.4 km/h)
* *Developer Note:* The phone's higher average speed despite a shorter distance suggests it chopped off the slower "start/stop" portions of the ride, or the watch's GPS experienced "drift" while stationary.

#### 3. Elevation Data Failure (Phone)
The phone completely failed to record or calculate altitude changes.
* **Watch:** 82m ↑ / 91m ↓ *(Looks like accurate barometer/GPS data)*
* **Phone:** 0m ↑ / 0m ↓
* *Developer Note:* Verify if the Phone's location permissions include altitude, or if the `PhoneBikeViewModel` is missing the mapping logic for `elevationGain` that we recently fixed on the Watch.

#### 4. Calorie Calculation Error (Critical Bug)
The calorie math is completely broken on both devices, but in opposite directions.
* **Watch:** 0 kcal
    * *Cause:* The watch likely didn't receive heart rate data (perhaps the `BODY_SENSORS` permission or `HealthConnect` toggle wasn't active), causing the formula to output zero.
* **Phone:** 20,408 kcal
    * *Cause:* This is physically impossible for a 20-minute ride. This is almost certainly a unit conversion bug. The phone's math engine might be calculating Joules instead of kilocalories, or accidentally multiplying the final result by 1,000.

---

### Suggested Next Step for Debugging
The most critical bug here is the **Calorie math**. Do you want to take a look at the `caloriesBurned` calculation in your Phone's Service/ViewModel to see why it's outputting 20,000+ calories?