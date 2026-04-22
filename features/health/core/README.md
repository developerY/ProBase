# Health Core Module

The primary health sub-module within `:features:health`. This module manages the integration with Google Health Connect and provides the main health dashboards for activity tracking.

## 🚀 Features

- **Health Connect Integration**: Sync and manage activity data (steps, distance, calories, heart rate).
- **Weekly Activity Visualizations**: Interactive bar charts for weekly metric trends.
- **Session Management**: List and detailed view of recent exercise sessions.
- **Permission Orchestration**: Automated handling of Health Connect permissions and system settings.

## 🏗 Architecture

- **UI Layer**: Jetpack Compose dashboards and tabbed navigation.
- **ViewModel**: MVI-style state management for reactive UI updates.
- **Integration**: Acts as the host for other health sub-modules (e.g., `:features:health:cgm`).

## 📦 Usage

Add the dependency in your `build.gradle.kts`:

```kotlin
implementation(project(":features:health:core"))
```

Include the `HealthRoute` in your app's navigation:

```kotlin
import com.zoewave.probase.features.health.core.ui.HealthRoute

@Composable
fun AppNavigation() {
    HealthRoute()
}
```

## 🧪 Testing

```bash
./gradlew :features:health:core:testDebugUnitTest
```
