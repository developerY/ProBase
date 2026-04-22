com.zoewave.probase.features.health
├── domain
│   ├── HealthRideRequest.kt
│   └── SyncRideUseCase.kt
└── ui
    ├── HealthEvent.kt
    ├── HealthRoute.kt                 // Main container hosting the Scaffold & Bottom Nav
    ├── HealthUiState.kt
    ├── HealthViewModel.kt
    │
    ├── components                     // Strictly generic, reusable building blocks
    │   ├── charts
    │   │   └── GenericWeeklyChart.kt
    │   ├── common
    │   │   ├── ErrorScreen.kt
    │   │   └── LoadingScreen.kt
    │   └── ExerciseSessionRow.kt      // Reusable list item
    │
    ├── overview                       // Tab: Overview
    │   └── OverviewTab.kt             // The column containing the stacked charts
    │
    ├── sessions                       // Tab: Sessions
    │   ├── SessionsTab.kt             // The LazyColumn list of sessions
    │   └── SessionDetailDialog.kt     // The popup for specific session data
    │
    └── settings                       // Tab: Settings
        ├── SettingsTab.kt             // The layout containing status and debug buttons
        └── HealthConnectionStatus.kt  // The "Access Granted" card