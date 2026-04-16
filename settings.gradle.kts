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
include(":features:camera")
include(":features:calendar")

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
include(":features:compliance")
include(":features:ai:capture")
include(":features:ai:vision")
include(":features:ai:configuration")


include(":applications:ashbike:apps:wear:features:home")
include(":applications:ashbike:apps:wear:features:rides")
include(":applications:ashbike:apps:wear:features:settings")
include(":applications:ashbike:apps:wear:data")
include(":applications:ashbike:apps:mobile:data")


include(":applications:photodo:apps:mobile:features:home")
include(":applications:photodo:apps:mobile")
include(":applications:photodo:model")
include(":applications:photodo:db")
include(":applications:photodo:data")

include(":applications:photodo:apps:mobile:features:tasks")
include(":applications:photodo:apps:mobile:features:settings")
include(":applications:photodo:apps:mobile:core")
include(":applications:photodo:features:timebudgeting")
include(":applications:photodo:features:smartadvice")
include(":applications:photodo:features:calendar")
include(":applications:photodo:features:camera")
include(":applications:photodo:apps:wear")
include(":applications:photodo:apps:wear:features:home")
include(":applications:photodo:apps:wear:features:project")
include(":applications:photodo:apps:wear:features:task")
include(":features:camera")

// --- Seaweed Product Line ---
include(":applications:seaweed:database")
include(":applications:seaweed:data")
include(":applications:seaweed:model")
include(":applications:seaweed:features:main")
include(":applications:seaweed:features:receiptcapture")
include(":applications:seaweed:apps:mobile")
include(":applications:seaweed:apps:mobile:features:home")
include(":applications:seaweed:apps:mobile:features:transaction")
include(":applications:seaweed:apps:mobile:features:settings")
include(":applications:seaweed:apps:mobile:features:bills")
include(":applications:seaweed:apps:mobile:features:budget")
include(":applications:seaweed:apps:mobile:core")
include(":applications:seaweed:apps:wear")
include(":applications:seaweed:apps:wear:features:home")
include(":applications:seaweed:apps:wear:features:transactions")
include(":applications:seaweed:apps:wear:features:bills")

// --- GoSwift Product Line ---
include(":applications:goswift:model")
include(":applications:goswift:database")
include(":applications:goswift:data")
include(":applications:goswift:features:main")
include(":applications:goswift:apps:mobile")
include(":applications:goswift:apps:mobile:features:home")
include(":applications:goswift:apps:mobile:features:shots")
include(":applications:goswift:apps:mobile:features:settings")
include(":applications:goswift:apps:mobile:features:hydration")
include(":applications:goswift:apps:mobile:features:input")
include(":applications:goswift:apps:mobile:features:nutrition")
include(":applications:goswift:apps:wear")
include(":applications:goswift:apps:wear:features:home")
include(":applications:goswift:apps:wear:features:input")
