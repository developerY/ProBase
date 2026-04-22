# Continuous Glucose Monitoring (CGM) Module

This module provides a comprehensive and isolated implementation for Continuous Glucose Monitoring (CGM) and Blood Glucose Monitoring (BGM) systems. It is structured as a sub-module of `:features:health` to ensure clean separation of concerns and reusability.

## 🚀 Features

- **Multi-Manufacturer Support**: Integrated support for 10+ major manufacturers.
- **Unified Dashboard**: Real-time visualization of glucose levels, trend arrows, and historical data.
- **Hardware-Backed Security**: Sensitive authentication tokens are protected using **StrongBox (Titan M / Knox)** where available.
- **Hybrid Data Handling**: Seamlessly handles both continuous data streams (CGM) and discrete manual readings (BGM).

## 🛠 Supported Systems

| Manufacturer | System Type | API Accessibility |
| :--- | :--- | :--- |
| **Dexcom** | CGM | High (Share API) |
| **LifeScan (OneTouch)** | BGM | High (Developer Portal) |
| **Abbott (Libre)** | CGM | Medium (LibreLinkUp) |
| **Medtronic** | CGM | Medium (CareLink) |
| **Ascensia (Contour)** | BGM/CGM | Medium (via Partners) |
| **Medtrum** | CGM | Medium (via Partners) |
| **SiBionics** | CGM | Medium (via Community) |
| **Trividia (TRUE Metrix)** | BGM | Medium (via Partners) |
| **Standard Bluetooth** | CGM/BGM | Low (Standard SIG) |
| **NFC Scan** | BGM | Low (Libre NFC) |

## 🏗 Architecture

Following **Modern Android Development (MAD)** gold standards:

- **UI Layer**: Built with Jetpack Compose using state hoisting and MVI-lite patterns.
- **Domain Layer**: Reactive data streams using Kotlin Coroutines and Flow.
- **Data Layer**: Isolated repositories for each manufacturer, managed by a `GlucoseRepositoryFactory`.
- **Dependency Injection**: Fully integrated with Hilt using custom qualifiers for repository implementations.

## 🔐 Security

The module implements a high-security layer for user credentials:
- **`SecureTokenManager`**: Leverages the Android Keystore system.
- **Hardware Isolation**: Encryption keys are generated in the device's Secure Element (StrongBox) ensuring they never leave the hardware boundary.
- **Encryption**: All tokens are encrypted using **AES-GCM (256-bit)** before local persistence.

## 📦 Usage

To use this module, add it as a dependency in your `build.gradle.kts`:

```kotlin
implementation(project(":features:health:cgm"))
```

Then, you can include the `GlucoseTab` in your health dashboard:

```kotlin
import com.zoewave.probase.features.health.cgm.ui.GlucoseTab

@Composable
fun YourDashboard() {
    GlucoseTab()
}
```

## 🧪 Testing

The module includes unit tests for state management and source switching logic:
```bash
./gradlew :features:health:cgm:testDebugUnitTest
```
