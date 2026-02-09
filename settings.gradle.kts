pluginManagement {
    repositories {
        // ADD THIS LINE:
        includeBuild("build-logic")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ProBase"

// --- Main App ---
include(":app")

// --- Core Modules (The Foundation) ---
include(":core:ui")
include(":core:model")   // ✅ Added
include(":core:network")    // ✅ Added
include(":core:database")
include(":core:util")    // ✅ Added

// --- Standalone Features ---
include(":features:nav3")
include(":features:weather")
include(":features:places")
include(":features:health")
include(":features:ml")

// --- AshBike Product Line ---
include(":applications:ashbike:database")
include(":applications:ashbike:apps:mobile")
include(":applications:ashbike:apps:wear")
include(":applications:ashbike:features:main")
//include(":applications:ashbike:features:settings")

include(":core:data")
include(":applications:ashbike:apps:mobile:features:home")
include(":applications:ashbike:apps:mobile:features:rides")
include(":applications:ashbike:apps:mobile:features:settings")
include(":applications:ashbike:data")
include(":applications:ashbike:model")
include(":applications:ashbike:apps:mobile:features:glass")
include(":applications:ashbike:features:places")
include(":features:ble")
include(":features:nfc")
include(":features:qrscanner")
