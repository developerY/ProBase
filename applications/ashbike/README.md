# AshBike 🚴‍♂️

Next-generation ride tracking built with Kotlin, Jetpack Compose, and a pure multi-module architecture.

## Overview

AshBike is a modern health and fitness application designed to deliver a **truly standalone Wear OS experience** alongside a powerful companion Android mobile app.

Our core engineering philosophy is simple: **The watch should be able to do everything the phone can do.** Whether you leave your phone at home or bring it along for the ride, AshBike seamlessly tracks your performance, persists your data locally on your wrist, and intelligently handles cloud and ecosystem syncing.

## Core Features

* **Standalone Wear OS Tracking:** Track your rides using the watch's hardware GPS and Heart Rate sensors via Android Health Services, entirely untethered from a mobile device.
* **The "Best of Both Worlds" Sync Engine:**
    * *No Phone?* The watch app operates autonomously, saving the completed ride to a local Room Database and syncing directly to the AshBike cloud backend via Wi-Fi or LTE.
    * *Phone Nearby?* The watch seamlessly beams the finished ride data over Bluetooth (via the Wearable Data Layer API) to the mobile app. The mobile app then acts as a bridge to write your official workout record into **Google Health Connect**.
* **Modern Declarative UI:** Built entirely in Jetpack Compose and Wear Compose Material 3 for fluid, responsive, and hardware-optimized interfaces (including native ScalingLazyColumns and Swipe-to-Dismiss gestures).
* **Strict Type-Safe Navigation:** Utilizes the cutting-edge Navigation 3 (Nav3) architecture. Routing is handled by pure state lists, eliminating standard string-based route parsing.

## Architecture

AshBike follows a strict feature-based, multi-module architecture. This maximizes code sharing between the phone and the watch, enforces separation of concerns, and keeps Gradle build times incredibly fast.



### Module Breakdown

* **:core:* (Model, UI, Network, Data):** Form-factor agnostic logic, state models, and utilities shared across the entire project.
* **:mobile:app** & **:wear:app:** The lightweight entry points. These modules contain the Hilt setups, main activities, and the Nav3 `NavDisplay` wireframes.
* **:wear:features:rides:** The standalone watch tracking experience. Uses `Health Services` for battery-efficient live sensor batching and `Room` DAOs for local persistence.
* **:mobile:features:rides:** The rich mobile dashboard. Uses `Health Connect` to read/write historical fitness data to the centralized Android ecosystem.

### The Tracking Data Flow



1.  **Track:** Wear OS `Health Services` continuously reads live hardware sensors.
2.  **Store:** Wear OS saves the completed run to its local `Room` database.
3.  **Handoff:** If a paired phone is detected, the `Wearable Data Layer API` transmits the payload to the mobile app in the background.
4.  **Publish:** The mobile app takes the payload and writes it to the `Health Connect` database on the phone.

## Local Development Setup

### Prerequisites
* Android Studio (Latest stable or canary supporting KSP 2.3+)
* JDK 17+
* A physical Wear OS 3+ device or emulator

### Building the Project
We use custom convention plugins to manage our build logic across both form factors.
1. Clone the repository:
   ```bash
   git clone [https://github.com/your-org/ashbike.git](https://github.com/your-org/ashbike.git)