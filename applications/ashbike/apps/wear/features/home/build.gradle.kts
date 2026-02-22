plugins {
    // 1. Apply Convention Plugins (Library, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")

    // ✅ Required for Type-Safe Navigation & Nav3
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    // Unique namespace for the Home Feature
    namespace = "com.zoewave.ashbike.wear.home"
}

dependencies {
    // --- Shared Projects ---
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:network"))
    implementation(project(":core:data"))
    implementation(project(":features:weather"))
    implementation(project(":features:places"))

    // --- AshBike Shared Modules ---
    // ✅ Keep database/data access
    implementation(project(":applications:ashbike:database"))
    implementation(project(":applications:ashbike:model"))
    implementation(project(":applications:ashbike:data"))
    implementation(project(":applications:ashbike:features:main"))
    implementation(project(":applications:ashbike:features:places"))

    // --- Serialization ---
    implementation(libs.kotlinx.serialization.json)

    // --- Navigation 3 (Strict) ---
    // ✅ UI is removed since the App module handles the display engine
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)


    // --- Maps & Location ---
    implementation(libs.google.play.services.location)
    implementation(libs.google.maps.compose)

    // --- Wear OS UI & Compose ---
    // ✅ Replaced standard Material3 with Wear Material3
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)

    // --- Hilt & Lifecycle ---
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)


    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.service)
    // ...

    // Permissions
    implementation(libs.google.accompanist.permissions)

    // --- UI Components ---
    // ✅ Icons for the dashboard
    implementation(libs.androidx.compose.material.icons.extended)

    // --- Collections ---
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.androidx.projected)

    // -- preview
    // The lightweight annotations (Required for the compiler to read your code)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.wear.tooling.preview)

    // The heavy rendering engine (Only used by Android Studio)
    debugImplementation(libs.androidx.compose.ui.tooling)


    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}