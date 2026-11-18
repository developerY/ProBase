applications/ashbike/
│
├── data/ (Shared)
│   └── src/main/java/.../services/RideTrackingEngine.kt  <-- The Interface
│
└── apps/
    ├── wear/ (Presentation / App Wiring)
    │   ├── build.gradle.kts
    │   └── src/main/java/.../features/main/service/BikeForegroundService.kt <-- Injects Interface
    │
    └── wear:data/ (New Module!)
        ├── build.gradle.kts
        └── src/main/java/.../wear/data/
            ├── sensor/WearExerciseClientEngine.kt        <-- The Implementation
            └── di/WearTrackingModule.kt                  <-- The Hilt Binder