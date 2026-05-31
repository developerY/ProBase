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
        maven { url = uri("https://jitpack.io") }
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
include(":features:health:core")
include(":features:health:cgm")
include(":features:camera")
include(":features:calendar")
include(":features:xr:ar:naillab")
include(":features:xr:ar:facelab")
include(":features:xr:glass")
include(":features:xr:xrglasses")
include(":features:payment:googlepay")
include(":features:payment:stripe")

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
include(":features:readers:nfc")
include(":features:readers:qrscanner")
include(":features:readers:barcode")
include(":features:compliance")
include(":features:graphics")
include(":features:obf")
include(":features:ai:capture")
include(":features:ai:vision")
include(":features:ai:configuration")
include(":features:ai:firebase")


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
include(":applications:seaweed:features:spendingcontrol")
include(":applications:seaweed:features:cashflow")
include(":applications:seaweed:features:affordability")
include(":applications:seaweed:apps:mobile")
include(":applications:seaweed:apps:mobile:features:home")
include(":applications:seaweed:apps:mobile:features:transaction")
include(":applications:seaweed:apps:mobile:features:settings")
include(":applications:seaweed:apps:mobile:features:bills")
include(":applications:seaweed:apps:mobile:features:budget")
include(":applications:seaweed:apps:mobile:features:glass")
include(":applications:seaweed:apps:mobile:features:smartcamera")
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

// --- KoColor Product Line ---
include(":applications:kocolor:model")
include(":applications:kocolor:db")
include(":applications:kocolor:data")
include(":applications:kocolor:features:analyzer")
include(":applications:kocolor:features:suggestions")
include(":applications:kocolor:features:inventory")
include(":applications:kocolor:features:routines")
include(":applications:kocolor:features:cosmetics")
include(":applications:kocolor:features:stitch")
include(":applications:kocolor:features:stitch")
include(":applications:kocolor:apps:mobile")
include(":applications:kocolor:apps:mobile:core")
include(":applications:kocolor:apps:mobile:features:home")
include(":applications:kocolor:apps:mobile:features:settings")
include(":applications:kocolor:apps:mobile:features:color")

// --- GotMind Product Line ---
include(":applications:gotmind:model")
include(":applications:gotmind:database")
include(":applications:gotmind:data")
include(":applications:gotmind:analytics")
include(":applications:gotmind:features:games")
include(":applications:gotmind:features:leaderboard")
include(":applications:gotmind:features:settings")
include(":applications:gotmind:features:memblox")
include(":applications:gotmind:features:mindwave")
include(":applications:gotmind:apps:mobile")

// --- RxLogic Product Line ---
include(":applications:rxlogic:model")
include(":applications:rxlogic:db")
include(":applications:rxlogic:data")
include(":applications:rxlogic:features:daily")
include(":applications:rxlogic:features:medications")
include(":applications:rxlogic:features:settings")
include(":applications:rxlogic:apps:mobile")

// --- GigWork Product Line ---
include(":applications:gigwork:model")
include(":applications:gigwork:database")
include(":applications:gigwork:data")
include(":applications:gigwork:apps:mobile")
