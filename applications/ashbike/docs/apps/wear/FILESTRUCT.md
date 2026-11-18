# With Features 

wear/
├── app/                           # The main application wireframe
│   ├── build.gradle.kts           # Depends on the feature modules below
│   ├── src/main/.../MainActivity.kt
│   ├── src/main/.../AshBikeUiRoute.kt
│   └── src/main/.../AshBikeApp.kt # Houses the NavDisplay
│
└── features/
    ├── home/                      # The Dashboard
    │   ├── build.gradle.kts
    │   └── src/main/.../WearHomeScreen.kt
    │   
    ├── rides/                     # The active ride tracking feature
    │   ├── build.gradle.kts
    │   ├── src/main/.../WearBikeScreen.kt
    │   └── src/main/.../WearBikeViewModel.kt
    │
    ├── history/                   # Past rides and detail views
    │   ├── build.gradle.kts
    │   └── src/main/.../RideDetailScreen.kt
    │
    └── settings/                  # Watch-specific settings
        ├── build.gradle.kts
        └── src/main/.../WearSettingsScreen.kt



---

com.ylabz.basepro.ashbike.wear
├── app
│   └── BaseProWearApp.kt          <-- (NEW) Hilt Application Entry Point
├── di
│   └── WearModule.kt              <-- (NEW) Hilt Module for Wear-specific dependencies
├── presentation
│   ├── MainActivity.kt            <-- @AndroidEntryPoint
│   ├── WearApp.kt                 <-- Root Composable (NavHost)
│   ├── theme/                     <-- Type, Color, Theme
│   ├── components/                <-- Shared Wear Composables (Chip, Button)
│   └── screens
│       └── ride
│           ├── WearBikeScreen.kt
│           └── WearBikeViewModel.kt <-- @HiltViewModel (Moves logic out of UI)
├── service
│   ├── ExerciseService.kt         <-- @AndroidEntryPoint (Your Foreground Service)
│   └── SensorHelpers.kt
├── tile
│   └── MainTileService.kt         <-- @AndroidEntryPoint
└── complication
└── MainComplicationService.kt <-- @AndroidEntryPoint

