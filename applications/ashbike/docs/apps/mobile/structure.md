applications
└── ashbike
├── apps
│   ├── mobile                  <-- The App Shell
│   │   │   build.gradle.kts
│   │   │
│   │   └── features            <-- FOLDER (Not a module itself)
│   │       ├── main            <-- MODULE (:apps:mobile:features:main)
│   │       │   ├── build.gradle.kts
│   │       │   └── src/main/java...
│   │       │
│   │       ├── settings        <-- MODULE (:apps:mobile:features:settings)
│   │       │   ├── build.gradle.kts
│   │       │   └── src/main/java...
│   │       │
│   │       └── trips           <-- MODULE (:apps:mobile:features:trips)
│   │           ├── build.gradle.kts
│   │           └── src/main/java...
│   │
│   └── wear                    <-- The Watch App Shell
│       │   build.gradle.kts
│       │
│       └── features            <-- FOLDER
│           └── run             <-- MODULE (:apps:wear:features:run)
│               ├── build.gradle.kts
│               └── src/main/java...
│
└── core                        <-- SHARED LOGIC
└── data