# AshBike Wear OS - Feature Specifications

AshBike is a high-performance, luxury-grade cycling computer built for Wear OS using Jetpack Compose. It rejects the standard "gamified" smartwatch aesthetic in favor of the negative space, precise typography, and dynamic geometry found in high-end chronographs.

## 🏎️ The Dynamic Racing Gauge (Speedometer)

Unlike standard fitness apps that simply display a number, AshBike features a true, mathematically drawn speedometer.

* **Non-Linear Tapering:** The gauge path dynamically expands in thickness as speed increases, visually simulating momentum. It starts razor-thin at 0 km/h and swells to a massive, aggressive band at maximum speed.
* **Segmented "Cutout" Track:** Utilizing negative space, the background track is sliced into discrete blocks using black structural lines, ensuring maximum contrast in harsh sunlight.
* **Contoured Typography:** The gauge numbers (10, 20, 30) dynamically calculate the inner radius of the tapered slope, contouring perfectly along the edge of the active void.
* **Responsive Gradient:** The active sweep uses a continuous Green-to-Yellow-to-Red gradient to indicate speed zones at a glance.

## 🫀 Smart Biometric Complications

AshBike avoids static UI elements. The side complications ("cheeks") use complex math to physically react to the rider's effort.

* **Proportional Pulsing Heart Rate:** * The heart icon scales and beats at a millisecond duration mathematically tied to the rider's live BPM (clamped to maintain a smooth 60fps frame rate).
* Automatically shifts colors based on standard athletic Heart Rate Zones (Resting/Blue -> Fat Burn/Green -> Aerobic/Orange -> Peak/Red).


* **Metabolic Calorie Burn:** * Features a continuous, animated crossfade between "Fire" and "Energy/Metabolism" icons.
* The speed of the crossfade transition dynamically accelerates based on the total cumulative calories burned, causing the UI to visually "heat up" later in the ride.



## ⏱️ Luxury Typography & Layout Design

Optimized specifically for the physical constraints of a 1.4-inch round screen.

* **Dynamic Speed Number:** The central speed digit uses linear interpolation (`lerp`) to perfectly match the exact hex color of the outer gauge gradient based on the current speed fraction.
* **High-Contrast Stroking:** Primary metrics utilize heavy black structural outlines beneath solid fills to ensure perfect readability over background elements, even when vibrating on a bike frame.
* **Ghost Interactions:** The central screen doubles as a massive touch target for Play/Pause, utilizing translucent "ghost" iconography to indicate state without muddying the primary data.

## 📡 Sensor & Hardware Integration (Powered by Android Health Services)

**(Currently in Architecture Phase)**

* **Fused GPS Telemetry:** Real-time calculation of current speed, distance, and bearing.
* **Altimeter / Barometer:** Tracking absolute elevation, cumulative gain/loss, and calculating real-time hill grade (incline %).
* **Internal Biometrics:** Continuous optical heart rate monitoring and active vs. total caloric burn calculations.
* **External BLE Support:** Capable of bridging external Bluetooth Low Energy (BLE) cycling sensors, including pedal crank Cadence (RPM) and Power Meters (Watts).

---