# ProBase Monorepo

**ProBase** is a modular Android repository designed to showcase scalable architecture, modern Android development practices, and professional-grade application structures. It serves as a foundation for multiple applications, primarily focusing on **AshBike**, a comprehensive cycling companion app.

## 📱 Applications

### 🚴 AshBike (Mobile & Wear OS)

The flagship application of this repository. AshBike is a multi-device cycling computer and tracker.

* **App Modules**:
* `applications/ashbike/apps/mobile`: The primary Android mobile application.
* `applications/ashbike/apps/wear`: A companion app for Wear OS devices.


* **Key Features**:
* **Ride Tracking**: Real-time GPS tracking, speed, distance, and elevation monitoring.
* **Sensor Integration**: Support for Bluetooth LE (BLE) Heart Rate monitors, Speed, and Cadence sensors.
* **Health Connect**: Seamless integration with Android Health Connect to sync exercise sessions and biometric data.
* **Smart Glass UI**: Dedicated UI modules for heads-up displays (likely for smart glasses).
* **Weather Intelligence**: Real-time weather updates and forecasts integrated into the ride dashboard.
* **Interactive Maps**: Integration with mapping services for route visualization.



### 📸 PhotoDo

A secondary application focused on photography and task management (implied by the name and structure).

* `applications/photodo/apps/mobile`

---

## 🏗️ Architecture

This project follows a strict **Clean Architecture** approach within a **Modular Monorepo** structure.

### Module Organization

* **`applications/`**: Contains the executable app modules (Mobile, Wear). These modules knit together various features.
* **`features/`**: Standalone, feature-specific modules. This separation allows for faster build times and better separation of concerns.
* `ble`: Bluetooth Low Energy management.
* `health`: Health Connect interactions and data management.
* `ml`: Machine Learning implementations.
* `nfc`: Near Field Communication capabilities.
* `places`: Location and place discovery (likely utilizing Maps/Yelp APIs).
* `qrscanner`: QR code scanning functionality.
* `weather`: Weather data fetching and UI components.
* `nav3`: Exploration of modern Navigation Compose patterns.


* **`core/`**: Shared foundation modules used across features and apps.
* `core:model`: Shared domain models and data classes.
* `core:data`: Repositories and data sources.
* `core:database`: Local persistence (Room).
* `core:network`: API clients (Retrofit/Apollo).
* `core:ui`: Common UI components, themes, and design system elements.
* `core:util`: Utility functions and logging.


* **`build-logic/`**: Custom Gradle Convention Plugins to standardize build configurations across all modules (e.g., Kotlin options, Compose setup, Hilt configuration).

---

## 🛠️ Tech Stack

* **Language**: [Kotlin](https://kotlinlang.org/) (100%)
* **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Asynchronous Programming**: [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
* **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
* **Database**: [Room](https://developer.android.com/training/data-storage/room)
* **Networking**:
* [Retrofit](https://square.github.io/retrofit/)
* [Apollo Kotlin](https://www.apollographql.com/docs/kotlin/) (GraphQL)


* **Platform Integrations**:
* [Health Connect API](https://developer.android.com/guide/health-and-fitness/health-connect)
* [CameraX](https://developer.android.com/training/camerax)
* Bluetooth Low Energy (BLE)
* NFC
* Wear OS Services


* **Build System**: Gradle with Version Catalogs (`libs.versions.toml`)

---

## 🚀 Getting Started

1. **Clone the repository**:
```bash
git clone https://github.com/your-username/probase.git
cd probase

```


2. **Open in Android Studio**:
   Open the project root directory in the latest version of Android Studio (Koala or later recommended).
3. **Sync Gradle**:
   Allow Gradle to sync and download dependencies.
4. **Run AshBike**:
* Select the `applications.ashbike.apps.mobile` run configuration to deploy to a phone/emulator.
* Select `applications.ashbike.apps.wear` to deploy to a Wear OS emulator.



## 🤝 Contribution

This project uses modularization to enable isolated development. You can work on individual feature modules (e.g., `:features:weather`) largely independently of the main application modules.

**Code Style**:

* The project enforces code style using `spotless` or similar configured within the `build-logic`.
* Compose UI code generally follows the state hoisting pattern.